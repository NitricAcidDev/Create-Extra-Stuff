package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.client.BookshelfInspectorCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.lukasabbe.network.client.BookShelfInventoryHandlerServer", remap = false)
public abstract class BookshelfInspectorResponseMixin {
    @Inject(method = "receive(Lcom/lukasabbe/network/packets/BookShelfInventoryPayload;Lnet/minecraft/client/player/LocalPlayer;)V", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$ignoreOldStationaryResponse(CallbackInfo ci) {
        if (BookshelfInspectorCompat.isInspectingContraption()) ci.cancel();
    }
}
