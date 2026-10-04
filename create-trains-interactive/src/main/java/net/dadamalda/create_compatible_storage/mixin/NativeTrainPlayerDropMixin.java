package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingTrainWorld;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class NativeTrainPlayerDropMixin {
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$worldDrop(ItemStack stack, boolean randomMotion, CallbackInfoReturnable<ItemEntity> cir) {
        var player = (Player) (Object) this;
        if (MovingTrainWorld.active(player.level()) != null)
            cir.setReturnValue(MovingTrainWorld.outside(() -> player.drop(stack, randomMotion)));
    }
}
