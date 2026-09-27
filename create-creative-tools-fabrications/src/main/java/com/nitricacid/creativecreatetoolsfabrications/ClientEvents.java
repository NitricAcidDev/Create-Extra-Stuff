package com.nitricacid.creativecreatetoolsfabrications;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "creativecreatetoolsfabrications", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientEvents {
    private ClientEvents() {}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) { CreativeCreateTools.clientTick(); }
}
