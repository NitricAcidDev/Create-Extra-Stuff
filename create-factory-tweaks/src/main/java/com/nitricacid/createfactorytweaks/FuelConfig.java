package com.nitricacid.createfactorytweaks;

import java.util.LinkedHashMap;
import java.util.Map;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FuelConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue LOCK_DEFAULTS = BUILDER
            .comment("Checked: enforce built-in defaults and ignore overrides. Uncheck to use custom values. Changes apply to newly added burner fuel. Distillation uses original recipe outputs.")
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
    public enum DistillationPreset { VANILLA, ADJUSTED, CUSTOM }
    private record Output(int adjusted, ModConfigSpec.IntValue custom) {}
    private static final Map<String, Map<String, Output>> OUTPUTS = new LinkedHashMap<>();
    public static final ModConfigSpec.EnumValue<DistillationPreset> DISTILLATION_PRESET;
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
        BUILDER.comment("Vanilla uses original TFMG recipes. Adjusted uses built-in values. Custom has separate values for each distillation recipe below.")
                .translation("createfactorytweaks.configuration.distillationPreset");
        DISTILLATION_PRESET = BUILDER.defineEnum("preset", DistillationPreset.VANILLA);
        recipe("crude_oil", new String[]{"heavy_oil", "diesel", "kerosene", "naphtha", "gasoline", "lpg"},
                new int[]{100, 60, 30, 30, 60, 60});
        recipe("crude_oil_light_distillation", new String[]{"heavy_oil", "diesel", "gasoline"},
                new int[]{125, 45, 5});
        recipe("crude_oil_no_naphtha", new String[]{"heavy_oil", "diesel", "kerosene", "gasoline", "lpg"},
                new int[]{100, 60, 30, 60, 60});
        recipe("heavy_oil", new String[]{"heavy_oil", "lubrication_oil", "diesel", "kerosene", "naphtha"},
                new int[]{83, 25, 50, 20, 15});
        recipe("heavy_oil_light_distillation", new String[]{"heavy_oil", "diesel", "lubrication_oil"},
                new int[]{83, 50, 50});
        recipe("heavy_oil_no_naphtha", new String[]{"heavy_oil", "lubrication_oil", "diesel", "kerosene"},
                new int[]{83, 30, 50, 20});
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    private static void recipe(String name, String[] fluids, int[] adjusted) {
        BUILDER.translation("createfactorytweaks.configuration.recipe." + name).push(name);
        Map<String, Output> values = new LinkedHashMap<>();
        for (int i = 0; i < fluids.length; i++) {
            String fluid = fluids[i];
            int amount = adjusted[i];
            values.put(fluid, new Output(amount, BUILDER.comment("Custom output in mB for " + fluid + " in this recipe.")
                    .translation("createfactorytweaks.configuration.output." + fluid)
                    .defineInRange(fluid + "Mb", amount, 1, 100000)));
        }
        OUTPUTS.put(name, values);
        BUILDER.pop();
    }
    public static DistillationPreset effectivePreset() {
        return locked() ? DistillationPreset.VANILLA : DISTILLATION_PRESET.get();
    }
    public static int outputAmount(String recipe, String fluid, int original) {
        DistillationPreset preset = effectivePreset();
        Map<String, Output> values = OUTPUTS.get(recipe);
        if (preset == DistillationPreset.VANILLA || values == null || !values.containsKey(fluid)) return original;
        Output value = values.get(fluid);
        return preset == DistillationPreset.ADJUSTED ? value.adjusted() : value.custom().get();
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
}
