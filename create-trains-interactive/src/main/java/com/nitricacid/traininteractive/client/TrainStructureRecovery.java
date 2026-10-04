package com.nitricacid.traininteractive.client;

import com.nitricacid.traininteractive.ContraptionResyncRequest;
import com.nitricacid.traininteractive.ContraptionStructureChunk;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import io.netty.buffer.Unpooled;
import net.dadamalda.create_compatible_storage.mixin.ContraptionSpawnAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "create_trains_interactive", value = Dist.CLIENT)
public final class TrainStructureRecovery {
    private record Pending(UUID transfer, byte[][] parts, long started) {}
    private static final Map<Integer, Pending> transfers = new HashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel level;
    private static long ticks;
    private TrainStructureRecovery() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (level != client.level) { level = client.level; transfers.clear(); ticks = 0; }
        if (level == null) return;
        long now = ++ticks;
        transfers.values().removeIf(p -> now - p.started > 200);
        for (var ref : ContraptionHandler.loadedContraptions.get(level).values()) {
            var entity = ref.get();
            if (entity != null && entity.isAlive() && entity.getContraption() == null && (now + entity.getId()) % 100 == 20)
                PacketDistributor.sendToServer(new ContraptionResyncRequest(entity.getId()));
        }
    }

    public static void receive(ContraptionStructureChunk packet) {
        var client = Minecraft.getInstance();
        if (client.level == null || !(client.level.getEntity(packet.entityId()) instanceof AbstractContraptionEntity entity)
                || !entity.getUUID().equals(packet.entityUuid()) || entity.getContraption() != null) return;
        if (packet.count() < 1 || packet.count() > ContraptionStructureChunk.MAX_BYTES / ContraptionStructureChunk.CHUNK_BYTES
                || packet.index() < 0 || packet.index() >= packet.count()) return;
        if (level != client.level) { level = client.level; transfers.clear(); ticks = 0; }
        var pending = transfers.get(packet.entityId());
        if (pending == null || !pending.transfer.equals(packet.transfer())) {
            pending = new Pending(packet.transfer(), new byte[packet.count()][], ticks);
            transfers.put(packet.entityId(), pending);
        }
        if (pending.parts.length != packet.count()) return;
        pending.parts[packet.index()] = packet.data();
        int length = 0;
        for (var part : pending.parts) { if (part == null) return; length += part.length; }
        byte[] all = new byte[length];
        int offset = 0;
        for (var part : pending.parts) { System.arraycopy(part, 0, all, offset, part.length); offset += part.length; }
        transfers.remove(packet.entityId());
        var buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(all));
        try {
            var tag = buffer.readNbt(NbtAccounter.create(64L * 1024 * 1024));
            if (tag instanceof net.minecraft.nbt.CompoundTag compound) {
                ((ContraptionSpawnAccess) entity).trainsInteractive$readSpawn(compound, true);
                entity.setPos(entity.getX(), entity.getY(), entity.getZ());
                org.slf4j.LoggerFactory.getLogger(TrainStructureRecovery.class).info("Recovered carriage structure {} in {} chunks", entity.getId(), packet.count());
            }
        } finally { buffer.release(); }
    }
}
