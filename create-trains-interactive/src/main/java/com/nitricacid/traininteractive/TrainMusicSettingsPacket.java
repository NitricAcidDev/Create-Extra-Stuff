package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrainMusicSettingsPacket(int entityId, BlockPos pos, int mode) implements CustomPacketPayload {
    public static final Type<TrainMusicSettingsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("create_trains_interactive", "music_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainMusicSettingsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TrainMusicSettingsPacket::entityId, BlockPos.STREAM_CODEC, TrainMusicSettingsPacket::pos,
            ByteBufCodecs.VAR_INT, TrainMusicSettingsPacket::mode, TrainMusicSettingsPacket::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(TrainMusicSettingsPacket packet, IPayloadContext context) {
        if (context.player().level().getEntity(packet.entityId) instanceof AbstractContraptionEntity entity)
            HarmonicsControls.configure(context.player(), entity, packet.pos, packet.mode);
    }
}
