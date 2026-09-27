package com.nitricacid.createfactorytweaks;

import java.util.Map;
import java.util.Optional;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.resources.ResourceLocation;

public record FuelValues(double consumptionMbPerTick, double burnDurationSecondsPerBucket,
                         BlazeBurnerBlock.HeatLevel heatLevel) {
    // One duration and heat tier shared by both burner types and all fuel amounts.
    public static final Map<String, FuelValues> DEFAULTS = Map.of(
            "lpg", new FuelValues(5.0 / 125, 1250, BlazeBurnerBlock.HeatLevel.SEETHING),
            "naphtha", new FuelValues(5.0 / 185, 1850, BlazeBurnerBlock.HeatLevel.SEETHING),
            "gasoline", new FuelValues(5.0 / 188, 1880, BlazeBurnerBlock.HeatLevel.KINDLED),
            "kerosene", new FuelValues(5.0 / 190, 1900, BlazeBurnerBlock.HeatLevel.KINDLED),
            "diesel", new FuelValues(5.0 / 200, 2000, BlazeBurnerBlock.HeatLevel.KINDLED),
            "heavy_oil", new FuelValues(0, 0, BlazeBurnerBlock.HeatLevel.NONE)
    );
    public int burnTicks(int amountMb) {
        return (int) Math.round(amountMb * burnDurationSecondsPerBucket * 20.0 / 1000.0);
    }
    public int bucketBurnTicks() { return burnTicks(1000); }
    public int addBucketTicks(int remainingTicks, boolean sameFuel) {
        return sameFuel ? (int) Math.min((long) Math.max(0, remainingTicks) + bucketBurnTicks(), Integer.MAX_VALUE) : bucketBurnTicks();
    }
    public static Optional<FuelValues> forFluid(ResourceLocation id) {
        if (id == null || !id.getNamespace().equals("tfmg")) return Optional.empty();
        FuelValues defaults = DEFAULTS.get(id.getPath());
        return defaults == null ? Optional.empty() : Optional.of(FuelConfig.values(id.getPath(), defaults));
    }
}
