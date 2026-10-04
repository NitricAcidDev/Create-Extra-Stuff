package com.nitricacid.traininteractive;

import com.simibubi.create.api.registry.SimpleRegistry;
import net.dadamalda.create_compatible_storage.mixin.SimpleRegistryAccess;

public final class OnTheMoveCompat {
    private OnTheMoveCompat() {}

    public static <K, V> void registerIfUnclaimed(SimpleRegistry<K, V> registry, K key, V value) {
        synchronized (registry) {
            // get() also resolves On the Move's universal provider. Check explicit
            // registrations instead, so its specialized handlers still get registered.
            if (!((SimpleRegistryAccess) registry).trainsInteractive$getRegistrations().containsKey(key))
                registry.register(key, value);
        }
    }
}
