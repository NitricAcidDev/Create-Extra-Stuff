package net.dadamalda.create_compatible_storage.mixin;

import com.simibubi.create.impl.registry.SimpleRegistryImpl;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SimpleRegistryImpl.class, remap = false)
public interface SimpleRegistryAccess {
    @Accessor("registrations")
    Map<?, ?> trainsInteractive$getRegistrations();
}
