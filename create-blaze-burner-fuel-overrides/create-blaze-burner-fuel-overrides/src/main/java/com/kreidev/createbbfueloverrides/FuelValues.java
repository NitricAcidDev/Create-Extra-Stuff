package com.kreidev.createbbfueloverrides;

import java.util.Map;
import java.util.Optional;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.resources.ResourceLocation;

public record FuelValues(double consumptionMbPerTick, double burnDurationSecondsPerBucket,
                         int totalHeatUnits, BlazeBurnerBlock.HeatLevel heatLevel) {
    private static final Map<String, FuelValues> VALUES = Map.of(
            "lpg", new FuelValues(1.25, 40, 31000, BlazeBurnerBlock.HeatLevel.KINDLED),
            "naphtha", new FuelValues(2.0, 25, 36000, BlazeBurnerBlock.HeatLevel.SEETHING),
            "gasoline", new FuelValues(1.56, 32, 34000, BlazeBurnerBlock.HeatLevel.KINDLED),
            "kerosene", new FuelValues(1.42, 35, 35000, BlazeBurnerBlock.HeatLevel.KINDLED),
            "diesel", new FuelValues(1.31, 38, 38000, BlazeBurnerBlock.HeatLevel.KINDLED),
            "heavy_oil", new FuelValues(1.11, 45, 40000, BlazeBurnerBlock.HeatLevel.KINDLED)
    );
    public static Optional<FuelValues> forFluid(ResourceLocation id) {
        if (id == null || !id.getNamespace().equals("tfmg")) return Optional.empty();
        return Optional.ofNullable(VALUES.get(id.getPath()));
    }
}
