package net.dadamalda.create_compatible_storage.mixin;

import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CarriageContraptionVisual.class, remap = false)
public abstract class CarriageVisualRecoveryMixin extends ContraptionVisual<CarriageContraptionEntity> {
    @Shadow @Final @Mutable private CarriageContraption contraption;
    protected CarriageVisualRecoveryMixin(VisualizationContext context, CarriageContraptionEntity entity, float partialTick) { super(context, entity, partialTick); }
    @Inject(method = "checkCarriage", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$refreshStructure(float partialTick, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getContraption() instanceof CarriageContraption current) contraption = current;
        else cir.setReturnValue(false);
    }
}
