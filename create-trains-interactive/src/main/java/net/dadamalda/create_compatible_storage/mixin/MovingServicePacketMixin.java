package net.dadamalda.create_compatible_storage.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nitricacid.traininteractive.MovingServiceMenus;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MovingServicePacketMixin {
    @Shadow public ServerPlayer player;
    @WrapMethod(method = "handleContainerClick")
    private void trainsInteractive$click(ServerboundContainerClickPacket packet, Operation<Void> original) {
        if (!player.server.isSameThread()) { original.call(packet); return; }
        MovingServiceMenus.run(player, () -> { original.call(packet); return null; });
    }
    @WrapMethod(method = "handleContainerButtonClick")
    private void trainsInteractive$button(ServerboundContainerButtonClickPacket packet, Operation<Void> original) {
        if (!player.server.isSameThread()) { original.call(packet); return; }
        MovingServiceMenus.run(player, () -> { original.call(packet); return null; });
    }
}
