package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.client.TrainPickBlock;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class TrainPickBlockMixin {
    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$pick(CallbackInfo ci) {
        if (TrainPickBlock.pick((Minecraft) (Object) this)) ci.cancel();
    }
}
