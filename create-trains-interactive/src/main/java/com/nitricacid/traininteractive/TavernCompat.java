package com.nitricacid.traininteractive;

import com.github.ysbbbbbb.kaleidoscopetavern.block.AbstractStorageBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.dadamalda.create_compatible_storage.mixin.TavernCabinetAccess;
import net.dadamalda.create_compatible_storage.mixin.TavernStorageAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TavernCompat {
    private TavernCompat() {}

    public static void register() {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof BarCabinetBlock || block instanceof AbstractStorageBlock)) continue;
            // Existing integrations can provide specialised functionality for these blocks.
            if (MovingInteractionBehaviour.REGISTRY.get(block) == null)
                MovingInteractionBehaviour.REGISTRY.register(block, new CabinetInteraction());
        }
    }

    public static final class CabinetInteraction extends MovingInteractionBehaviour {
        @Override
        public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos pos, AbstractContraptionEntity entity) {
            if (hand != InteractionHand.MAIN_HAND) return false;
            StructureBlockInfo info = entity.getContraption().getBlocks().get(pos);
            if (info == null || !(info.state().getBlock() instanceof EntityBlock block)) return false;
            if (entity.level().isClientSide) return true;
            if (!entity.canInteractWithBlock(player, pos, player.blockInteractionRange() + 1)) return false;

            Vec3 start = entity.toLocalVector(player.getEyePosition(), 1);
            Vec3 end = entity.toLocalVector(player.getEyePosition().add(player.getLookAngle().scale(player.blockInteractionRange() + 1)), 1);
            BlockHitResult hit = info.state().getShape(EmptyBlockGetter.INSTANCE, pos).clip(start, end, pos);
            if (hit == null) return false;

            BlockEntity be = block.newBlockEntity(pos, info.state());
            if (be == null) return false;
            // Keep level unset: native refresh() must not update an unrelated world block at localPos.
            if (info.nbt() != null) be.loadWithComponents(info.nbt().copy(), entity.level().registryAccess());
            if (!interact(player, info, be, hit)) return false;
            var tag = be.saveWithFullMetadata(entity.level().registryAccess());
            tag.remove("x"); tag.remove("y"); tag.remove("z");
            CabinetDataPacket.apply(entity, pos, tag);
            PacketDistributor.sendToPlayersTrackingEntity(entity, new CabinetDataPacket(entity.getId(), pos, tag));
            Vec3 worldPos = entity.toGlobalVector(Vec3.atCenterOf(pos), 1);
            entity.level().playSound(null, BlockPos.containing(worldPos), SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.8f, 1f);
            return true;
        }

        public static boolean interact(Player player, StructureBlockInfo info, BlockEntity be, BlockHitResult hit) {
            ItemStack held = player.getMainHandItem();
            BlockPos pos = info.pos();
            var facing = info.state().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            if (be instanceof BarCabinetBlockEntity cabinet) {
                Vec3 point = hit.getLocation().subtract(Vec3.atLowerCornerOf(pos));
                boolean left = switch (facing) {
                    case NORTH -> point.x > 0.5;
                    case SOUTH -> point.x < 0.5;
                    case EAST -> point.z < 0.5;
                    case WEST -> point.z > 0.5;
                    default -> false;
                };
                return ((TavernCabinetAccess) info.state().getBlock()).trainsInteractive$onClick(cabinet, player, held, left);
            }
            if (!(be instanceof StorageBlockEntity storage)) return false;
            TavernStorageAccess access = (TavernStorageAccess) info.state().getBlock();
            int slot = access.trainsInteractive$slot(facing, pos, hit);
            if (slot < 0 || slot >= storage.getItems().getSlots()) return false;
            ItemStack stored = storage.getItems().getStackInSlot(slot);
            if (held.isEmpty()) {
                if (stored.isEmpty()) return false;
                player.setItemInHand(InteractionHand.MAIN_HAND, stored.copy());
                storage.getItems().setStackInSlot(slot, ItemStack.EMPTY);
                return true;
            }
            if (!(held.getItem() instanceof BottleBlockItem) || access.trainsInteractive$blocked(held) || !stored.isEmpty()) return false;
            storage.getItems().setStackInSlot(slot, held.split(1));
            return true;
        }
    }
}
