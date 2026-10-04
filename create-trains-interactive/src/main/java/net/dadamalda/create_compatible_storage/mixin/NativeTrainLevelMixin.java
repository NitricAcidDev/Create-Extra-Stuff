package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingTrainWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class NativeTrainLevelMixin {
    private MovingTrainWorld trainsInteractive$active() { return MovingTrainWorld.active((Level) (Object) this); }

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$state(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        var world = trainsInteractive$active();
        if (world != null) cir.setReturnValue(world.state(pos));
    }
    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$fluid(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        var world = trainsInteractive$active();
        if (world != null) cir.setReturnValue(world.state(pos).getFluidState());
    }
    @Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$blockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        var world = trainsInteractive$active();
        if (world != null) cir.setReturnValue(world.blockEntity(pos));
    }
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$set(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> cir) {
        var world = trainsInteractive$active();
        if (world != null) cir.setReturnValue(world.set(pos, state));
    }
    @Inject(method = "setBlockEntity", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$setEntity(BlockEntity be, CallbackInfo ci) {
        var world = trainsInteractive$active();
        if (world != null) { world.setBlockEntity(be); ci.cancel(); }
    }
    @Inject(method = "removeBlockEntity", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$removeEntity(BlockPos pos, CallbackInfo ci) {
        var world = trainsInteractive$active();
        if (world != null) { world.removeBlockEntity(pos); ci.cancel(); }
    }
    @Inject(method = "blockEntityChanged", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$changed(BlockPos pos, CallbackInfo ci) {
        if (trainsInteractive$active() != null) ci.cancel();
    }
    @Inject(method = "updateNeighbourForOutputSignal", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$comparator(BlockPos pos, Block block, CallbackInfo ci) {
        if (trainsInteractive$active() != null) ci.cancel();
    }
}
