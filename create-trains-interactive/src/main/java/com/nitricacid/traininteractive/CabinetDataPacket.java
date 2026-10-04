package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.dadamalda.create_compatible_storage.mixin.ContraptionUpdateTagsAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CabinetDataPacket(int entityId, BlockPos pos, CompoundTag data) implements CustomPacketPayload {
    public static final Type<CabinetDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("create_trains_interactive", "cabinet_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CabinetDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CabinetDataPacket::entityId, BlockPos.STREAM_CODEC, CabinetDataPacket::pos,
            ByteBufCodecs.COMPOUND_TAG, CabinetDataPacket::data, CabinetDataPacket::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void apply(AbstractContraptionEntity entity, BlockPos pos, CompoundTag data) {
        var contraption = entity.getContraption();
        var info = contraption.getBlocks().get(pos);
        if (info == null) return;
        var updated = new StructureBlockInfo(pos, info.state(), data.copy());
        contraption.getBlocks().put(pos, updated);
        ((ContraptionUpdateTagsAccess) contraption).trainsInteractive$updateTags().put(pos, data.copy());
        for (var actor : contraption.getActors()) {
            if (!actor.getLeft().pos().equals(pos)) continue;
            actor.setLeft(updated);
            if (actor.getRight() != null) actor.getRight().blockEntityData = data.copy();
        }
    }

    public static void handle(CabinetDataPacket packet, IPayloadContext context) {
        if (!(context.player().level().getEntity(packet.entityId) instanceof AbstractContraptionEntity entity)) return;
        if (entity.getContraption() == null) return;
        apply(entity, packet.pos, packet.data);
        var be = entity.getContraption().getBlockEntityClientSide(packet.pos);
        if (be != null) be.loadWithComponents(packet.data, entity.level().registryAccess());
    }
}
