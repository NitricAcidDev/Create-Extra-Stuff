package com.nitricacid.traininteractive;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.dadamalda.create_compatible_storage.mixin.ContraptionUpdateTagsAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** Routes only the current native bar/kitchen call to an assembled contraption. */
public final class MovingTrainWorld {
    private static final ThreadLocal<MovingTrainWorld> ACTIVE = new ThreadLocal<>();
    public final AbstractContraptionEntity entity;
    public final Contraption contraption;
    private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
    private final Map<BlockPos, CompoundTag> saved = new HashMap<>();
    private final Set<BlockPos> changedStates = new HashSet<>();
    private final Set<BlockPos> tickingPositions = new HashSet<>();
    private final Set<BlockPos> networkDirty = new HashSet<>();
    private static final Map<BlockEntity, java.lang.ref.WeakReference<MovingTrainWorld>> OWNERS = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private final TrainLighting lighting = new TrainLighting(this);
    public TrainLighting lighting() { return lighting; }
    public static void bind(BlockEntity be, MovingTrainWorld world) { OWNERS.put(be, new java.lang.ref.WeakReference<>(world)); }
    public static MovingTrainWorld owner(BlockEntity be) {
        var reference = OWNERS.get(be);
        return reference == null ? null : reference.get();
    }
    private BlockPos interactingPos = BlockPos.ZERO;
    private record Pending(Block block, long time) {}
    private final Map<BlockPos, Pending> pendingTicks = new HashMap<>();
    private final net.minecraft.world.ticks.LevelTicks<Block> blockTicks;
    private final net.minecraft.world.ticks.LevelTicks<net.minecraft.world.level.material.Fluid> fluidTicks;

    public MovingTrainWorld(AbstractContraptionEntity entity) {
        this.entity = entity;
        this.contraption = entity.getContraption();
        if (contraption != null) contraption.getBlocks().forEach((pos, info) -> {
            if (ticks(info.state())) tickingPositions.add(pos);
        });
        blockTicks = new net.minecraft.world.ticks.LevelTicks<>(key -> false, () -> entity.level().getProfiler()) {
            @Override public void schedule(net.minecraft.world.ticks.ScheduledTick<Block> tick) {
                var existing = pendingTicks.get(tick.pos());
                if (existing == null || tick.triggerTick() < existing.time)
                    pendingTicks.put(tick.pos().immutable(), new Pending(tick.type(), tick.triggerTick()));
                changedStates.add(tick.pos().immutable());
            }
        };
        fluidTicks = new net.minecraft.world.ticks.LevelTicks<>(key -> false, () -> entity.level().getProfiler()) {
            @Override public void schedule(net.minecraft.world.ticks.ScheduledTick<net.minecraft.world.level.material.Fluid> tick) {
                // Waterlogging is retained as block state; stationary-world fluid ticks must not be scheduled here.
            }
        };
    }

    public net.minecraft.world.ticks.LevelTicks<Block> blockTicks() { return blockTicks; }
    public net.minecraft.world.ticks.LevelTicks<net.minecraft.world.level.material.Fluid> fluidTicks() { return fluidTicks; }

    public static MovingTrainWorld active(Level level) {
        var world = ACTIVE.get();
        return world != null && world.entity.level() == level ? world : null;
    }

    public static MovingTrainWorld current() { return ACTIVE.get(); }
    public BlockPos interactingPos() { return interactingPos; }

    public <T> T run(BlockPos pos, Supplier<T> action) {
        var previous = ACTIVE.get();
        var previousPos = interactingPos;
        ACTIVE.set(this);
        interactingPos = pos;
        try { return action.get(); }
        finally {
            interactingPos = previousPos;
            if (previous == null) ACTIVE.remove(); else ACTIVE.set(previous);
        }
    }

    public static <T> T outside(Supplier<T> action) {
        var previous = ACTIVE.get();
        ACTIVE.remove();
        try { return action.get(); }
        finally { if (previous != null) ACTIVE.set(previous); }
    }

    public Vec3 global(Vec3 local) { return entity.toGlobalVector(local, 1); }

