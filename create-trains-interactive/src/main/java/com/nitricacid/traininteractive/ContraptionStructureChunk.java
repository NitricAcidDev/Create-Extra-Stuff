package com.nitricacid.traininteractive;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.UUID;

public record ContraptionStructureChunk(int entityId, UUID entityUuid, UUID transfer, int index, int count, byte[] data) implements CustomPacketPayload {
    public static final int CHUNK_BYTES = 256 * 1024;
    public static final int MAX_BYTES = 16 * 1024 * 1024;
    public static final Type<ContraptionStructureChunk> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("create_trains_interactive", "structure_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionStructureChunk> STREAM_CODEC = new StreamCodec<>() {
        public void encode(RegistryFriendlyByteBuf buf, ContraptionStructureChunk packet) {
            buf.writeVarInt(packet.entityId); buf.writeUUID(packet.entityUuid); buf.writeUUID(packet.transfer);
            buf.writeVarInt(packet.index); buf.writeVarInt(packet.count); buf.writeByteArray(packet.data);
        }
        public ContraptionStructureChunk decode(RegistryFriendlyByteBuf buf) {
            return new ContraptionStructureChunk(buf.readVarInt(), buf.readUUID(), buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readByteArray(CHUNK_BYTES));
        }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(ContraptionStructureChunk packet, IPayloadContext context) {
        com.nitricacid.traininteractive.client.TrainStructureRecovery.receive(packet);
    }
}
