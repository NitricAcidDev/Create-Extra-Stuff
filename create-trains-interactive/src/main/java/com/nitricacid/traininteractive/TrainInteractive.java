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
        // Known furniture only. No guesswork based on names such as 'seat' or 'cabinet'.
        for (Block block : BuiltInRegistries.BLOCK) {
            if (FurnitureSeats.height(block.defaultBlockState()).isPresent()) {
                if (MovementBehaviour.REGISTRY.get(block) == null)
                    MovementBehaviour.REGISTRY.register(block, new SeatMovementBehaviour());
                if (MovingInteractionBehaviour.REGISTRY.get(block) == null)
                    MovingInteractionBehaviour.REGISTRY.register(block, new SeatInteractionBehaviour());
            }
        }
        if (ModList.get().isLoaded("kaleidoscope_tavern")) TavernCompat.register();
    }
}
