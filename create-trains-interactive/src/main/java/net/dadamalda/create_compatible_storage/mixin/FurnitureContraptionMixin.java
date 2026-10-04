package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.FurnitureSeats;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;

@Mixin(Contraption.class)
public abstract class FurnitureContraptionMixin {
    @Shadow protected Map<BlockPos, Entity> initialPassengers;
    @Shadow protected abstract BlockPos toLocalPos(BlockPos pos);

    @Inject(method = "addBlock", at = @At("TAIL"), remap = false)
    private void addFurnitureSeat(Level level, BlockPos pos, Pair<StructureBlockInfo, BlockEntity> pair, CallbackInfo ci) {
        if (FurnitureSeats.height(pair.getLeft().state()).isEmpty()) return;
        Contraption self = (Contraption)(Object)this;
        BlockPos localPos = toLocalPos(pos);
        if (!self.getSeats().contains(localPos)) self.getSeats().add(localPos);
        // Transfer a rider from the furniture mod's stationary seat entity during assembly.
        for (Entity entity : level.getEntities(null, new AABB(pos).inflate(0.1))) {
            if (entity.isVehicle() && entity.getClass().getSimpleName().matches(".*(Sit|Seat|Chair).*")) {
                initialPassengers.put(localPos, entity.getFirstPassenger());
                break;
            }
        }
    }
}
