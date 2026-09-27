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
        System.out.println("PASS: synchronized normal/liquid buckets and charges, no clipping or overflow, updated rates, shared live settings, locking and heat tiers; distillation override removed");
    }
    private static FuelValues value(String fuel) { return FuelConfig.values(fuel, FuelValues.DEFAULTS.get(fuel)); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
