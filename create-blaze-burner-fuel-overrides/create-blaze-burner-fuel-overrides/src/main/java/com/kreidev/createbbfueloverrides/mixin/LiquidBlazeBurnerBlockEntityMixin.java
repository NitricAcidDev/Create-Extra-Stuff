package com.kreidev.createbbfueloverrides.mixin;

import java.util.Optional;

import com.kreidev.createbbfueloverrides.FuelConfig;
import com.kreidev.createbbfueloverrides.FuelValues;
import com.kreidev.createbbfueloverrides.NaphthaFuel;
import com.kreidev.createbbfueloverrides.LimitedBurnerTank;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LiquidBlazeBurnerBlockEntity.class, remap = false, priority = 500)
public abstract class LiquidBlazeBurnerBlockEntityMixin {

    @Shadow
    protected FluidTank tankInventory;

    @Shadow
    protected int remainingBurnTime;

    @Shadow
    protected LiquidBlazeBurnerBlockEntity.FuelType activeFuel;

    @Shadow
    BlazeBurnerBlock.HeatLevel heatLevel;

    @Shadow
    protected abstract void playSound();

    @Shadow
    protected abstract void onFluidStackChanged(FluidStack fluidStack);

    @Shadow
    public abstract void updateBlockState();

    @Shadow
    public abstract void spawnParticleBurst(boolean soulFlame);

    @Inject(method = "createInventory", at = @At("RETURN"), cancellable = true)
    private void createbbfueloverrides$limitPipeIntake(CallbackInfoReturnable<SmartFluidTank> cir) {
        cir.setReturnValue(new LimitedBurnerTank(cir.getReturnValue().getCapacity(), this::onFluidStackChanged));
    }

    @Inject(
            method = "find",
            at = @At("HEAD"),
            cancellable = true
    )
    private void createbbfueloverrides$find(FluidStack stack, Level level, CallbackInfoReturnable<Optional<RecipeHolder<LiquidBurningRecipe>>> cir) {
        if (stack != null && NaphthaFuel.isNaphtha(stack.getFluid()) && !FuelConfig.NAPHTA_ENABLED.get()) {
            cir.setReturnValue(Optional.empty());
        }
    }

    @Inject(method = "tryUpdateLiquid", at = @At("HEAD"), cancellable = true)
    private void createbbfueloverrides$tryUpdateLiquid(ItemStack stack, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        LiquidBlazeBurnerBlockEntity self = (LiquidBlazeBurnerBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (!FuelConfig.NAPHTA_ENABLED.get() || stack == null || level == null) return;
        var handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        FluidStack input = handler != null && !handler.getFluidInTank(0).isEmpty()
                ? handler.getFluidInTank(0) : createbbfueloverrides$fluidFromTfmgBucket(stack);
        if (input == null || input.isEmpty()) return;
        if (!NaphthaFuel.isNaphtha(input.getFluid()) || tankInventory.getCapacity() - tankInventory.getFluidAmount() < 1000) return;
        if (!simulate) {
            if (tankInventory instanceof LimitedBurnerTank limitedTank) {
                limitedTank.fillUnrestricted(new FluidStack(input.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
            } else {
                tankInventory.fill(new FluidStack(input.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
            }
            level.playSound(null, self.getBlockPos(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, .125f, .75f);
        }
        cir.setReturnValue(true);
    }

    @Unique
    private FluidStack createbbfueloverrides$fluidFromTfmgBucket(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null || !itemId.getNamespace().equals("tfmg")) return null;
        String path = itemId.getPath();
        if (path.endsWith("_bucket")) path = path.substring(0, path.length() - "_bucket".length());
        else if (path.startsWith("bucket_of_")) path = path.substring("bucket_of_".length());
        ResourceLocation fluidId = ResourceLocation.fromNamespaceAndPath("tfmg", path);
        return BuiltInRegistries.FLUID.getOptional(fluidId).map(fluid -> new FluidStack(fluid, 1000)).orElse(null);
    }

    @Inject(
            method = "burningTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void createbbfueloverrides$burningTick(CallbackInfo ci) {
        LiquidBlazeBurnerBlockEntity self = (LiquidBlazeBurnerBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        FluidStack fluidStack = tankInventory.getFluid();
        Fluid fluid = fluidStack.getFluid();
        FuelValues values = NaphthaFuel.values(fluid);
        if (values == null) {
            return;
        }

        ci.cancel();

        if (!FuelConfig.NAPHTA_ENABLED.get()) {
            return;
        }

        int consume = 100;
        if (tankInventory.getFluidAmount() < consume) {
            return;
        }
        if (remainingBurnTime > LiquidBlazeBurnerBlockEntity.MAX_HEAT_CAPACITY) {
            return;
        }

        // CSV Total Heat Units are expressed per bucket. Convert them to ticks per 100 mB event:
        // naphtha 36000 HU / 1000 * 100 = 3600 ticks.
        int burnTicks = (int) Math.round(consume * values.totalHeatUnits() / 1000.0);
        if (burnTicks < 1) return;
        LiquidBlazeBurnerBlockEntity.FuelType fuelType = values.heatLevel() == BlazeBurnerBlock.HeatLevel.SEETHING
                ? LiquidBlazeBurnerBlockEntity.FuelType.SPECIAL : LiquidBlazeBurnerBlockEntity.FuelType.NORMAL;
        remainingBurnTime = activeFuel == fuelType ? remainingBurnTime + burnTicks : burnTicks;
        activeFuel = fuelType;
        tankInventory.drain(consume, IFluidHandler.FluidAction.EXECUTE);

        BlazeBurnerBlock.HeatLevel prev = heatLevel;
        playSound();
        updateBlockState();

        if (prev != heatLevel) {
            level.playSound(null, self.getBlockPos(), SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS,
                    .125f + level.random.nextFloat() * .125f, 1.15f - level.random.nextFloat() * .25f);
            spawnParticleBurst(true);
        }
    }
}
