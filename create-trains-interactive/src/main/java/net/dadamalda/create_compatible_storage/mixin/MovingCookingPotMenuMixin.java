package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingServiceMenus;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu", remap = false)
public abstract class MovingCookingPotMenuMixin {
    @Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
    private static void trainsInteractive$movingPot(Inventory inventory, FriendlyByteBuf data,
            CallbackInfoReturnable<CookingPotBlockEntity> cir) {
        if (data.readableBytes() < 13 || data.getInt(data.readerIndex() + Long.BYTES) != MovingServiceMenus.MAGIC) return;
        var pos = data.readBlockPos();
        data.readInt();
        var entity = inventory.player.level().getEntity(data.readVarInt());
        if (!(entity instanceof AbstractContraptionEntity train) || train.getContraption() == null)
            throw new IllegalStateException("The moving cooking pot's train is no longer present");
        var info = train.getContraption().getBlocks().get(pos);
        if (info == null) throw new IllegalStateException("The moving cooking pot is no longer present");
        var pot = new CookingPotBlockEntity(pos, info.state());
        if (info.nbt() != null) pot.loadWithComponents(info.nbt(), inventory.player.level().registryAccess());
        pot.setLevel(train.getContraption().getContraptionWorld());
        cir.setReturnValue(pot);
    }
}
