package net.dadamalda.create_compatible_storage.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nitricacid.traininteractive.MovingBedSleep;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class MovingBedSleepMixin {
    @ModifyExpressionValue(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"))
    private Object trainsInteractive$validateMovingBed(Object original) {
        // NeoForge still receives the vanilla result and can apply its normal sleep event overrides.
        return MovingBedSleep.vanillaResult((ServerPlayer) (Object) this, original);
    }
}
