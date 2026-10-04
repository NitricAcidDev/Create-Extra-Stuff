package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.OnTheMoveCompat;
import com.simibubi.create.api.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.woudlee.createonthemove.registry.ModInteractions", remap = false)
public abstract class OnTheMoveInteractionsMixin {
    @Redirect(method = "register", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/api/registry/SimpleRegistry;register(Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static void trainsInteractive$preserveExistingHandler(SimpleRegistry<Object, Object> registry,
                                                                 Object key, Object value) {
        OnTheMoveCompat.registerIfUnclaimed(registry, key, value);
    }
}
