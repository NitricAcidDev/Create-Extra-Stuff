package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.FurnitureSeats;
import com.nitricacid.traininteractive.TavernSeatPositions;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractContraptionEntity.class, priority = 1200)
public abstract class FurniturePassengerMixin {
    @Inject(method = "getPassengerPosition", at = @At("HEAD"), cancellable = true, remap = false, order = 900)
    private void positionOnFurniture(Entity passenger, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        Vec3 position = furniturePosition(passenger, partialTicks);
        if (position != null) cir.setReturnValue(position);
    }

    @Inject(method = "positionRider", at = @At("HEAD"), cancellable = true, remap = false, order = 900)
    private void positionFurnitureRider(Entity passenger, Entity.MoveFunction callback, CallbackInfo ci) {
        AbstractContraptionEntity self = (AbstractContraptionEntity)(Object)this;
        if (passenger.getVehicle() != self) return;
        Vec3 position = furniturePosition(passenger, 1);
        if (position == null) return;
        callback.accept(passenger, position.x, position.y, position.z);
        ci.cancel();
    }

    @org.spongepowered.asm.mixin.Unique
    private Vec3 furniturePosition(Entity passenger, float partialTicks) {
        AbstractContraptionEntity self = (AbstractContraptionEntity)(Object)this;
        if (self.getContraption() == null) return null;
        BlockPos pos = self.getContraption().getSeatOf(passenger.getUUID());
        if (pos == null) return null;
        var info = self.getContraption().getBlocks().get(pos);
        if (info == null) return null;
        var height = FurnitureSeats.height(info.state());
        if (height.isEmpty()) return null;
        // Match the furniture's native small seat entity and the passenger attachment point.
        Vec3 sitting = self.toGlobalVector(Vec3.atLowerCornerOf(pos).add(0.5, height.getAsDouble(), 0.5), partialTicks);
        String namespace = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(info.state().getBlock()).getNamespace();
        double offset = namespace.equals("kaleidoscope_tavern") || namespace.equals("kaleidoscope_world_liquor")
                ? TavernSeatPositions.passengerOffset(passenger, self.level()) : -0.3;
        return sitting.add(0, offset, 0);
    }
}
