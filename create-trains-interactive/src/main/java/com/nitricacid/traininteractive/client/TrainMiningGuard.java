package com.nitricacid.traininteractive.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = "create_trains_interactive", value = Dist.CLIENT)
public final class TrainMiningGuard {
    private static int entityId = -1;
    private static BlockPos pos;
    private TrainMiningGuard() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || !ModList.get().isLoaded("createonthemove")) return;
        if (pos != null) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        var target = TrainRayTrace.find(Minecraft.getInstance());
        if (target != null) {
            entityId = target.entity().getId();
            pos = target.hit().getBlockPos();
        }
    }

    public static boolean allows(int candidateEntity, BlockPos candidatePos) {
        return pos == null || entityId == candidateEntity && pos.equals(candidatePos);
    }

    @SubscribeEvent
    public static void released(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (client.level == null || client.screen != null || !client.options.keyAttack.isDown()) {
            entityId = -1;
            pos = null;
        }
    }
}
