package com.nitricacid.traininteractive.client;

import com.nitricacid.traininteractive.TrainPickedItem;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class TrainPickBlock {
    private TrainPickBlock() {}
    public static boolean pick(Minecraft client) {
        if (client.level == null || client.player == null || client.gameMode == null || client.getCameraEntity() == null) return false;
        var camera = client.getCameraEntity();
        var origin = camera.getEyePosition(1);
        double reach = client.player.blockInteractionRange();
        var stationary = camera.pick(reach, 1, false);
        double nearest = stationary.getType() == HitResult.Type.MISS ? reach : stationary.getLocation().distanceTo(origin);
        var end = origin.add(camera.getViewVector(1).scale(nearest));
        var bounds = new AABB(origin, end).inflate(16);
        AbstractContraptionEntity target = null;
        BlockHitResult best = null;
        for (var reference : ContraptionHandler.loadedContraptions.get(client.level).values()) {
            var entity = reference.get();
            if (entity == null || !entity.isAlive() || entity.getContraption() == null || !entity.getBoundingBox().intersects(bounds)) continue;
            var hit = ContraptionHandlerClient.rayTraceContraption(origin, end, entity);
            if (hit == null) continue;
            double distance = entity.toGlobalVector(hit.getLocation(), 1).distanceTo(origin);
            if (distance >= nearest) continue;
            nearest = distance; target = entity; best = hit;
        }
        if (best == null) return false;
        boolean creative = client.player.getAbilities().instabuild;
        var stack = TrainPickedItem.create(target, best, client.player, creative && Screen.hasControlDown());
        if (stack.isEmpty()) return true;
        var inventory = client.player.getInventory();
        if (creative) {
            inventory.setPickedItem(stack);
            client.gameMode.handleCreativeModeItemAdd(client.player.getMainHandItem(), 36 + inventory.selected);
        } else {
            int slot = inventory.findSlotMatchingItem(stack);
            if (slot >= 0) {
                if (Inventory.isHotbarSlot(slot)) inventory.selected = slot;
                else client.gameMode.handlePickItem(slot);
            }
        }
        return true;
    }
}
