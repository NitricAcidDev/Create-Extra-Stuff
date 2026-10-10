package com.nitricacid.traininteractive.verification;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = "create_trains_interactive", bus = EventBusSubscriber.Bus.MOD)
public final class OptionalServiceTestBootstrap {
    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        if (ModList.get().isLoaded("exposure") && ModList.get().isLoaded("createharmonics"))
            event.register(TrainMusicPhotoTests.class);
    }
}
