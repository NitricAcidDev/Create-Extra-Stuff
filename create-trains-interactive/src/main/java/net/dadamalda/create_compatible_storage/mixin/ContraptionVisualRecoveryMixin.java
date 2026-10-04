package net.dadamalda.create_compatible_storage.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContraptionVisual.class, remap = false)
public abstract class ContraptionVisualRecoveryMixin extends AbstractEntityVisual<AbstractContraptionEntity> {
    @Shadow protected int lastStructureVersion;
    @Shadow protected int lastVersionChildren;
    protected ContraptionVisualRecoveryMixin(VisualizationContext context, AbstractContraptionEntity entity, float partialTick) { super(context, entity, partialTick); }
    @Inject(method = "<init>", at = @At("RETURN"))
    private void trainsInteractive$markMissingStructure(VisualizationContext context, AbstractContraptionEntity source, float partialTick, CallbackInfo ci) {
        if (source.getContraption() == null) { lastStructureVersion = -1; lastVersionChildren = -1; }
    }
    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$waitForStructure(DynamicVisual.Context context, CallbackInfo ci) {
        if (entity.getContraption() == null) {
            // Version zero is also a newly received structure's version; force its first build.
            lastStructureVersion = -1;
            lastVersionChildren = -1;
            ci.cancel();
        }
    }
}
