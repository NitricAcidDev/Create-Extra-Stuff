package net.dadamalda.create_compatible_storage.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Contraption.class)
public abstract class BookshelfSyncMixin {
    @Shadow protected Map<BlockPos, StructureBlockInfo> blocks;
    @Shadow protected Map<BlockPos, CompoundTag> updateTags;

    @Inject(method = "writeBlocksCompound", at = @At("HEAD"), remap = false)
    private void trainsInteractive$syncBooks(boolean spawnPacket, CallbackInfoReturnable<CompoundTag> cir) {
        if (!spawnPacket) return;
        // Vanilla's update tag omits books. Include them when a contraption is sent to a client,
        // including when an existing train is loaded or a player starts tracking it later.
        for (var info : blocks.values()) {
            if ((info.state().is(Blocks.CHISELED_BOOKSHELF)
                    || com.nitricacid.traininteractive.MovingTrainWorld.isServiceBlock(info.state())) && info.nbt() != null)
                updateTags.put(info.pos(), info.nbt().copy());
        }
    }
}
