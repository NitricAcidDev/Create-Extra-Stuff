package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingServiceMenus;
import com.nitricacid.traininteractive.MovingTrainWorld;
import com.nitricacid.traininteractive.TrainWorldAccess;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.mortuusars.exposure.world.inventory.LightroomMenu", remap = false)
public abstract class MovingLightroomMenuMixin {
    @Inject(method = "getBlockEntity(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/FriendlyByteBuf;)Lio/github/mortuusars/exposure/world/block/entity/LightroomBlockEntity;", at = @At("HEAD"), cancellable = true)
    private static void trainsInteractive$movingLightroom(Inventory inventory, FriendlyByteBuf data,
            CallbackInfoReturnable<LightroomBlockEntity> cir) {
        if (data.readableBytes() < 13 || data.getInt(data.readerIndex() + Long.BYTES) != MovingServiceMenus.MAGIC) return;
        var pos = data.readBlockPos(); data.readInt();
        if (!(inventory.player.level().getEntity(data.readVarInt()) instanceof AbstractContraptionEntity train)
                || train.getContraption() == null) throw new IllegalStateException("The moving Lightroom's train is no longer present");
        var info = train.getContraption().getBlocks().get(pos);
        if (info == null) throw new IllegalStateException("The moving Lightroom is no longer present");
        var be = new LightroomBlockEntity(pos, info.state());
        if (info.nbt() != null) be.loadWithComponents(info.nbt(), inventory.player.level().registryAccess());
        be.setLevel(train.getContraption().getContraptionWorld());
        MovingTrainWorld.bind(be, ((TrainWorldAccess) train).trainsInteractive$world());
        cir.setReturnValue(be);
    }
    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$valid(Player player, CallbackInfoReturnable<Boolean> cir) {
        var valid = MovingServiceMenus.valid(player);
        if (valid != null) cir.setReturnValue(valid);
    }
}
