package com.nitricacid.traininteractive.client;

import com.nitricacid.traininteractive.TrainHandUseFallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;

public final class TrainHandUseClient {
    private TrainHandUseClient() {}
    public static void receive(TrainHandUseFallback packet) {
        var client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null || client.player.isSpectator()) return;
        var held = client.player.getItemInHand(packet.hand());
        if (held.isEmpty() || !BuiltInRegistries.ITEM.getKey(held.getItem()).equals(packet.item())) return;
        var result = client.gameMode.useItem(client.player, packet.hand());
        if (result.shouldSwing()) client.player.swing(packet.hand());
        if (result.consumesAction()) client.gameRenderer.itemInHandRenderer.itemUsed(packet.hand());
    }
}
