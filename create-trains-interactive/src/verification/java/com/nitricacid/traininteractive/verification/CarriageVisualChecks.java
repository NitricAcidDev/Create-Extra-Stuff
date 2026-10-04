package com.nitricacid.traininteractive.verification;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionVisual;
import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;

public final class CarriageVisualChecks {
    private CarriageVisualChecks() {}
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void check(ClientLevel level) throws Exception {
        var made = new int[1];
        var handle = new InstanceHandle() {
            public void setChanged() {}
            public void setDeleted() {}
            public void setVisible(boolean visible) {}
            public boolean isVisible() { return true; }
        };
        InstancerProvider provider = new InstancerProvider() {
            public <I extends dev.engine_room.flywheel.api.instance.Instance> Instancer<I> instancer(InstanceType<I> type, Model model, int bias) {
                return (Instancer<I>) new Instancer<TransformedInstance>() {
                    public TransformedInstance createInstance() { made[0]++; return new TransformedInstance(InstanceTypes.TRANSFORMED, handle); }
                    public void stealInstance(TransformedInstance instance) {}
                };
            }
        };
        var embedding = new VisualEmbedding() {
            public InstancerProvider instancerProvider() { return provider; }
            public Vec3i renderOrigin() { return Vec3i.ZERO; }
            public VisualEmbedding createEmbedding(Vec3i origin) { return this; }
            public void transforms(org.joml.Matrix4fc pose, org.joml.Matrix3fc normal) {}
            public void delete() {}
        };
        var entity = new CarriageContraptionEntity(AllEntityTypes.CARRIAGE_CONTRAPTION.get(), level);
        var visual = new CarriageContraptionVisual(embedding, entity, 1);
        visual.setSectionCollector(sections -> {});
        var frame = (DynamicVisual.Context) java.lang.reflect.Proxy.newProxyInstance(DynamicVisual.Context.class.getClassLoader(),
                new Class<?>[]{DynamicVisual.Context.class}, (proxy, method, args) -> method.getName().equals("partialTick") ? 1F : null);
        visual.beginFrame(frame);
        if (made[0] != 0) throw new AssertionError("Missing carriage data must defer its visual safely");
        var c = new CarriageContraption(Direction.SOUTH);
        c.anchor = BlockPos.ZERO;
        c.bounds = new AABB(BlockPos.ZERO);
        c.getBlocks().put(BlockPos.ZERO, new StructureBlockInfo(BlockPos.ZERO, Blocks.STONE.defaultBlockState(), null));
        c.entity = entity;
        var set = AbstractContraptionEntity.class.getDeclaredMethod("setContraption", com.simibubi.create.content.contraptions.Contraption.class);
        set.setAccessible(true); set.invoke(entity, c);
        visual.beginFrame(frame);
        if (made[0] != 1) throw new AssertionError("A delayed version-zero carriage must build its structure");
        var bound = CarriageContraptionVisual.class.getDeclaredField("contraption");
        bound.setAccessible(true);
        if (bound.get(visual) != c) throw new AssertionError("The bogey visual must refresh its delayed carriage reference");
        visual.delete();
        org.slf4j.LoggerFactory.getLogger(CarriageVisualChecks.class).info("Delayed carriage structure and bogey visual recovery checks passed");
    }
}
