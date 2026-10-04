package net.dadamalda.create_compatible_storage.mixin;

import java.util.OptionalInt;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChiseledBookShelfBlock.class)
public interface ChiseledBookshelfAccess {
    @Invoker("getHitSlot")
    OptionalInt trainsInteractive$getHitSlot(BlockHitResult hit, BlockState state);
}
