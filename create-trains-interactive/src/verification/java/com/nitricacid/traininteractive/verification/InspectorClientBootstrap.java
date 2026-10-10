package com.nitricacid.traininteractive.verification;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "create_trains_interactive", value = Dist.CLIENT)
public final class InspectorClientBootstrap {
    private static boolean finished;

    @SubscribeEvent
    public static void verify(ClientTickEvent.Post event) throws Exception {
        if (finished || !Boolean.getBoolean("create_trains_interactive.verifyInspector")) return;
        finished = true;
        Class.forName("vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu");
        if (ModList.get().isLoaded("exposure")) {
            Class.forName("io.github.mortuusars.exposure.world.inventory.LightroomMenu");
            Class.forName("io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity");
        }
        com.nitricacid.traininteractive.client.TrainHarmonicsControls.behaviour(null);
        if (com.nitricacid.traininteractive.client.TrainPickBlock.pick(Minecraft.getInstance()))
            throw new AssertionError("Pick Block must safely ignore a client without a world");
        if (ModList.get().isLoaded("bookshelfinspector")) {
            InspectorClientChecks.check(event);
        } else {
            org.slf4j.LoggerFactory.getLogger(InspectorClientBootstrap.class).info("Optional Bookshelf Inspector absence check passed");
            if (!Boolean.getBoolean("create_trains_interactive.verifyGameplay")) Minecraft.getInstance().stop();
        }
    }
}
