package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingTrainWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.mortuusars.exposure.world.level.LevelUtil", remap = false)
public abstract class MovingExposureLightMixin {
    @Inject(method = "getLightLevelAt", at = @At("HEAD"), cancellable = true)
    private static void trainsInteractive$light(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        var world = MovingTrainWorld.current();
        if (world != null) cir.setReturnValue(world.lighting().combined(pos));
    }
}
