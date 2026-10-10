package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import me.mochibit.createharmonics.content.kinetics.recordPlayer.RecordPlayerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class HarmonicsControls {
    private HarmonicsControls() {}
    public static ScrollOptionBehaviour<?> behaviour(MovingTrainWorld world, BlockPos pos) {
        if (!ModList.get().isLoaded("createharmonics")) return null;
        return world.blockEntity(pos) instanceof RecordPlayerBlockEntity player ? player.getPlaybackMode() : null;
    }
    public static boolean configure(Player player, AbstractContraptionEntity entity, BlockPos pos, int mode) {
        if (!ModList.get().isLoaded("createharmonics") || player.isSpectator() || !player.mayBuild()
                || !entity.isAlive() || entity.level() != player.level() || entity.getContraption() == null
                || !entity.canInteractWithBlock(player, pos, player.blockInteractionRange() + 1)) return false;
        var world = ((TrainWorldAccess) entity).trainsInteractive$world();
        var behaviour = behaviour(world, pos);
        if (behaviour == null || mode < 0 || mode >= behaviour.get().getDeclaringClass().getEnumConstants().length) return false;
        var info = world.contraption.getBlocks().get(pos);
        var tag = info.nbt() == null ? new net.minecraft.nbt.CompoundTag() : info.nbt().copy();
        // Change only the native setting, preserving the actor's disc and playback clock.
        tag.putInt("ScrollValue", mode);
        world.updateData(pos, tag);
        world.flush(true);
        return true;
    }
}
