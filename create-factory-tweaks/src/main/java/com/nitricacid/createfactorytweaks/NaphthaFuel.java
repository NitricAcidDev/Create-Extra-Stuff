package com.nitricacid.createfactorytweaks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

public class NaphthaFuel {
    public static boolean isNaphtha(Fluid fluid) {
        return values(fluid) != null;
    }
    public static boolean enabled(Fluid fluid) {
        var id = BuiltInRegistries.FLUID.getKey(fluid);
        return values(fluid) != null && FuelConfig.fuelEnabled(id.getPath());
    }
    public static FuelValues values(Fluid fluid) {
        if (fluid == null) return null;
        return FuelValues.forFluid(BuiltInRegistries.FLUID.getKey(fluid)).orElse(null);
    }
}
