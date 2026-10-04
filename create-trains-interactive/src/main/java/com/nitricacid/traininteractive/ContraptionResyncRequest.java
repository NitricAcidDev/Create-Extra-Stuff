package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import io.netty.buffer.Unpooled;
import net.dadamalda.create_compatible_storage.mixin.ContraptionSpawnAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
        var tag = new CompoundTag();
        ((ContraptionSpawnAccess) entity).trainsInteractive$writeSpawn(tag, player.registryAccess(), true);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeNbt(tag);
            int length = buffer.readableBytes();
            if (length > ContraptionStructureChunk.MAX_BYTES) return;
            byte[] data = new byte[length];
            buffer.readBytes(data);
            var transfer = java.util.UUID.randomUUID();
            int total = (data.length + ContraptionStructureChunk.CHUNK_BYTES - 1) / ContraptionStructureChunk.CHUNK_BYTES;
            for (int i = 0; i < total; i++) {
                int from = i * ContraptionStructureChunk.CHUNK_BYTES;
                PacketDistributor.sendToPlayer(player, new ContraptionStructureChunk(entity.getId(), entity.getUUID(), transfer, i, total,
                        java.util.Arrays.copyOfRange(data, from, Math.min(data.length, from + ContraptionStructureChunk.CHUNK_BYTES))));
            }
        } finally { buffer.release(); }
    }
}