    public BlockState state(BlockPos pos) {
        var info = contraption.getBlocks().get(pos);
        return info == null ? Blocks.AIR.defaultBlockState() : info.state();
    }

    public BlockEntity blockEntity(BlockPos pos) {
        var info = contraption.getBlocks().get(pos);
        if (info == null || !(info.state().getBlock() instanceof EntityBlock block)) return null;
        var be = blockEntities.get(pos);
        if (be == null || !be.getType().isValid(info.state())) {
            be = block.newBlockEntity(pos, info.state());
            if (be == null) return null;
            if (info.nbt() != null) be.loadWithComponents(info.nbt().copy(), entity.level().registryAccess());
            if (info.nbt() != null && info.nbt().contains("TrainScheduledTick"))
                pendingTicks.put(pos.immutable(), new Pending(info.state().getBlock(), info.nbt().getLong("TrainScheduledTick")));
            be.setLevel(entity.level());
            bind(be, this);
            blockEntities.put(pos.immutable(), be);
            saved.put(pos.immutable(), info.nbt() == null ? new CompoundTag() : info.nbt().copy());
        } else if (info.nbt() != null && !info.nbt().equals(saved.get(pos))) {
            // Another integration may have edited this block since our last call.
            var refresh = be;
            run(pos, () -> { refresh.loadWithComponents(info.nbt().copy(), entity.level().registryAccess()); return null; });
            saved.put(pos.immutable(), info.nbt().copy());
        }
        be.setBlockState(info.state());
        return be;
    }

    public boolean set(BlockPos pos, BlockState state) {
        // Never let a local-coordinate native call write into the stationary world.
        // New serving items may occupy a directly adjacent cell of this contraption.
        if (!contraption.getBlocks().containsKey(pos)
                && net.minecraft.core.Direction.stream().noneMatch(d -> contraption.getBlocks().containsKey(pos.relative(d))))
            return false;
        var previous = contraption.getBlocks().get(pos);
        if (previous != null && previous.state() == state) return true;
        var nbt = previous != null && previous.state().getBlock() == state.getBlock() ? previous.nbt() : null;
        contraption.getBlocks().put(pos.immutable(), new StructureBlockInfo(pos.immutable(), state, nbt));
        if (previous == null || previous.state().getBlock() != state.getBlock()) {
            blockEntities.remove(pos); saved.remove(pos);
            pendingTicks.remove(pos);
        } else if (blockEntities.containsKey(pos)) blockEntities.get(pos).setBlockState(state);
        changedStates.add(pos.immutable());
        if (ticks(state)) tickingPositions.add(pos.immutable()); else tickingPositions.remove(pos);
        var handler = MovingInteractionBehaviour.REGISTRY.get(state);
        if (handler == null || state.isAir()) contraption.getInteractors().remove(pos);
        else contraption.getInteractors().put(pos.immutable(), handler);
        if (contraption.bounds != null)
            contraption.bounds = contraption.bounds.minmax(new net.minecraft.world.phys.AABB(pos));
        contraption.invalidateColliders();
        return true;
    }

    public void setBlockEntity(BlockEntity be) {
        be.setLevel(entity.level());
        blockEntities.put(be.getBlockPos().immutable(), be);
    }

    public void removeBlockEntity(BlockPos pos) {
        blockEntities.remove(pos); saved.remove(pos);
    }

    public void updateData(BlockPos pos, CompoundTag tag) {
        var info = contraption.getBlocks().get(pos);
        if (info == null || !info.state().hasBlockEntity()) return;
        contraption.getBlocks().put(pos, new StructureBlockInfo(pos, info.state(), tag.copy()));
        blockEntities.remove(pos); saved.remove(pos);
        changedStates.add(pos.immutable());
    }

