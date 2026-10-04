package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.TrainStructureSpawn;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public abstract class ContraptionSpawnBundleMixin {
    @Shadow @Final private Entity entity;

    @Inject(method = "sendPairingData", at = @At("TAIL"))
    private void trainsInteractive$includeStructure(ServerPlayer player, PacketAndPayloadAcceptor<ClientGamePacketListener> packets, CallbackInfo ci) {
        // Keep add-entity, native metadata and every structure part in the same
        // ordered spawn bundle, before the client can tick or render the entity.
        if (entity instanceof AbstractContraptionEntity contraption)
            TrainStructureSpawn.send(contraption, true, packets::accept);
    }
}
