package com.nitricacid.traininteractive.client;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class TrainRayTrace {
    private TrainRayTrace() {}
    public record Target(AbstractContraptionEntity entity, BlockHitResult hit) {}
    public static Target find(Minecraft client) {
        if (client.level == null || client.player == null || client.getCameraEntity() == null) return null;
        var camera = client.getCameraEntity();
        var origin = camera.getEyePosition(1);
        double nearest = client.player.blockInteractionRange();
        var stationary = camera.pick(nearest, 1, false);
        if (stationary.getType() != HitResult.Type.MISS) nearest = stationary.getLocation().distanceTo(origin);
        var end = origin.add(camera.getViewVector(1).scale(nearest));
        var bounds = new AABB(origin, end).inflate(16);
        Target best = null;
        for (var reference : ContraptionHandler.loadedContraptions.get(client.level).values()) {
            var entity = reference.get();
            if (entity == null || !entity.isAlive() || entity.getContraption() == null || !entity.getBoundingBox().intersects(bounds)) continue;
            var hit = ContraptionHandlerClient.rayTraceContraption(origin, end, entity);
            if (hit == null) continue;
            double distance = entity.toGlobalVector(hit.getLocation(), 1).distanceTo(origin);
            if (distance >= nearest) continue;
            nearest = distance;
            best = new Target(entity, hit);
        }
        return best;
    }
}