    public void flush(boolean notifyClients) {
        Set<BlockPos> changes = new HashSet<>(changedStates);
        for (var entry : blockEntities.entrySet()) {
            if (!contraption.getBlocks().containsKey(entry.getKey())) continue;
            var tag = entry.getValue().saveWithFullMetadata(entity.level().registryAccess());
            tag.remove("x"); tag.remove("y"); tag.remove("z");
            var pending = pendingTicks.get(entry.getKey());
            if (pending != null) tag.putLong("TrainScheduledTick", pending.time);
            if (!tag.equals(saved.get(entry.getKey()))) {
                saved.put(entry.getKey(), tag.copy());
                changes.add(entry.getKey());
            }
        }
        for (BlockPos pos : changes) {
            var info = contraption.getBlocks().get(pos);
            if (info == null) continue;
            var tag = saved.getOrDefault(pos, info.nbt() == null ? new CompoundTag() : info.nbt());
            var updated = new StructureBlockInfo(pos, info.state(), info.state().hasBlockEntity() ? tag.copy() : null);
            contraption.getBlocks().put(pos, updated);
            var updateTags = ((ContraptionUpdateTagsAccess) contraption).trainsInteractive$updateTags();
            if (updated.nbt() == null) updateTags.remove(pos); else updateTags.put(pos, tag.copy());
            for (var actor : contraption.getActors()) {
                if (actor.getLeft().pos().equals(pos)) {
                    actor.setLeft(updated);
                    if (actor.getRight() != null) actor.getRight().blockEntityData = updated.nbt();
                }
            }
            networkDirty.add(pos);
        }
        changedStates.clear();
        if (notifyClients) {
            for (var pos : networkDirty) {
                var info = contraption.getBlocks().get(pos);
                if (info != null) PacketDistributor.sendToPlayersTrackingEntity(entity,
                        new TrainBlockDataPacket(entity.getId(), pos, info.state(), info.nbt() == null ? new CompoundTag() : info.nbt().copy()));
            }
            networkDirty.clear();
        }
    }

    public static boolean isServiceBlock(BlockState state) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.getNamespace().equals("kaleidoscope_tavern")
                || id.getNamespace().equals("kaleidoscope_world_liquor")
                || id.getNamespace().equals("farmersdelight")
                || id.toString().equals("exposure:lightroom")
                || id.toString().equals("createharmonics:andesite_jukebox");
    }

    public static boolean ticks(BlockState state) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return switch (id.toString()) {
            case "kaleidoscope_tavern:barrel", "kaleidoscope_tavern:tap",
                 "farmersdelight:cooking_pot", "farmersdelight:stove", "farmersdelight:skillet", "exposure:lightroom" -> true;
            default -> id.getNamespace().equals("kaleidoscope_world_liquor")
                    && (id.getPath().contains("freezer") || id.getPath().contains("brewing"));
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void tick() {
        tickAt(entity.level().getGameTime());
    }

    public void tickAt(long time) {
        if (!(entity.level() instanceof ServerLevel) || contraption == null || !entity.isAlive()) return;
        TrainSignals.update(this);
        // Copy the keys: serving drinks can replace glassware while a native ticker runs.
        for (var pos : java.util.List.copyOf(tickingPositions)) {
            var info = contraption.getBlocks().get(pos);
            if (info == null || !ticks(info.state()) || !(info.state().getBlock() instanceof EntityBlock block)) {
                tickingPositions.remove(pos); continue;
            }
            run(info.pos(), () -> {
                var be = blockEntity(info.pos());
                if (be != null) {
                    BlockEntityTicker ticker = block.getTicker(entity.level(), state(info.pos()), be.getType());
                    if (ticker != null) ticker.tick(entity.level(), info.pos(), state(info.pos()), be);
                } else tickingPositions.remove(info.pos());
                return null;
            });
        }
        for (var entry : java.util.List.copyOf(pendingTicks.entrySet())) {
            if (entry.getValue().time > time) continue;
            pendingTicks.remove(entry.getKey());
            changedStates.add(entry.getKey());
            run(entry.getKey(), () -> {
                var state = state(entry.getKey());
                if (state.getBlock() == entry.getValue().block)
                    state.tick((ServerLevel) entity.level(), entry.getKey(), entity.level().random);
                return null;
            });
        }
        // Persist every tick; batch counter-only visual updates to four per second.
        flush(!changedStates.isEmpty() || time % 5 == 0);
    }
}
