package com.nitricacid.createtfmgaviation.verification;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Runs with the published dependency mods. These classes are excluded from the release JAR. */
@GameTestHolder("createtfmgaviation")
@PrefixGameTestTemplate(false)
public final class AviationRecipeTests {
    private static final BlockPos BASIN = new BlockPos(1, 2, 1);

    @GameTest(template = "empty", templateNamespace = "createtfmgaviation")
    public static void heatedBatchProducesAviationKerosene(GameTestHelper helper) {
        BasinBlockEntity basin = setup(helper, HeatLevel.KINDLED, "tfmg:kerosene", 800, 200);
        BasinRecipe recipe = recipe(helper);
        helper.assertTrue(BasinRecipe.match(basin, recipe), "Heated Basin must match");
        helper.assertTrue(BasinRecipe.apply(basin, recipe), "Batch must process");
        IFluidHandler tank = handler(helper);
        FluidStack fuel = tank.drain(new FluidStack(fluid("aeroengineering:aviation_kerosene"), 2000), FluidAction.EXECUTE);
        helper.assertTrue(fuel.getAmount() == 1000, "Must produce exactly 1,000 mB of AeroEngine's actual fuel");
        helper.assertTrue(basin.inputTank.isEmpty(), "Must consume both inputs completely");
        helper.assertTrue(!BasinRecipe.match(basin, recipe), "Empty Basin must not produce a second free batch");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "createtfmgaviation")
    public static void unheatedBasinRejectsBatch(GameTestHelper helper) {
        BasinBlockEntity basin = setup(helper, HeatLevel.SMOULDERING, "tfmg:kerosene", 800, 200);
        helper.assertTrue(!BasinRecipe.apply(basin, recipe(helper)), "Unheated Basin must reject the recipe");
        assertInputsUnchanged(helper, 800, 200);
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "createtfmgaviation")
    public static void shortKeroseneRejectsBatch(GameTestHelper helper) {
        BasinBlockEntity basin = setup(helper, HeatLevel.KINDLED, "tfmg:kerosene", 799, 200);
        helper.assertTrue(!BasinRecipe.apply(basin, recipe(helper)), "799 mB kerosene must not process");
        assertInputsUnchanged(helper, 799, 200);
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "createtfmgaviation")
    public static void shortHeavyOilRejectsBatch(GameTestHelper helper) {
        BasinBlockEntity basin = setup(helper, HeatLevel.KINDLED, "tfmg:kerosene", 800, 199);
        helper.assertTrue(!BasinRecipe.apply(basin, recipe(helper)), "199 mB heavy oil must not process");
        assertInputsUnchanged(helper, 800, 199);
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "createtfmgaviation")
    public static void wrongFuelRejectsBatch(GameTestHelper helper) {
        BasinBlockEntity basin = setup(helper, HeatLevel.KINDLED, "tfmg:diesel", 800, 200);
        helper.assertTrue(!BasinRecipe.apply(basin, recipe(helper)), "Diesel must not substitute for kerosene");
        helper.succeed();
    }

    private static BasinRecipe recipe(GameTestHelper helper) {
        var id = ResourceLocation.fromNamespaceAndPath("createtfmgaviation", "mixing/aviation_kerosene");
        var holder = helper.getLevel().getRecipeManager().byKey(id)
                .orElseThrow(() -> new AssertionError("Bundled aviation recipe was not loaded"));
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(
                ResourceLocation.parse("aeroengineering:mixing/aviation_kerosene")).isPresent(),
                "AeroEngine's original recipe must remain available");
        helper.assertTrue(holder.value().getType() == com.simibubi.create.AllRecipeTypes.MIXING.getType(),
                "Recipe must load as Create mixing");
        return (BasinRecipe) holder.value();
    }

    private static BasinBlockEntity setup(GameTestHelper helper, HeatLevel heat, String fuel, int kerosene, int oil) {
        helper.setBlock(BASIN.below(), AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, heat));
        helper.setBlock(BASIN, AllBlocks.BASIN.getDefaultState());
        BasinBlockEntity basin = (BasinBlockEntity) helper.getBlockEntity(BASIN);
        basin.initialize();
        IFluidHandler tank = handler(helper);
        helper.assertTrue(tank.fill(new FluidStack(fluid(fuel), kerosene), FluidAction.EXECUTE) == kerosene,
                "Basin must accept fuel input");
        helper.assertTrue(tank.fill(new FluidStack(fluid("tfmg:heavy_oil"), oil), FluidAction.EXECUTE) == oil,
                "Basin must accept heavy oil input");
        return basin;
    }

    private static IFluidHandler handler(GameTestHelper helper) {
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(BASIN), null);
        if (tank == null) throw new AssertionError("Basin fluid capability is missing");
        return tank;
    }

    private static Fluid fluid(String id) {
        ResourceLocation location = ResourceLocation.parse(id);
        if (!BuiltInRegistries.FLUID.containsKey(location)) throw new AssertionError("Missing fluid " + id);
        return BuiltInRegistries.FLUID.get(location);
    }

    private static void assertInputsUnchanged(GameTestHelper helper, int kerosene, int oil) {
        IFluidHandler tank = handler(helper);
        helper.assertTrue(tank.drain(new FluidStack(fluid("tfmg:kerosene"), 1000), FluidAction.SIMULATE).getAmount() == kerosene,
                "Failed batch must preserve kerosene");
        helper.assertTrue(tank.drain(new FluidStack(fluid("tfmg:heavy_oil"), 1000), FluidAction.SIMULATE).getAmount() == oil,
                "Failed batch must preserve heavy oil");
    }
}
