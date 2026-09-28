package com.nitricacid.createfactorytweaks;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ConfigRegression {
    public static void main(String[] args) throws Exception {
        CommentedConfig data = CommentedConfig.inMemory();
        FuelConfig.SPEC.correct(data);
        var accept = java.util.Arrays.stream(FuelConfig.SPEC.getClass().getMethods())
                .filter(m -> m.getName().equals("acceptConfig")).findFirst().orElseThrow();
        Class<?> loadedType = accept.getParameterTypes()[0].getPermittedSubclasses()[0];
        var constructor = loadedType.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object loaded = constructor.newInstance(data, null, null);
        accept.invoke(FuelConfig.SPEC, loaded);
        check(FuelConfig.locked(), "Default lock");
        // The Create Addition capacity is read from a loaded game config, so test
        // the shared gate with its standard 10,000-tick value in this headless run.
        int pumpLimit = 10000;
        check(FuelValues.canFeed(pumpLimit, pumpLimit), "Bucket allowed exactly at pump limit");
        check(!FuelValues.canFeed(pumpLimit + 1, pumpLimit), "Bucket blocked above pump limit even with overflow");
        check(FuelValues.maxStoredTicks(pumpLimit) == pumpLimit + value("diesel").bucketBurnTicks(),
                "Previously stacked time limited to threshold plus one full bucket");
        check(FuelConfig.effectivePreset() == FuelConfig.DistillationPreset.VANILLA, "Vanilla is the default preset");
        check(FuelConfig.outputAmount("crude_oil", "naphtha", 10) == 10, "Vanilla keeps original outputs");
        checkRecipe("crude_oil", "crude_oil", "heavy_oil", "diesel", "kerosene", "naphtha", "gasoline", "lpg");
        checkRecipe("crude_oil_light_distillation", "crude_oil", "heavy_oil", "diesel", "gasoline");
        checkRecipe("crude_oil_no_naphtha", "crude_oil", "heavy_oil", "diesel", "kerosene", "gasoline", "lpg");
        checkRecipe("heavy_oil", "heavy_oil", "heavy_oil", "lubrication_oil", "diesel", "kerosene", "naphtha");
        checkRecipe("heavy_oil_light_distillation", "heavy_oil", "heavy_oil", "diesel", "lubrication_oil");
        checkRecipe("heavy_oil_no_naphtha", "heavy_oil", "heavy_oil", "lubrication_oil", "diesel", "kerosene");
        check(DistillationRecipes.identify("heavy_oil", java.util.Set.of("diesel")) == null, "Unknown recipe stays untouched");
        FuelConfig.ENABLED.set(false);
        FuelConfig.CHARGE_AMOUNT.set(200);
        ModConfigSpec.DoubleValue duration = FuelConfig.SPEC.getValues().get("blazeBurnerFuels.naphtha.liquidBurnSecondsPer100Mb");
        duration.set(300.0);
        check(FuelConfig.enabled() && FuelConfig.chargeAmount() == 100, "Lock ignores global overrides");
        check(value("naphtha").burnDurationSecondsPerBucket() == 1850, "Locked naphtha default");
        check(value("lpg").burnDurationSecondsPerBucket() == 1250, "LPG default");
        FuelConfig.LOCK_DEFAULTS.set(false);
        check(!FuelConfig.enabled() && FuelConfig.chargeAmount() == 200, "Unlock applies global overrides");
        check(value("naphtha").burnDurationSecondsPerBucket() == 3000, "Unlock applies live fuel override");
        check(value("naphtha").bucketBurnTicks() == 60000 && value("naphtha").burnTicks(100) == 6000, "Live duration update reaches both burner paths");
        ModConfigSpec.EnumValue<com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel> heat = FuelConfig.SPEC.getValues().get("blazeBurnerFuels.naphtha.heatLevel");
        heat.set(com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED);
        check(value("naphtha").heatLevel() == com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED, "Live heat update is shared");
        heat.set(com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.SEETHING);
        FuelConfig.DISTILLATION_PRESET.set(FuelConfig.DistillationPreset.ADJUSTED);
        check(FuelConfig.effectivePreset() == FuelConfig.DistillationPreset.ADJUSTED, "Adjusted preset activates when unlocked");
        check(FuelConfig.outputAmount("crude_oil", "naphtha", 10) == 30, "Adjusted crude naphtha");
        check(FuelConfig.outputAmount("heavy_oil", "naphtha", 5) == 15, "Adjusted heavy oil naphtha");
        check(FuelConfig.outputAmount("crude_oil_light_distillation", "heavy_oil", 150) == 125, "Adjusted crude light value");
        ModConfigSpec.IntValue naphtha = FuelConfig.SPEC.getValues().get("distillationOutputs.crude_oil.naphthaMb");
        naphtha.set(50);
        check(FuelConfig.outputAmount("crude_oil", "naphtha", 10) == 30, "Adjusted stays fixed when custom values change");
        FuelConfig.DISTILLATION_PRESET.set(FuelConfig.DistillationPreset.CUSTOM);
        check(FuelConfig.outputAmount("crude_oil", "naphtha", 10) == 50, "Crude custom output edits apply live");
        check(FuelConfig.outputAmount("heavy_oil", "naphtha", 5) == 15, "Heavy oil custom output stays separate");
        ModConfigSpec.IntValue heavyNaphtha = FuelConfig.SPEC.getValues().get("distillationOutputs.heavy_oil.naphthaMb");
        heavyNaphtha.set(20);
        check(FuelConfig.outputAmount("heavy_oil", "naphtha", 5) == 20, "Heavy oil custom output edits apply live");
        check(FuelConfig.outputAmount("crude_oil", "naphtha", 10) == 50, "Heavy oil edits do not change crude oil");
        FuelConfig.LOCK_DEFAULTS.set(true);
        check(FuelConfig.effectivePreset() == FuelConfig.DistillationPreset.VANILLA, "Lock forces vanilla output");
        check(FuelConfig.outputAmount("heavy_oil", "naphtha", 5) == 5, "Lock ignores custom output");
        FuelConfig.LOCK_DEFAULTS.set(false);
        check(FuelConfig.effectivePreset() == FuelConfig.DistillationPreset.CUSTOM, "Unlock restores selected custom preset");
        FuelConfig.LOCK_DEFAULTS.set(true);
        check(value("naphtha").burnDurationSecondsPerBucket() == 1850, "Relock restores defaults");
        String[] fuels = {"lpg", "naphtha", "gasoline", "kerosene", "diesel"};
        double[] seconds = {125, 185, 188, 190, 200};
        for (int i = 0; i < fuels.length; i++) {
            FuelValues values = value(fuels[i]);
            check(values.burnTicks(100) == (int)(seconds[i] * 20), "Exact charge duration: " + fuels[i]);
            check(values.burnTicks(1000) == (int)(seconds[i] * 200), "Exact bucket duration: " + fuels[i]);
            check(values.bucketBurnTicks() == values.burnTicks(1000), "Normal bucket matches liquid bucket: " + fuels[i]);
            check(values.bucketBurnTicks() == values.burnTicks(100) * 10, "Full bucket equals ten charges: " + fuels[i]);
            check(values.addBucketTicks(200, true) == values.bucketBurnTicks() + 200, "Bucket top-up retains all ticks: " + fuels[i]);
            check(values.addBucketTicks(200, false) == values.bucketBurnTicks(), "Fuel type replacement: " + fuels[i]);
            check(values.burnTicks(50) == (int)(seconds[i] * 10), "Half charge scales correctly: " + fuels[i]);
            check(Math.abs(values.consumptionMbPerTick() - 5.0 / seconds[i]) < 0.0000001, "Consumption: " + fuels[i]);
            check(FuelConfig.fuelEnabled(fuels[i]), "Enabled fuel: " + fuels[i]);
            check(values.heatLevel().name().equals(i < 2 ? "SEETHING" : "KINDLED"), "Heat tier: " + fuels[i]);
        }
        check(!FuelConfig.fuelEnabled("heavy_oil"), "Heavy oil disabled when locked");
        check(value("heavy_oil").burnTicks(100) == 0 && value("heavy_oil").heatLevel().name().equals("NONE"), "Heavy oil has no heat or duration");
        FuelConfig.LOCK_DEFAULTS.set(false);
        FuelConfig.ENABLED.set(true);
        check(!FuelConfig.fuelEnabled("heavy_oil"), "Heavy oil disabled when unlocked by default");
        ModConfigSpec.DoubleValue zero = FuelConfig.SPEC.getValues().get("blazeBurnerFuels.lpg.liquidBurnSecondsPer100Mb");
        zero.set(0.0);
        check(!FuelConfig.fuelEnabled("lpg") && value("lpg").consumptionMbPerTick() == 0, "Zero duration disables fuel without division by zero");
        check(value("naphtha").addBucketTicks(Integer.MAX_VALUE - 1, true) == Integer.MAX_VALUE, "Bucket accumulation cannot overflow");
        check(FuelConfig.SPEC.getValues().get("blazeBurnerFuels.naphtha.solidBucketHeatUnits") == null, "No conflicting solid burner setting");
        System.out.println("PASS: burner fuel limits and rates; vanilla default, fixed adjusted outputs, separate crude/heavy custom recipes, and locking");
    }
    private static FuelValues value(String fuel) { return FuelConfig.values(fuel, FuelValues.DEFAULTS.get(fuel)); }
    private static void checkRecipe(String expected, String input, String... outputs) {
        check(expected.equals(DistillationRecipes.identify(input, java.util.Set.of(outputs))), "Identify " + expected);
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
