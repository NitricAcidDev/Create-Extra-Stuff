package com.nitricacid.createfactorytweaks.mixin;

import com.nitricacid.createfactorytweaks.FuelConfig;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ProcessingRecipe.class, remap = false)
public abstract class DistillationOutputsMixin {
    @Inject(method = "getFluidResults", at = @At("RETURN"), cancellable = true)
    private void createfactorytweaks$outputs(CallbackInfoReturnable<NonNullList<FluidStack>> cir) {
        // No hard dependency on TFMG; leave all other processing recipes alone.
        if (!getClass().getName().equals("com.drmangotea.tfmg.recipes.DistillationRecipe")) return;
        if (FuelConfig.effectivePreset() == FuelConfig.DistillationPreset.VANILLA) return;
        if (recipeName(cir.getReturnValue()) == null) return;
        NonNullList<FluidStack> results = NonNullList.create();
        for (FluidStack original : cir.getReturnValue()) {
            FluidStack copy = original.copy();
            var id = BuiltInRegistries.FLUID.getKey(copy.getFluid());
            if (id.getNamespace().equals("tfmg"))
                copy.setAmount(FuelConfig.outputAmount(id.getPath(), original.getAmount()));
            results.add(copy);
        }
        cir.setReturnValue(results);
    }
    private static String recipeName(NonNullList<FluidStack> results) {
        StringBuilder signature = new StringBuilder();
        for (FluidStack stack : results)
            signature.append(BuiltInRegistries.FLUID.getKey(stack.getFluid())).append('=').append(stack.getAmount()).append(';');
        return switch (signature.toString()) {
            case "tfmg:heavy_oil=120;tfmg:diesel=60;tfmg:kerosene=30;tfmg:naphtha=10;tfmg:gasoline=60;tfmg:lpg=60;" -> "crude_oil";
            case "tfmg:heavy_oil=150;tfmg:diesel=45;tfmg:gasoline=5;" -> "crude_oil_light_distillation";
            case "tfmg:heavy_oil=120;tfmg:diesel=60;tfmg:kerosene=30;tfmg:gasoline=60;tfmg:lpg=60;" -> "crude_oil_no_naphtha";
            case "tfmg:heavy_oil=100;tfmg:lubrication_oil=25;tfmg:diesel=50;tfmg:kerosene=20;tfmg:naphtha=5;" -> "heavy_oil";
            case "tfmg:heavy_oil=100;tfmg:diesel=50;tfmg:lubrication_oil=50;" -> "heavy_oil_light_distillation";
            case "tfmg:heavy_oil=100;tfmg:lubrication_oil=30;tfmg:diesel=50;tfmg:kerosene=20;" -> "heavy_oil_no_naphtha";
            default -> null;
        };
    }

}
