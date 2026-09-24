package com.kreidev.createbbfueloverrides;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

public class NaphthaFuel {
    public static boolean isNaphtha(Fluid fluid) {
        return values(fluid) != null;
    }
    public static FuelValues values(Fluid fluid) {
        if (fluid == null) return null;
        return FuelValues.forFluid(BuiltInRegistries.FLUID.getKey(fluid)).orElse(null);
    }
}
