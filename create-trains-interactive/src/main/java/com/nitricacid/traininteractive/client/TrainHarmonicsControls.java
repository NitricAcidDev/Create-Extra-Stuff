package com.nitricacid.traininteractive.client;

import com.nitricacid.traininteractive.HarmonicsControls;
import com.nitricacid.traininteractive.TrainMusicSettingsPacket;
import com.nitricacid.traininteractive.TrainWorldAccess;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "create_trains_interactive", value = Dist.CLIENT)
public final class TrainHarmonicsControls {
    private TrainHarmonicsControls() {}
    public static ScrollOptionBehaviour<?> behaviour(TrainRayTrace.Target target) {
        if (target == null || !ModList.get().isLoaded("createharmonics")) return null;
        var world = ((TrainWorldAccess) target.entity()).trainsInteractive$world();
        var result = world.run(target.hit().getBlockPos(), () -> HarmonicsControls.behaviour(world, target.hit().getBlockPos()));
        if (result != null && result.getSlotPositioning() instanceof ValueBoxTransform.Sided sided)
            sided.fromSide(target.hit().getDirection());
        return result;
    }
    public static boolean hitsIcon(TrainRayTrace.Target target, ScrollOptionBehaviour<?> behaviour) {
        var world = ((TrainWorldAccess) target.entity()).trainsInteractive$world();
        return world.run(target.hit().getBlockPos(), () -> behaviour.testHit(target.hit().getLocation()));
    }
    public static ValueSettingsScreen screen(TrainRayTrace.Target target, ScrollOptionBehaviour<?> behaviour) {
        return new ValueSettingsScreen(target.hit().getBlockPos(), behaviour.createBoard(Minecraft.getInstance().player, target.hit()),
                behaviour.getValueSettings(), ignored -> {}, 0) {
            @Override protected void saveAndClose(double x, double y) {
                var value = getClosestCoordinate((int) x, (int) y);
                PacketDistributor.sendToServer(new TrainMusicSettingsPacket(target.entity().getId(), target.hit().getBlockPos(), value.value()));
                onClose();
            }
        };
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void use(InputEvent.InteractionKeyMappingTriggered event) {
        var client = Minecraft.getInstance();
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND || client.screen != null
                || client.player == null || client.player.isSpectator() || client.player.isShiftKeyDown()) return;
        var target = TrainRayTrace.find(client);
        var behaviour = behaviour(target);
        if (behaviour == null || !hitsIcon(target, behaviour)) return;
        event.setCanceled(true); event.setSwingHand(false);
        client.setScreen(screen(target, behaviour));
    }
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        var client = Minecraft.getInstance();
        if (client.player == null || client.screen != null || client.options.hideGui || client.player.isSpectator()) return;
        var target = TrainRayTrace.find(client);
        var behaviour = behaviour(target);
        if (behaviour == null) return;
        var transform = behaviour.getSlotPositioning();
        var world = ((TrainWorldAccess) target.entity()).trainsInteractive$world();
        var pos = target.hit().getBlockPos();
        var state = world.state(pos);
        if (!transform.shouldRender(target.entity().level(), pos, state)) return;
        var pose = event.getPoseStack();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var camera = event.getCamera().getPosition();
        var at = target.entity().getPosition(partial);
        var buffers = client.renderBuffers().bufferSource();
        pose.pushPose();
        pose.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
        target.entity().applyLocalTransforms(pose, partial);
        pose.translate(pos.getX(), pos.getY(), pos.getZ());
        transform.transform(target.entity().level(), pos, state, pose);
        if (hitsIcon(target, behaviour)) {
            pose.pushPose(); pose.scale(-2.01f, -2.01f, 2.01f);
            pose.translate(-.5, -.5, -.5 / 16);
            com.simibubi.create.foundation.gui.AllIcons.VALUE_BOX_HOVER_6PX.render(pose, buffers, 0xffffff);
            pose.popPose();
        }
        float fontScale = -transform.getFontScale();
        pose.scale(fontScale, fontScale, fontScale);
        // Native Create icon and Harmonics positioning, attached to the carriage transform.
        new ValueBox.IconValueBox(behaviour.label, behaviour.get(), new AABB(BlockPos.ZERO), pos)
                .transform(transform).renderContents(pose, buffers);
        pose.popPose();
    }
}
