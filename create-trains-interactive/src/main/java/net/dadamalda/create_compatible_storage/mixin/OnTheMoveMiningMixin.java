package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.client.TrainMiningGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.woudlee.createonthemove.client.ContraptionMiningClientHandler.TargetContraptionHit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.woudlee.createonthemove.client.ContraptionMiningClientHandler", remap = false)
public abstract class OnTheMoveMiningMixin {
    @Inject(method = "findTargetBlock", at = @At("RETURN"), cancellable = true)
    private static void trainsInteractive$keepMiningTarget(Minecraft client, LocalPlayer player, CallbackInfoReturnable<TargetContraptionHit> cir) {
        var target = cir.getReturnValue();
        if (target != null && !TrainMiningGuard.allows(target.entity().getId(), target.localPos())) cir.setReturnValue(null);
    }
}
