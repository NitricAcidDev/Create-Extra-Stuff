package net.dadamalda.create_compatible_storage.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nitricacid.traininteractive.MovingTrainWorld;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.github.mortuusars.exposure.world.block.LightroomBlock;

@Pseudo
@Mixin(targets = "io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity", remap = false)
public abstract class MovingLightroomEntityMixin {
    @Inject(method = "isRefracted", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$refracted(CallbackInfoReturnable<Boolean> cir) {
        var be = (BlockEntity) (Object) this;
        var world = MovingTrainWorld.owner(be);
        if (world != null && world.state(be.getBlockPos()).hasProperty(LightroomBlock.REFRACTED))
            cir.setReturnValue(world.state(be.getBlockPos()).getValue(LightroomBlock.REFRACTED));
    }
    @Inject(method = "isPrinting", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$printing(CallbackInfoReturnable<Boolean> cir) {
        var be = (BlockEntity) (Object) this;
        var world = MovingTrainWorld.owner(be);
        if (world != null && world.state(be.getBlockPos()).hasProperty(LightroomBlock.PRINTING))
            cir.setReturnValue(world.state(be.getBlockPos()).getValue(LightroomBlock.PRINTING));
    }
    @WrapMethod(method = "hasSufficientLightLevel")
    private boolean trainsInteractive$light(Operation<Boolean> original) {
        var be = (BlockEntity) (Object) this;
        var world = MovingTrainWorld.owner(be);
        return world == null ? original.call() : world.run(be.getBlockPos(), original::call);
    }
}
