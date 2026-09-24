package com.kreidev.createbbfueloverrides.mixin;

import com.kreidev.createbbfueloverrides.FuelConfig;
import com.kreidev.createbbfueloverrides.FuelValues;
import com.kreidev.createbbfueloverrides.NaphthaFuel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false, priority = 500)
public abstract class BlazeBurnerBlockEntityMixin {
    @Shadow protected BlazeBurnerBlockEntity.FuelType activeFuel;
    @Shadow protected int remainingBurnTime;
    @Shadow protected abstract void playSound();
    @Shadow public abstract void updateBlockState();
    @Shadow public abstract void spawnParticleBurst(boolean soulFlame);

    @Inject(method = "tryUpdateFuel", at = @At("HEAD"), cancellable = true)
    private void createbbfueloverrides$tryUpdateFuel(ItemStack stack, boolean forceOverflow, boolean simulate,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (!FuelConfig.NAPHTA_ENABLED.get() || stack == null) return;
        BlazeBurnerBlockEntity self = (BlazeBurnerBlockEntity) (Object) this;
        FluidStack fluidStack = createbbfueloverrides$fluidFromBucket(stack);
        if (fluidStack == null) return;
        FuelValues values = NaphthaFuel.values(fluidStack.getFluid());
        if (values == null) return;

        BlazeBurnerBlockEntity.FuelType newFuel = values.heatLevel() == BlazeBurnerBlock.HeatLevel.SEETHING
                ? BlazeBurnerBlockEntity.FuelType.SPECIAL : BlazeBurnerBlockEntity.FuelType.NORMAL;
        int newBurnTime = Math.min((int) Math.round(values.totalHeatUnits() / 10.0), BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY);
        if (newFuel.ordinal() < activeFuel.ordinal()) return;
        if (newFuel == activeFuel && remainingBurnTime > BlazeBurnerBlockEntity.INSERTION_THRESHOLD && !forceOverflow) return;
        if (simulate) { cir.setReturnValue(true); return; }

        boolean sameFuel = activeFuel == newFuel;
        activeFuel = newFuel;
        remainingBurnTime = sameFuel ? Math.min(remainingBurnTime + newBurnTime, BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY) : newBurnTime;
        playSound();
        updateBlockState();
        spawnParticleBurst(newFuel == BlazeBurnerBlockEntity.FuelType.SPECIAL);
        cir.setReturnValue(true);
    }

    @Unique
    private FluidStack createbbfueloverrides$fluidFromBucket(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        // Only the actual TFMG bucket items belong in the normal burner path.
        // This deliberately excludes fluid straws and other reusable fluid tools,
        // which expose an item fluid capability but are not fuel containers.
        if (itemId == null || !itemId.getNamespace().equals("tfmg")
                || (!itemId.getPath().endsWith("_bucket") && !itemId.getPath().startsWith("bucket_of_"))) return null;
        var handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if (handler != null && !handler.getFluidInTank(0).isEmpty()) return handler.getFluidInTank(0);
        String path = itemId.getPath();
        if (path.endsWith("_bucket")) path = path.substring(0, path.length() - 7);
        else if (path.startsWith("bucket_of_")) path = path.substring(10);
        ResourceLocation fluidId = ResourceLocation.fromNamespaceAndPath("tfmg", path);
        return BuiltInRegistries.FLUID.getOptional(fluidId).map(fluid -> new FluidStack(fluid, 1000)).orElse(null);
    }
}
