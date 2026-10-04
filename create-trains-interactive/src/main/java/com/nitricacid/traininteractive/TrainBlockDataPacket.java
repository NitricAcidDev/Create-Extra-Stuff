package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.dadamalda.create_compatible_storage.mixin.ContraptionUpdateTagsAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrainBlockDataPacket(int entityId, BlockPos pos, BlockState state, CompoundTag data) implements CustomPacketPayload {
    public static final Type<TrainBlockDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("create_trains_interactive", "train_block_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainBlockDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TrainBlockDataPacket::entityId, BlockPos.STREAM_CODEC, TrainBlockDataPacket::pos,
            CatnipStreamCodecs.BLOCK_STATE, TrainBlockDataPacket::state,
            ByteBufCodecs.COMPOUND_TAG, TrainBlockDataPacket::data, TrainBlockDataPacket::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void apply(AbstractContraptionEntity entity, BlockPos pos, BlockState state, CompoundTag data) {
        var c = entity.getContraption();
        var old = c.getBlocks().get(pos);
        c.getBlocks().put(pos, new StructureBlockInfo(pos, state, state.hasBlockEntity() ? data.copy() : null));
        var handler = com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.REGISTRY.get(state);
        if (handler == null || state.isAir()) c.getInteractors().remove(pos); else c.getInteractors().put(pos, handler);
        if (c.bounds != null) c.bounds = c.bounds.minmax(new net.minecraft.world.phys.AABB(pos));
        var tags = ((ContraptionUpdateTagsAccess) c).trainsInteractive$updateTags();
        if (state.hasBlockEntity()) tags.put(pos, data.copy()); else tags.remove(pos);
        if (old == null || old.state() != state) {
            c.resetClientContraption();
            c.invalidateColliders();
        } else {
            var be = c.getBlockEntityClientSide(pos);
            if (be != null) be.loadWithComponents(data, entity.level().registryAccess());
        }
    }

    public static void handle(TrainBlockDataPacket packet, IPayloadContext context) {
        if (context.player().level().getEntity(packet.entityId) instanceof AbstractContraptionEntity entity
                && entity.getContraption() != null)
            apply(entity, packet.pos, packet.state, packet.data);
    }
}
