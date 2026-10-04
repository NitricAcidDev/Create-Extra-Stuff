package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingTrainWorld;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class NativeTrainPlayerSoundMixin {
    @Inject(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$playerSound(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        var entity = (Entity) (Object) this;
        if (!(entity instanceof Player) || MovingTrainWorld.active(entity.level()) == null) return;
        MovingTrainWorld.outside(() -> { entity.playSound(sound, volume, pitch); return null; });
        ci.cancel();
    }
}
