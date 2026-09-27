package com.nitricacid.createfactorytweaks;

import java.util.LinkedHashMap;
import java.util.Map;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FuelConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue LOCK_DEFAULTS = BUILDER
            .comment("Checked: enforce built-in defaults and ignore overrides. Uncheck to use custom values. Changes apply to future fuel charges and distillation batches.")
            .translation("createfactorytweaks.configuration.lockDefaults")
            .define("lockDefaults", true);
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable the existing TFMG burner overrides for all supported fuels.")
            .translation("createfactorytweaks.configuration.enabled")
            .define("enabled", true);
    public static final ModConfigSpec.IntValue TRANSFER_LIMIT = BUILDER
            .comment("Maximum pipe transfer in mB; bucket insertion remains unrestricted.")
            .translation("createfactorytweaks.configuration.pipeTransferLimit")
            .defineInRange("pipeTransferLimit", 100, 1, 1000);
    public static final ModConfigSpec.IntValue CHARGE_AMOUNT = BUILDER
            .comment("Liquid consumed per burner charge in mB.")
            .translation("createfactorytweaks.configuration.chargeAmount")
            .defineInRange("chargeAmount", 100, 1, 1000);
    private record Settings(ModConfigSpec.BooleanValue enabled, ModConfigSpec.DoubleValue duration,
                            ModConfigSpec.EnumValue<BlazeBurnerBlock.HeatLevel> heat) {}
    private static final Map<String, Settings> FUELS = new LinkedHashMap<>();
    private static final Map<String, ModConfigSpec.IntValue> OUTPUTS = new LinkedHashMap<>();
    private static final Map<String, Integer> OUTPUT_DEFAULTS = new LinkedHashMap<>();
    private static final Map<String, Integer> OUTPUT_BASELINES = new LinkedHashMap<>();
    public static final ModConfigSpec SPEC;
    static {
        BUILDER.translation("createfactorytweaks.configuration.blazeBurnerFuels").push("blazeBurnerFuels");
        for (String fuel : new String[]{"lpg", "naphtha", "gasoline", "kerosene", "diesel", "heavy_oil"}) {
            FuelValues defaults = FuelValues.DEFAULTS.get(fuel);
            BUILDER.translation("createfactorytweaks.configuration." + fuel).push(fuel);
            FUELS.put(fuel, new Settings(
                    BUILDER.translation("createfactorytweaks.configuration.fuelEnabled").define("enabled", !fuel.equals("heavy_oil")),
                    BUILDER.comment("Seconds per 100 mB in either burner. A full bucket provides ten times this duration. Average mB/t = 5 / seconds. Zero disables burning. Rounded to whole ticks per charge. Already charged fuel keeps its remaining time.")
                            .translation("createfactorytweaks.configuration.liquidBurnSecondsPer100Mb")
                            .defineInRange("liquidBurnSecondsPer100Mb", defaults.burnDurationSecondsPerBucket() / 10.0, 0.0, 10000),
                    BUILDER.comment("Burner heat tier: NONE disables burning; KINDLED or SEETHING provides heat.")
                            .translation("createfactorytweaks.configuration.heatLevel")
                            .defineEnum("heatLevel", defaults.heatLevel(), BlazeBurnerBlock.HeatLevel.NONE, BlazeBurnerBlock.HeatLevel.KINDLED, BlazeBurnerBlock.HeatLevel.SEETHING)));
            BUILDER.pop();
        }
        BUILDER.pop();
        BUILDER.translation("createfactorytweaks.configuration.distillationOutputs").push("distillationOutputs");
        output("heavy_oil", 100, 120);
        output("diesel", 60, 60);
        output("kerosene", 30, 30);
        output("naphtha", 30, 10);
        output("gasoline", 60, 60);
        output("lpg", 60, 60);
        output("lubrication_oil", 25, 25);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    private static void output(String fluid, int amount, int baseline) {
        OUTPUT_DEFAULTS.put(fluid, amount);
        OUTPUT_BASELINES.put(fluid, baseline);
        OUTPUTS.put(fluid, BUILDER.comment("Reference output mB for " + fluid + ". Other distillation recipes scale automatically using their original proportions.")
                .translation("createfactorytweaks.configuration.output." + fluid)
                .defineInRange(fluid + "Mb", amount, 1, 100000));
    }
    public static boolean locked() { return !SPEC.isLoaded() || LOCK_DEFAULTS.get(); }
    public static boolean enabled() { return locked() || ENABLED.get(); }
    public static int transferLimit() { return locked() ? 100 : TRANSFER_LIMIT.get(); }
    public static int chargeAmount() { return locked() ? 100 : CHARGE_AMOUNT.get(); }
    public static FuelValues values(String fuel, FuelValues defaults) {
        if (locked()) return defaults;
        Settings settings = FUELS.get(fuel);
        double duration = settings.duration.get();
        return new FuelValues(duration > 0 ? 5.0 / duration : 0, duration * 10.0, settings.heat.get());
    }
    public static boolean fuelEnabled(String fuel) {
        FuelValues defaults = FuelValues.DEFAULTS.get(fuel);
        if (defaults == null || !enabled()) return false;
        FuelValues values = values(fuel, defaults);
        return values.burnDurationSecondsPerBucket() > 0 && values.heatLevel() != BlazeBurnerBlock.HeatLevel.NONE
                && (locked() ? !fuel.equals("heavy_oil") : FUELS.get(fuel).enabled.get());
    }
    public static int outputAmount(String recipe, String fluid, int original) {
        if (!OUTPUTS.containsKey(fluid)) return original;
        int reference = locked() ? OUTPUT_DEFAULTS.get(fluid) : OUTPUTS.get(fluid).get();
        return Math.max(1, (int) Math.round(original * (double) reference / OUTPUT_BASELINES.get(fluid)));
    }
}
