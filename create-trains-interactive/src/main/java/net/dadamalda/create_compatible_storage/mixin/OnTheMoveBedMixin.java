package net.dadamalda.create_compatible_storage.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nitricacid.traininteractive.MovingBedSleep;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.woudlee.createonthemove.interaction.BedInteractionBehaviour", remap = false)
public abstract class OnTheMoveBedMixin {
    @WrapMethod(method = "handlePlayerInteraction")
    private boolean trainsInteractive$checkBeforeSleeping(Player player, InteractionHand hand, BlockPos pos,
            AbstractContraptionEntity entity, Operation<Boolean> original) {
        if (!(player instanceof ServerPlayer serverPlayer)) return original.call(player, hand, pos, entity);
        return MovingBedSleep.run(serverPlayer, entity, pos, () -> original.call(player, hand, pos, entity));
    }
}
