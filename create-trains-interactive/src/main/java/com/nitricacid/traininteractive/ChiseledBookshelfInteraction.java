package com.nitricacid.traininteractive;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.dadamalda.create_compatible_storage.mixin.ChiseledBookshelfAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ChiseledBookshelfInteraction extends MovingInteractionBehaviour {
    public static void register() {
        if (REGISTRY.get(Blocks.CHISELED_BOOKSHELF) == null)
            REGISTRY.register(Blocks.CHISELED_BOOKSHELF, new ChiseledBookshelfInteraction());
    }

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos pos, AbstractContraptionEntity entity) {
        var info = entity.getContraption().getBlocks().get(pos);
        if (info == null || !info.state().is(Blocks.CHISELED_BOOKSHELF) || !player.mayBuild()) return false;
        if (!entity.canInteractWithBlock(player, pos, player.blockInteractionRange() + 1)) return false;
        Vec3 start = entity.toLocalVector(player.getEyePosition(), 1);
        Vec3 end = entity.toLocalVector(player.getEyePosition().add(player.getLookAngle().scale(player.blockInteractionRange() + 1)), 1);
        var hit = info.state().getShape(EmptyBlockGetter.INSTANCE, pos).clip(start, end, pos);
        if (hit == null) return false;
        var clicked = ((ChiseledBookshelfAccess) info.state().getBlock()).trainsInteractive$getHitSlot(hit, info.state());
        if (clicked.isEmpty()) return false;
        if (entity.level().isClientSide) return true;

        // Read the native inventory, but don't call its setters: they update stationary world blocks.
        var shelf = new ChiseledBookShelfBlockEntity(pos, info.state());
        if (info.nbt() != null) shelf.loadWithComponents(info.nbt().copy(), entity.level().registryAccess());
        var books = NonNullList.withSize(6, ItemStack.EMPTY);
        for (int i = 0; i < books.size(); i++) books.set(i, shelf.getItem(i).copy());
        int slot = clicked.getAsInt();
        ItemStack held = player.getItemInHand(hand);
        ItemStack stored = books.get(slot);
        boolean removing = !stored.isEmpty();
        if (!removing && !held.is(ItemTags.BOOKSHELF_BOOKS)) return false;
        ItemStack book;
        if (removing) {
            book = stored.copy();
            books.set(slot, ItemStack.EMPTY);
            if (!player.getInventory().add(stored)) player.drop(stored, false);
        } else {
            player.awardStat(Stats.ITEM_USED.get(held.getItem()));
            book = held.consumeAndReturn(1, player);
            books.set(slot, book);
        }
        var tag = shelf.saveWithFullMetadata(entity.level().registryAccess());
        ContainerHelper.saveAllItems(tag, books, true, entity.level().registryAccess());
        tag.putInt("last_interacted_slot", slot);
        tag.remove("x"); tag.remove("y"); tag.remove("z");
        var state = info.state();
        for (int i = 0; i < books.size(); i++)
            state = state.setValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(i), !books.get(i).isEmpty());
        entity.setBlock(pos, new StructureBlockInfo(pos, state, tag));
        CabinetDataPacket.apply(entity, pos, tag);
        PacketDistributor.sendToPlayersTrackingEntity(entity, new CabinetDataPacket(entity.getId(), pos, tag));
        var sound = removing
                ? (book.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP)
                : (book.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT);
        entity.level().playSound(null, BlockPos.containing(entity.toGlobalVector(Vec3.atCenterOf(pos), 1)), sound, SoundSource.BLOCKS, 1f, 1f);
        return true;
    }
}
