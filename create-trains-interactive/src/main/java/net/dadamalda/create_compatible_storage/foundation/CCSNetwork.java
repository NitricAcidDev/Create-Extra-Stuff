package net.dadamalda.create_compatible_storage.foundation;

import net.dadamalda.create_compatible_storage.Create_compatible_storage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Create_compatible_storage.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CCSNetwork {
    private static final String PROTOCOL = "4";

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToClient(com.nitricacid.traininteractive.TrainHandUseFallback.TYPE,
            com.nitricacid.traininteractive.TrainHandUseFallback.STREAM_CODEC, com.nitricacid.traininteractive.TrainHandUseFallback::handle);
        registrar.playToServer(com.nitricacid.traininteractive.ContraptionResyncRequest.TYPE,
            com.nitricacid.traininteractive.ContraptionResyncRequest.STREAM_CODEC, com.nitricacid.traininteractive.ContraptionResyncRequest::handle);
        registrar.playToClient(com.nitricacid.traininteractive.ContraptionStructureChunk.TYPE,
            com.nitricacid.traininteractive.ContraptionStructureChunk.STREAM_CODEC, com.nitricacid.traininteractive.ContraptionStructureChunk::handle);
        registrar.playToClient(com.nitricacid.traininteractive.TrainBlockDataPacket.TYPE,
            com.nitricacid.traininteractive.TrainBlockDataPacket.STREAM_CODEC, com.nitricacid.traininteractive.TrainBlockDataPacket::handle);
        registrar.playToClient(com.nitricacid.traininteractive.CabinetDataPacket.TYPE,
            com.nitricacid.traininteractive.CabinetDataPacket.STREAM_CODEC, com.nitricacid.traininteractive.CabinetDataPacket::handle);
        registrar.playToServer(
            PreciseContraptionInteractionPacket.TYPE,
                PreciseContraptionInteractionPacket.STREAM_CODEC,
                PreciseContraptionInteractionStore::handle
        );
    }
}
