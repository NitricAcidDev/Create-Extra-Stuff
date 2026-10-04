package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingServiceMenus;
import com.nitricacid.traininteractive.MovingTrainWorld;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class NativeTrainPlayerMixin {
    @ModifyVariable(method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At("HEAD"), argsOnly = true)
    private Consumer<RegistryFriendlyByteBuf> trainsInteractive$menuData(Consumer<RegistryFriendlyByteBuf> original) {
        var world = MovingTrainWorld.current();
        if (world == null || original == null) return original;
        return buf -> {
            original.accept(buf);
            buf.writeInt(MovingServiceMenus.MAGIC);
            buf.writeVarInt(world.entity.getId());
        };
    }
    @Inject(method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At("RETURN"))
    private void trainsInteractive$opened(MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extra,
            CallbackInfoReturnable<OptionalInt> cir) {
        var world = MovingTrainWorld.current();
        if (world != null && cir.getReturnValue().isPresent()) {
            world.flush(true);
            MovingServiceMenus.opened((ServerPlayer) (Object) this, world);
        }
    }
    @Inject(method = "doCloseContainer", at = @At("HEAD"))
    private void trainsInteractive$closed(CallbackInfo ci) { MovingServiceMenus.closed((ServerPlayer) (Object) this); }

    @Redirect(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean trainsInteractive$playerDrop(Level level, Entity entity) {
        // Player drops are already in world coordinates, unlike native block drops.
        return MovingTrainWorld.outside(() -> level.addFreshEntity(entity));
    }
}
