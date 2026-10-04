package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.client.BookshelfInspectorCompat;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.lukasabbe.inspector.Inspector", remap = false)
public abstract class BookshelfInspectorMixin {
    @Inject(method = "inspect", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$inspectMovingBookshelf(Minecraft client, CallbackInfo ci) {
        if (BookshelfInspectorCompat.inspect(client)) ci.cancel();
    }
}
