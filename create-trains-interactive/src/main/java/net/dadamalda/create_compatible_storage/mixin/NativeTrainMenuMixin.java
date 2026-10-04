package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingServiceMenus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class NativeTrainMenuMixin {
    @Inject(method = "stillValid(Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/Block;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void trainsInteractive$valid(ContainerLevelAccess access, Player player, Block block,
            CallbackInfoReturnable<Boolean> cir) {
        var valid = MovingServiceMenus.valid(player);
        if (valid != null) cir.setReturnValue(valid);
    }
}
