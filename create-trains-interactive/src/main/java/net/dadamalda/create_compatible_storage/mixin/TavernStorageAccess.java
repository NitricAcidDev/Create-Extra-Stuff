package net.dadamalda.create_compatible_storage.mixin;

import com.github.ysbbbbbb.kaleidoscopetavern.block.AbstractStorageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.block.AbstractStorageBlock")
public interface TavernStorageAccess {
    @Invoker(value = "getClickedSlot", remap = false)
    int trainsInteractive$slot(Direction facing, BlockPos pos, BlockHitResult hit);
    @Invoker(value = "blockListCheck", remap = false)
    boolean trainsInteractive$blocked(ItemStack stack);
}
