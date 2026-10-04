package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.TrainEditingConfig;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.woudlee.createonthemove.contraption.ContraptionBlockBreaker", remap = false)
public abstract class OnTheMoveBreakingMixin {
    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private static void trainsInteractive$serverWhitelist(ServerPlayer player, int entityId, BlockPos pos, CallbackInfo ci) {
        if (player != null && (!player.mayBuild() || player.isSpectator())) { ci.cancel(); return; }
        if (player == null || pos == null || !(player.level().getEntity(entityId) instanceof AbstractContraptionEntity entity)
                || entity.getContraption() == null) return;
        var info = entity.getContraption().getBlocks().get(pos);
        if (info != null && !TrainEditingConfig.allows(info.state().getBlock())) {
            TrainEditingConfig.rejected(player);
            ci.cancel();
        }
    }
}
