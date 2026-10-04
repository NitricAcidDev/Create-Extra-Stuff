package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingTrainWorld;
import com.nitricacid.traininteractive.TrainWorldAccess;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContraptionEntity.class)
public abstract class NativeTrainEntityMixin implements TrainWorldAccess {
    @Unique private MovingTrainWorld trainsInteractive$world;

    @Override public MovingTrainWorld trainsInteractive$world() {
        var entity = (AbstractContraptionEntity) (Object) this;
        if (trainsInteractive$world == null || trainsInteractive$world.contraption != entity.getContraption())
            trainsInteractive$world = new MovingTrainWorld(entity);
        return trainsInteractive$world;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void trainsInteractive$tickServiceBlocks(CallbackInfo ci) {
        var entity = (AbstractContraptionEntity) (Object) this;
        if (!entity.level().isClientSide && entity.getContraption() != null) trainsInteractive$world().tick();
    }
}
