package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingTrainPlacement;
import com.nitricacid.traininteractive.NativeServiceInteraction;
import com.nitricacid.traininteractive.TrainEditingConfig;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.woudlee.createonthemove.contraption.ContraptionBlockPlacer", remap = false)
public abstract class OnTheMovePlacementMixin {
    @Inject(method = "tryPlaceBlock", at = @At("HEAD"), cancellable = true)
    private static void trainsInteractive$serverWhitelist(Player player, AbstractContraptionEntity entity, BlockPos pos,
            Direction side, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        if (player == null || entity == null || pos == null || side == null || hand == null
                || !(player.getItemInHand(hand).getItem() instanceof BlockItem item)) return;
        if (!player.mayBuild() || player.isSpectator()) { cir.setReturnValue(false); return; }
        if (!TrainEditingConfig.allows(item.getBlock())) {
            TrainEditingConfig.rejected(player);
            cir.setReturnValue(false);
        } else if (!player.level().isClientSide && NativeServiceInteraction.canPlace(item.getBlock())) {
            // Native placement preserves filled shakers, drinks and prepared food instead of using an empty default state.
            cir.setReturnValue(MovingTrainPlacement.place(player, entity, pos, side, hand));
        }
    }
}
