package net.dadamalda.create_compatible_storage.mixin;

import com.nitricacid.traininteractive.MovingTrainWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class NativeTrainServerLevelMixin {
    private ServerLevel trainsInteractive$level() { return (ServerLevel) (Object) this; }
    private MovingTrainWorld trainsInteractive$active() { return MovingTrainWorld.active(trainsInteractive$level()); }

    @Inject(method = "getBlockTicks", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$blockTicks(CallbackInfoReturnable<net.minecraft.world.ticks.LevelTicks<Block>> cir) {
        var world = trainsInteractive$active();
        if (world != null) cir.setReturnValue(world.blockTicks());
    }
    @Inject(method = "getFluidTicks", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$fluidTicks(CallbackInfoReturnable<net.minecraft.world.ticks.LevelTicks<net.minecraft.world.level.material.Fluid>> cir) {
        var world = trainsInteractive$active();
        if (world != null) cir.setReturnValue(world.fluidTicks());
    }

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$updated(BlockPos pos, BlockState before, BlockState after, int flags, CallbackInfo ci) {
        if (trainsInteractive$active() != null) ci.cancel();
    }
    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$sound(Player player, double x, double y, double z, Holder<SoundEvent> sound,
            SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
        var world = trainsInteractive$active();
        if (world == null) return;
        var point = world.global(new Vec3(x, y, z));
        MovingTrainWorld.outside(() -> {
            // Native train item actions run only on the server, so the placer has
            // no predicted local sound and must receive this broadcast too.
            trainsInteractive$level().playSeededSound(null, point.x, point.y, point.z, sound, source, volume, pitch, seed);
            return null;
        });
        ci.cancel();
    }
    @Inject(method = "sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", at = @At("HEAD"), cancellable = true)
    private <T extends ParticleOptions> void trainsInteractive$particles(T type, double x, double y, double z, int count,
            double dx, double dy, double dz, double speed, CallbackInfoReturnable<Integer> cir) {
        var world = trainsInteractive$active();
        if (world == null) return;
        var point = world.global(new Vec3(x, y, z));
        cir.setReturnValue(MovingTrainWorld.outside(() -> trainsInteractive$level()
                .sendParticles(type, point.x, point.y, point.z, count, dx, dy, dz, speed)));
    }
    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$event(Player player, int event, BlockPos pos, int data, CallbackInfo ci) {
        var world = trainsInteractive$active();
        if (world == null) return;
        var point = BlockPos.containing(world.global(Vec3.atCenterOf(pos)));
        MovingTrainWorld.outside(() -> { trainsInteractive$level().levelEvent(player, event, point, data); return null; });
        ci.cancel();
    }
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$drop(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        var world = trainsInteractive$active();
        if (world == null) return;
        var point = world.global(entity.position());
        entity.setPos(point.x, point.y, point.z);
        cir.setReturnValue(MovingTrainWorld.outside(() -> trainsInteractive$level().addFreshEntity(entity)));
    }
    @Inject(method = "updateNeighborsAt", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$neighbors(BlockPos pos, Block block, CallbackInfo ci) {
        if (trainsInteractive$active() != null) ci.cancel();
    }
    @Inject(method = "updateNeighborsAtExceptFromFacing", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$otherNeighbors(BlockPos pos, Block block, Direction direction, CallbackInfo ci) {
        if (trainsInteractive$active() != null) ci.cancel();
    }
    @Inject(method = "neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$neighbor(BlockPos pos, Block block, BlockPos source, CallbackInfo ci) {
        if (trainsInteractive$active() != null) ci.cancel();
    }
    @Inject(method = "neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$stateNeighbor(BlockState state, BlockPos pos, Block block, BlockPos source, boolean moving, CallbackInfo ci) {
        if (trainsInteractive$active() != null) ci.cancel();
    }

    @Inject(method = "gameEvent", at = @At("HEAD"), cancellable = true)
    private void trainsInteractive$gameEvent(Holder<GameEvent> event, Vec3 pos, GameEvent.Context context, CallbackInfo ci) {
        var world = trainsInteractive$active();
        if (world == null) return;
        var point = world.global(pos);
        MovingTrainWorld.outside(() -> { trainsInteractive$level().gameEvent(event, point, context); return null; });
        ci.cancel();
    }
}
