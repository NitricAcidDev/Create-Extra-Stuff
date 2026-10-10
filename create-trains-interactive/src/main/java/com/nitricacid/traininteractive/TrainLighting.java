package com.nitricacid.traininteractive;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;

/** Local light propagation. Train coordinates never enter the stationary chunk light engine. */
public final class TrainLighting {
    private final MovingTrainWorld world;
    private final Map<BlockPos, Integer> light = new HashMap<>();
    private final Map<BlockPos, net.minecraft.world.level.block.state.BlockState> states = new HashMap<>();
    public TrainLighting(MovingTrainWorld world) { this.world = world; }

    public int block(BlockPos pos) {
        return world.run(pos, () -> {
            boolean changed = states.size() != world.contraption.getBlocks().size();
            if (!changed) for (var info : world.contraption.getBlocks().values())
                if (states.get(info.pos()) != info.state()) { changed = true; break; }
            if (changed) rebuild();
            return light.getOrDefault(pos, 0);
        });
    }

    private void rebuild() {
        states.clear(); light.clear();
        var queue = new ArrayDeque<BlockPos>();
        for (var info : world.contraption.getBlocks().values()) {
            states.put(info.pos(), info.state());
            int emission = info.state().getLightEmission(world.entity.level(), info.pos());
            if (emission > 0) { light.put(info.pos(), emission); queue.add(info.pos()); }
        }
        while (!queue.isEmpty()) {
            var from = queue.removeFirst();
            int value = light.get(from);
            if (value <= 1) continue;
            for (var direction : Direction.values()) {
                var to = from.relative(direction);
                int opacity = world.state(to).getLightBlock(world.entity.level(), to);
                int next = value - Math.max(1, opacity);
                if (next > light.getOrDefault(to, 0)) { light.put(to, next); queue.addLast(to); }
            }
        }
    }

    public int sky(BlockPos pos) {
        // Follow world-up through the train, including pitched/rotated contraptions.
        var up = world.entity.reverseRotation(new net.minecraft.world.phys.Vec3(0, 1, 0), 1);
        var point = net.minecraft.world.phys.Vec3.atCenterOf(pos);
        double distance = world.contraption.bounds == null ? 32 : world.contraption.bounds.getSize() * 3 + 2;
        for (double step = .5; step < distance; step += .5) {
            var check = BlockPos.containing(point.add(up.scale(step)));
            if (world.run(check, () -> world.state(check).getLightBlock(world.entity.level(), check)) >= 15) return 0;
        }
        var global = BlockPos.containing(world.global(point));
        return MovingTrainWorld.outside(() -> world.entity.level().getBrightness(LightLayer.SKY, global));
    }

    public int combined(BlockPos pos) {
        return Math.max(block(pos), sky(pos) * (15 - world.entity.level().getSkyDarken()) / 15);
    }
}
