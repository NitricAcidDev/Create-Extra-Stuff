package com.nitricacid.traininteractive;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

public final class TrainInteractive {
    private TrainInteractive() {}

    public static void register() {
        ChiseledBookshelfInteraction.register();
        com.simibubi.create.api.contraption.BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) ->
                BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals("kaleidoscope_tavern:barrel")
                        ? com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult.SUCCESS
                        : com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult.PASS);
        com.simibubi.create.api.contraption.BlockMovementChecks.registerAttachedCheck((state, level, pos, direction) -> {
            var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (id.getNamespace().equals("farmersdelight") && direction == net.minecraft.core.Direction.DOWN
                    && com.simibubi.create.api.contraption.BlockMovementChecks.isBrittle(state) && state.canSurvive(level, pos))
                return com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult.SUCCESS;
            if (id.toString().equals("kaleidoscope_tavern:barrel")) {
                var other = level.getBlockState(pos.relative(direction));
                if (other.is(state.getBlock()) && barrelOrigin(pos, state).equals(barrelOrigin(pos.relative(direction), other)))
                    return com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult.SUCCESS;
            }
            return com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult.PASS;
        });
        // Known furniture only. No guesswork based on names such as 'seat' or 'cabinet'.
        for (Block block : BuiltInRegistries.BLOCK) {
            // Explicit entries take precedence over On the Move's universal
            // provider, which can otherwise mask Create's wooden-trapdoor provider.
            if (block instanceof net.minecraft.world.level.block.TrapDoorBlock)
                OnTheMoveCompat.registerIfUnclaimed(MovingInteractionBehaviour.REGISTRY, block,
                        new com.simibubi.create.content.contraptions.behaviour.TrapdoorMovingInteraction());
            if (FurnitureSeats.height(block.defaultBlockState()).isPresent()) {
                if (MovementBehaviour.REGISTRY.get(block) == null)
                    MovementBehaviour.REGISTRY.register(block, new SeatMovementBehaviour());
                if (MovingInteractionBehaviour.REGISTRY.get(block) == null)
                    MovingInteractionBehaviour.REGISTRY.register(block, new SeatInteractionBehaviour());
            }
        }
        if (ModList.get().isLoaded("kaleidoscope_tavern")) TavernCompat.register();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (MovingTrainWorld.isServiceBlock(block.defaultBlockState()))
                OnTheMoveCompat.registerIfUnclaimed(MovingInteractionBehaviour.REGISTRY, block, new NativeServiceInteraction());
        }
    }

    private static net.minecraft.core.BlockPos barrelOrigin(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        int index = 4, height = 0;
        for (var property : state.getProperties()) {
            if (property.getName().equals("index")) index = (Integer) state.getValue(property);
            if (property.getName().equals("layer")) height = ((Enum<?>) state.getValue(property)).ordinal();
        }
        return pos.offset(1 - index % 3, -height, 1 - index / 3);
    }
}
