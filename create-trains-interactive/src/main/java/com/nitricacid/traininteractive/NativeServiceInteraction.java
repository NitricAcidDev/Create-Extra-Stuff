package com.nitricacid.traininteractive;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class NativeServiceInteraction extends MovingInteractionBehaviour {
    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos pos, AbstractContraptionEntity entity) {
        if (!player.mayBuild() || player.isSpectator() || entity.getContraption() == null
                || !entity.canInteractWithBlock(player, pos, player.blockInteractionRange() + 1)) return false;
        var info = entity.getContraption().getBlocks().get(pos);
        if (info == null) return false;
        if (entity.getContraption().getStorage().getAllItemStorages().containsKey(pos))
            return entity.getContraption().getStorage().handlePlayerStorageInteraction(entity.getContraption(), player, pos);
        var world = ((TrainWorldAccess) entity).trainsInteractive$world();
        var start = entity.toLocalVector(player.getEyePosition(), 1);
        var end = entity.toLocalVector(player.getEyePosition().add(player.getLookAngle().scale(player.blockInteractionRange() + 1)), 1);
        var hit = world.run(pos, () -> info.state().getShape(entity.level(), pos).clip(start, end, pos));
        if (hit == null) return false;
        if (entity.level().isClientSide) return true;
        var previousMenu = player.containerMenu;
        boolean result = world.run(pos, () -> interact(player, hand, hit, world));
        if (player.containerMenu != previousMenu) world.run(pos, () -> { MovingServiceMenus.opened(player, world); return null; });
        world.flush(true);
        return result;
    }

    public static boolean interact(Player player, InteractionHand hand, BlockHitResult hit, MovingTrainWorld world) {
        var state = world.state(hit.getBlockPos());
        var held = player.getItemInHand(hand);
        var itemResult = state.useItemOn(held, world.entity.level(), player, hand, hit);
        if (itemResult.consumesAction()) return true;
        if (itemResult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                && state.useWithoutItem(world.entity.level(), player, hit).consumesAction()) return true;
        if (held.isEmpty()) return false;
        if (held.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
            if (!canPlace(blockItem.getBlock())) return false;
            if (!TrainEditingConfig.allows(blockItem.getBlock())) { TrainEditingConfig.rejected(player); return false; }
        }
        // Shaker pouring, bottle stacking and placing prepared food are item actions.
        var look = world.entity.reverseRotation(player.getLookAngle(), 1);
        UseOnContext context = new UseOnContext(world.entity.level(), player, hand, held, hit) {
            @Override public Direction getHorizontalDirection() { return Direction.getNearest(look.x, 0, look.z); }
            @Override public float getRotation() { return (float) (Math.atan2(-look.x, look.z) * 180 / Math.PI); }
        };
        return held.getItem().useOn(context).consumesAction();
    }

    public static boolean canPlace(net.minecraft.world.level.block.Block block) {
        var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        if (id.getNamespace().equals("farmersdelight") && java.util.Set.of("cooking_pot", "stove", "skillet", "cutting_board").contains(id.getPath())) return true;
        for (Class<?> type = block.getClass(); type != null; type = type.getSuperclass()) {
            var name = type.getName();
            if (name.equals("vectorwing.farmersdelight.common.block.FeastBlock")) return true;
            if (name.startsWith("com.github.ysbbbbbb.kaleidoscopetavern.block.")
                    && java.util.Set.of("ShakerBlock", "BottleBlock", "DrinkBlock", "GlasswareBlock").contains(type.getSimpleName())) return true;
        }
        return false;
    }
}
