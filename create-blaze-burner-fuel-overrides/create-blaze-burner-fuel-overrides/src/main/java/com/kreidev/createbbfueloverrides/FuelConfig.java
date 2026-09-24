package com.kreidev.createbbfueloverrides;

import net.neoforged.neoforge.common.ModConfigSpec;

public class FuelConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue NAPHTA_ENABLED = BUILDER
            .comment("Allow Create Crafts & Additions Naphta to burn in the Liquid Blaze Burner as superheated fuel")
            .translation("createbbfueloverrides.configuration.naphtaEnabled")
            .define("naphtaEnabled", true);

    public static final ModConfigSpec.IntValue CONSUMPTION_PER_TICK = BUILDER
            .comment("Millibuckets of Naphta consumed per tick while burning")
            .translation("createbbfueloverrides.configuration.consumptionPerTick")
            .defineInRange("consumptionPerTick", 1, 1, 1000);

    public static final ModConfigSpec.IntValue BURN_TICKS_PER_MILLIBUCKET = BUILDER
            .comment("Burn ticks granted per millibucket of Naphta consumed")
            .translation("createbbfueloverrides.configuration.burnTicksPerMillibucket")
            .defineInRange("burnTicksPerMillibucket", 36, 1, 100000);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
