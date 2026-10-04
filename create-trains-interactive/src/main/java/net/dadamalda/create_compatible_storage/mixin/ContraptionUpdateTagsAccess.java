package net.dadamalda.create_compatible_storage.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(Contraption.class)
public interface ContraptionUpdateTagsAccess {
    @Accessor(value = "updateTags", remap = false)
    Map<BlockPos, CompoundTag> trainsInteractive$updateTags();
}
