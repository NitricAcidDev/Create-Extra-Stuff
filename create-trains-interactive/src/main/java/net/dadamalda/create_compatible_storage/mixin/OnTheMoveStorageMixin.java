package net.dadamalda.create_compatible_storage.mixin;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.woudlee.createonthemove.interaction.universal.UniversalInteractionProvider", remap = false)
public abstract class OnTheMoveStorageMixin {
    @Inject(method = "get(Lnet/minecraft/world/level/block/Block;)Lcom/simibubi/create/api/behaviour/interaction/MovingInteractionBehaviour;",
            at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$preserveMountedStorage(Block block,
            CallbackInfoReturnable<MovingInteractionBehaviour> cir) {
        // Create opens menus against the mounted inventory. A generic native menu
        // instead points at the stationary block and closes as the train moves.
        var type = MountedItemStorageType.REGISTRY.get(block);
        // Create's fallback type is returned even for blocks with no inventory.
        if (type != null && type != com.simibubi.create.AllMountedStorageTypes.FALLBACK.get()) cir.setReturnValue(null);
    }
}
