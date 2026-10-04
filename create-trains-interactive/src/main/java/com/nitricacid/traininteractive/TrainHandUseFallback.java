package com.nitricacid.traininteractive;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrainHandUseFallback(InteractionHand hand, ResourceLocation item) implements CustomPacketPayload {
    public static final Type<TrainHandUseFallback> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("create_trains_interactive", "use_held_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainHandUseFallback> STREAM_CODEC = new StreamCodec<>() {
        public void encode(RegistryFriendlyByteBuf buf, TrainHandUseFallback packet) { buf.writeEnum(packet.hand); buf.writeResourceLocation(packet.item); }
        public TrainHandUseFallback decode(RegistryFriendlyByteBuf buf) { return new TrainHandUseFallback(buf.readEnum(InteractionHand.class), buf.readResourceLocation()); }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(TrainHandUseFallback packet, IPayloadContext context) {
        com.nitricacid.traininteractive.client.TrainHandUseClient.receive(packet);
    }
}
