package com.nitricacid.traininteractive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;

/** Refresh onboard dust and lamps without notifying stationary blocks at local coordinates. */
public final class TrainSignals {
    private TrainSignals() {}
    public static void update(MovingTrainWorld world) {
        var wires = world.contraption.getBlocks().values().stream()
                .filter(info -> info.state().getBlock() instanceof RedStoneWireBlock).map(info -> info.pos()).toList();
        for (int pass = 0; pass < 16; pass++) {
            boolean changed = false;
            for (var pos : wires) {
                var before = world.state(pos);
                world.run(pos, () -> { before.handleNeighborChanged(world.entity.level(), pos, Blocks.AIR, pos, false); return null; });
                changed |= world.state(pos) != before;
            }
            if (!changed) break;
        }
        for (var info : java.util.List.copyOf(world.contraption.getBlocks().values())) {
            var block = info.state().getBlock();
            if (block instanceof RedstoneLampBlock) {
                world.run(info.pos(), () -> {
                    boolean lit = world.state(info.pos()).getValue(RedstoneLampBlock.LIT);
                    boolean powered = world.entity.level().hasNeighborSignal(info.pos());
                    if (lit != powered) world.state(info.pos()).handleNeighborChanged(world.entity.level(), info.pos(), Blocks.AIR, info.pos(), false);
                    return null;
                });
            } else if (BuiltInRegistries.BLOCK.getKey(block).toString().equals("exposure:lightroom")) {
                world.run(info.pos(), () -> {
                    for (var direction : Direction.values())
                        world.state(info.pos()).handleNeighborChanged(world.entity.level(), info.pos(), Blocks.AIR, info.pos().relative(direction), false);
                    return null;
                });
            } else if (BuiltInRegistries.BLOCK.getKey(block).toString().equals("createharmonics:andesite_jukebox")) {
                int power = world.run(info.pos(), () -> world.entity.level().getBestNeighborSignal(info.pos()));
                var current = world.contraption.getBlocks().get(info.pos());
                if (current.nbt() != null && current.nbt().getInt("RedstonePower") != power) {
                    var data = current.nbt().copy(); data.putInt("RedstonePower", power);
                    world.updateData(info.pos(), data);
                }
            }
        }
    }
}
