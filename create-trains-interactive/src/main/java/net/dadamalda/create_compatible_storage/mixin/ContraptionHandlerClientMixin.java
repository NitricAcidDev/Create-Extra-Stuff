package net.dadamalda.create_compatible_storage.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.dadamalda.create_compatible_storage.foundation.PreciseContraptionInteractionPacket;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContraptionHandlerClient.class)
public class ContraptionHandlerClientMixin {
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "rightClickingOnContraptionsGetsHandledLocally",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/ContraptionHandlerClient;handleSpecialInteractions(Lcom/simibubi/create/content/contraptions/AbstractContraptionEntity;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/InteractionHand;)Z"), remap = false)
    private static boolean trainsInteractive$useHandAfterMiss(com.simibubi.create.content.contraptions.AbstractContraptionEntity entity,
            net.minecraft.world.entity.player.Player player, net.minecraft.core.BlockPos pos, net.minecraft.core.Direction face,
            net.minecraft.world.InteractionHand hand, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        if (original.call(entity, player, pos, face, hand)) return true;
        var client = net.minecraft.client.Minecraft.getInstance();
        return client.gameMode != null && !player.getItemInHand(hand).isEmpty()
                && client.gameMode.useItem(client.player, hand).consumesAction();
    }
    @Inject(method = "rayTraceContraption", at = @At("HEAD"), cancellable = true, remap = false)
    private static void trainsInteractive$waitForSync(Vec3 origin, Vec3 target,
            com.simibubi.create.content.contraptions.AbstractContraptionEntity entity,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<BlockHitResult> cir) {
        if (entity.getContraption() == null) cir.setReturnValue(null);
    }
    @Inject(method = "rightClickingOnContraptionsGetsHandledLocally",
            at = @At(value = "INVOKE",
                    target = "Lnet/createmod/catnip/platform/services/NetworkHelper;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V",
                    shift = At.Shift.BEFORE),
            remap = false
    )
    private static void rightClickingOnContraptionHandledLocally(
            net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event, CallbackInfo ci, @Local(ordinal = 0) BlockHitResult raycastHit)
    {
        Vec3 location = raycastHit.getLocation();

        PacketDistributor.sendToServer(
                new PreciseContraptionInteractionPacket(
                        location.x, location.y, location.z
                )
        );
    }
}
