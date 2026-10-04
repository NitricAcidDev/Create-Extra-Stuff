package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.data.ContraptionSyncLimiting;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;
import net.dadamalda.create_compatible_storage.mixin.ContraptionSpawnAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class TrainStructureSpawn {
    private TrainStructureSpawn() {}

    public static void send(AbstractContraptionEntity entity, boolean oversizedOnly, Consumer<CustomPacketPayload> send) {
        if (entity.getContraption() == null) return;
        var tag = new CompoundTag();
        ((ContraptionSpawnAccess) entity).trainsInteractive$writeSpawn(tag, entity.registryAccess(), true);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeNbt(tag);
            int length = buffer.readableBytes();
            if ((oversizedOnly && length <= ContraptionSyncLimiting.LIMIT) || length > ContraptionStructureChunk.MAX_BYTES) return;
            byte[] data = new byte[length];
            buffer.readBytes(data);
            var transfer = UUID.randomUUID();
            int count = (length + ContraptionStructureChunk.CHUNK_BYTES - 1) / ContraptionStructureChunk.CHUNK_BYTES;
            for (int i = 0; i < count; i++) {
                int from = i * ContraptionStructureChunk.CHUNK_BYTES;
                send.accept(new ContraptionStructureChunk(entity.getId(), entity.getUUID(), transfer, i, count,
                        Arrays.copyOfRange(data, from, Math.min(length, from + ContraptionStructureChunk.CHUNK_BYTES))));
            }
        } finally { buffer.release(); }
    }
}
