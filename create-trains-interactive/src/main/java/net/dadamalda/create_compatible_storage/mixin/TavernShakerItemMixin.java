package net.dadamalda.create_compatible_storage.mixin;

import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem", remap = false)
public abstract class TavernShakerItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$keepFilledShaker(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        var stack = context.getItemInHand();
        // Consume a failed pour so neither vanilla nor a train placement handler places it.
        if (ShakerItem.hasResult(stack) && !context.getLevel().getBlockState(context.getClickedPos()).is(ModBlocks.EMPTY_GLASSWARE.get()))
            cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
