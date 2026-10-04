package net.dadamalda.create_compatible_storage.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nitricacid.traininteractive.TrainHandUse;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ContraptionInteractionPacket.class, remap = false)
public abstract class ContraptionHandUseMixin {
    @WrapOperation(method = "handle", at = @At(value = "INVOKE", target =
            "Lcom/simibubi/create/content/contraptions/AbstractContraptionEntity;handlePlayerInteraction(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/InteractionHand;)Z"))
    private boolean trainsInteractive$useHeldItem(AbstractContraptionEntity entity, Player player, BlockPos pos,
            Direction face, InteractionHand hand, Operation<Boolean> original) {
        return TrainHandUse.afterInteraction(original.call(entity, player, pos, face, hand), (ServerPlayer) player, entity, pos, hand);
    }
}
