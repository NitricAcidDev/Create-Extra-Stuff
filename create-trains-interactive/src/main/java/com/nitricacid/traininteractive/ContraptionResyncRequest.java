package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ContraptionResyncRequest(int entityId) implements CustomPacketPayload {
    public static final Type<ContraptionResyncRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("create_trains_interactive", "request_structure"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionResyncRequest> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ContraptionResyncRequest::entityId, ContraptionResyncRequest::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ContraptionResyncRequest packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.level().getEntity(packet.entityId) instanceof AbstractContraptionEntity entity)
                || entity.getContraption() == null || !entity.getBoundingBox().inflate(128).contains(player.position())) return;
        var key = "TrainStructureRequest:" + player.getUUID();
        long now = entity.level().getGameTime();
        var throttle = entity.getPersistentData();
        if (throttle.contains(key) && now - throttle.getLong(key) < 100) return;
        throttle.putLong(key, now);
        TrainStructureSpawn.send(entity, false, payload -> PacketDistributor.sendToPlayer(player, payload));
    }
}
