package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public final class TrainHandUse {
    private TrainHandUse() {}

    public static boolean afterInteraction(boolean handled, ServerPlayer player, AbstractContraptionEntity entity,
            BlockPos pos, InteractionHand hand) {
        if (handled || hand == null || player.isSpectator() || entity.getContraption() == null
                || !entity.getContraption().getBlocks().containsKey(pos)
                || !entity.canInteractWithBlock(player, pos, player.blockInteractionRange() + 1)) return handled;
        var held = player.getItemInHand(hand);
        if (held.isEmpty()) return false;
        // Ask the client to use Minecraft's normal item-use path. Some items,
        // including writable books, open their UI only on the client.
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new TrainHandUseFallback(hand, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem())));
        return false;
    }
}
