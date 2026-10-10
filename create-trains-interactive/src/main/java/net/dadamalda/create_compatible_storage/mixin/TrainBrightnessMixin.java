package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingTrainWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAndTintGetter.class)
public interface TrainBrightnessMixin {
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$brightness(LightLayer layer, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof Level level) {
            var world = MovingTrainWorld.active(level);
            if (world != null) cir.setReturnValue(layer == LightLayer.BLOCK ? world.lighting().block(pos) : world.lighting().sky(pos));
        }
    }
    @Inject(method = "getRawBrightness", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$raw(BlockPos pos, int skyDarken, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof Level level) {
            var world = MovingTrainWorld.active(level);
            if (world != null) cir.setReturnValue(Math.max(world.lighting().block(pos), world.lighting().sky(pos) - skyDarken));
        }
    }
}
