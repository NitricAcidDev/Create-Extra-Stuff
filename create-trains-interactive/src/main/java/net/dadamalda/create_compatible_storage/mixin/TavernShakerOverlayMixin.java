package net.dadamalda.create_compatible_storage.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nitricacid.traininteractive.TrainWorldAccess;
import com.nitricacid.traininteractive.client.TrainRayTrace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.client.gui.overlay.ShakerOverlay", remap = false)
public abstract class TavernShakerOverlayMixin {
    @WrapMethod(method = "renderShakerBlockTips")
    private void trainsInteractive$showMovingIngredients(GuiGraphics graphics, int width, int height,
            Minecraft client, LocalPlayer player, Operation<Void> original) {
        var target = TrainRayTrace.find(client);
        if (target == null) { original.call(graphics, width, height, client, player); return; }
        var oldHit = client.hitResult;
        try {
            client.hitResult = target.hit();
            ((TrainWorldAccess) target.entity()).trainsInteractive$world().run(target.hit().getBlockPos(), () -> {
                original.call(graphics, width, height, client, player);
                return null;
            });
        } finally { client.hitResult = oldHit; }
    }
}
