package net.dadamalda.create_compatible_storage.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AbstractContraptionEntity.class, remap = false)
public interface ContraptionSpawnAccess {
    @Invoker("writeAdditional") void trainsInteractive$writeSpawn(CompoundTag tag, HolderLookup.Provider registries, boolean spawn);
    @Invoker("readAdditional") void trainsInteractive$readSpawn(CompoundTag tag, boolean spawn);
}
