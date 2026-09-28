package com.nitricacid.createfactorytweaks.mixin;

import com.nitricacid.createfactorytweaks.DistillationRecipes;
import com.nitricacid.createfactorytweaks.FuelConfig;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import java.util.HashSet;
import java.util.Set;
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
        var ingredients = ((ProcessingRecipe<?, ?>) (Object) this).getFluidIngredients();
        if (ingredients.size() != 1) return;
        FluidStack[] inputs = ingredients.getFirst().getFluids();
        if (inputs.length != 1) return;
        var inputId = BuiltInRegistries.FLUID.getKey(inputs[0].getFluid());
        if (!inputId.getNamespace().equals("tfmg")) return;
        Set<String> outputNames = new HashSet<>();
        for (FluidStack stack : cir.getReturnValue()) {
            var id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
            if (!id.getNamespace().equals("tfmg") || !outputNames.add(id.getPath())) return;
        }
        String recipe = DistillationRecipes.identify(inputId.getPath(), outputNames);
        if (recipe == null) return;
        NonNullList<FluidStack> results = NonNullList.create();
        for (FluidStack original : cir.getReturnValue()) {
            FluidStack copy = original.copy();
            var id = BuiltInRegistries.FLUID.getKey(copy.getFluid());
            copy.setAmount(FuelConfig.outputAmount(recipe, id.getPath(), original.getAmount()));
            results.add(copy);
        }
        cir.setReturnValue(results);
    }
}
