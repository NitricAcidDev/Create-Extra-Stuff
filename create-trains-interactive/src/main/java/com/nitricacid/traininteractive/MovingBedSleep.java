package com.nitricacid.traininteractive;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.function.Supplier;

/** Supplies vanilla sleep checks with the moving bed's actual position and blocks. */
public final class MovingBedSleep {
    private record Target(ServerPlayer player, AbstractContraptionEntity entity, BlockPos pos) {}
    private static final ThreadLocal<Target> ACTIVE = new ThreadLocal<>();
    private MovingBedSleep() {}

    public static <T> T run(ServerPlayer player, AbstractContraptionEntity entity, BlockPos pos, Supplier<T> action) {
        var previous = ACTIVE.get();
        ACTIVE.set(new Target(player, entity, pos));
        try { return action.get(); }
        finally { if (previous == null) ACTIVE.remove(); else ACTIVE.set(previous); }
    }

    public static Object vanillaResult(ServerPlayer player, Object original) {
        var target = ACTIVE.get();
        if (target == null || target.player != player) return original;
        var problem = check(player, target.entity, target.pos);
        return problem == null ? Either.right(Unit.INSTANCE) : Either.left(problem);
    }

    public static Player.BedSleepingProblem check(ServerPlayer player, AbstractContraptionEntity entity, BlockPos pos) {
        var c = entity.getContraption();
        var info = c == null ? null : c.getBlocks().get(pos);
        if (info == null || !(info.state().getBlock() instanceof BedBlock) || player.isSleeping() || !player.isAlive())
            return Player.BedSleepingProblem.OTHER_PROBLEM;
        if (!player.level().dimensionType().natural()) return Player.BedSleepingProblem.NOT_POSSIBLE_HERE;
        var facing = info.state().getValue(BedBlock.FACING);
        var head = info.state().getValue(BedBlock.PART) == BedPart.HEAD ? pos : pos.relative(facing);
        var foot = head.relative(facing.getOpposite());
        var headGlobal = entity.toGlobalVector(Vec3.atBottomCenterOf(head), 1);
        var footGlobal = entity.toGlobalVector(Vec3.atBottomCenterOf(foot), 1);
        if (!inRange(player, headGlobal) && !inRange(player, footGlobal)) return Player.BedSleepingProblem.TOO_FAR_AWAY;
        var world = c.getContraptionWorld();
        if (world.getBlockState(head.above()).isSuffocating(world, head.above())
                || world.getBlockState(foot.above()).isSuffocating(world, foot.above())) return Player.BedSleepingProblem.OBSTRUCTED;
        if (player.level().isDay()) return Player.BedSleepingProblem.NOT_POSSIBLE_NOW;
        if (!player.isCreative() && !player.level().getEntitiesOfClass(Monster.class,
                new AABB(headGlobal.x - 8, headGlobal.y - 5, headGlobal.z - 8, headGlobal.x + 8, headGlobal.y + 5, headGlobal.z + 8),
                monster -> monster.isPreventingPlayerRest(player)).isEmpty()) return Player.BedSleepingProblem.NOT_SAFE;
        return null;
    }

    private static boolean inRange(Player player, Vec3 bed) {
        return Math.abs(player.getX() - bed.x) <= 3 && Math.abs(player.getY() - bed.y) <= 2 && Math.abs(player.getZ() - bed.z) <= 3;
    }
}
