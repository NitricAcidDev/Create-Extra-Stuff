package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class MovingTrainPlacement {
    private MovingTrainPlacement() {}
    public static boolean place(Player player, AbstractContraptionEntity entity, BlockPos pos, Direction side, InteractionHand hand) {
        if (!player.mayBuild() || player.isSpectator() || entity.getContraption() == null
                || !entity.canInteractWithBlock(player, pos, player.blockInteractionRange() + 1)) return false;
        var world = ((TrainWorldAccess) entity).trainsInteractive$world();
        var location = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(side.getNormal()).scale(.5));
        var hit = new BlockHitResult(location, side, pos, false);
        var look = entity.reverseRotation(player.getLookAngle(), 1);
        boolean result = world.run(pos, () -> player.getItemInHand(hand).getItem().useOn(new UseOnContext(entity.level(), player, hand, player.getItemInHand(hand), hit) {
            @Override public Direction getHorizontalDirection() { return Direction.getNearest(look.x, 0, look.z); }
            @Override public float getRotation() { return (float) (Math.atan2(-look.x, look.z) * 180 / Math.PI); }
        }).consumesAction());
        world.flush(true);
        return result;
    }
}
