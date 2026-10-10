package com.nitricacid.traininteractive.verification;

import com.nitricacid.traininteractive.*;
import com.nitricacid.traininteractive.client.TrainHarmonicsControls;
import com.nitricacid.traininteractive.client.TrainRayTrace;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.world.inventory.LightroomMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

public final class TrainMusicPhotoClientChecks {
    public static void check(Minecraft client, GuiGraphics graphics) {
        if (!ModList.get().isLoaded("exposure") || !ModList.get().isLoaded("createharmonics")) return;
        var c = new BearingContraption(false, Direction.EAST);
        var photo = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("exposure:lightroom")).defaultBlockState();
        var jukebox = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("createharmonics:andesite_jukebox")).defaultBlockState();
        c.anchor = BlockPos.ZERO; c.bounds = new AABB(-2, -2, -2, 4, 4, 4);
        c.getBlocks().put(BlockPos.ZERO, new StructureBlockInfo(BlockPos.ZERO, photo, null));
        c.getBlocks().put(BlockPos.ZERO.east(), new StructureBlockInfo(BlockPos.ZERO.east(), jukebox, null));
        c.getStorage().initialize();
        var train = OrientedContraptionEntity.create(client.level, c, Direction.SOUTH);
        c.onEntityCreated(train);
        train.setId(-54321); train.setPos(client.player.getX(), client.player.getY(), client.player.getZ());
        client.level.addEntity(train);
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), client.level.registryAccess());
        try {
            world.set(BlockPos.ZERO.above(), Blocks.AIR.defaultBlockState());
            world.set(BlockPos.ZERO.above(2), Blocks.STONE.defaultBlockState());
            world.set(BlockPos.ZERO.above().west(), Blocks.GLOWSTONE.defaultBlockState());
            buffer.writeBlockPos(BlockPos.ZERO); buffer.writeInt(MovingServiceMenus.MAGIC); buffer.writeVarInt(train.getId());
            var menu = LightroomMenu.fromBuffer(19, client.player.getInventory(), buffer);
            if (menu.getBlockEntity().getLevel() == client.level || !menu.getBlockEntity().hasSufficientLightLevel())
                throw new AssertionError("Lightroom menu must resolve the train block and read its onboard light");
            var photoScreen = new LightroomScreen(menu, client.player.getInventory(), Component.literal("Moving Lightroom"));
            photoScreen.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
            photoScreen.render(graphics, 0, 0, 0);
            var pos = BlockPos.ZERO.east();
            var target = new TrainRayTrace.Target(train, new BlockHitResult(Vec3.atCenterOf(pos), Direction.SOUTH, pos, false));
            var behaviour = TrainHarmonicsControls.behaviour(target);
            if (behaviour == null) throw new AssertionError("Harmonics moving jukebox must expose its native mode behaviour");
            for (var side : Direction.values()) {
                if (behaviour.getSlotPositioning() instanceof com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided sided) sided.fromSide(side);
                if (!behaviour.getSlotPositioning().shouldRender(client.level, pos, jukebox)) continue;
                var icon = behaviour.getSlotPositioning().getLocalOffset(client.level, pos, jukebox).add(Vec3.atLowerCornerOf(pos));
                target = new TrainRayTrace.Target(train, new BlockHitResult(icon, side, pos, false));
                if (!TrainHarmonicsControls.hitsIcon(target, behaviour)) throw new AssertionError("Native moving icon must remain clickable");
                var screen = TrainHarmonicsControls.screen(target, behaviour);
                screen.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
                screen.render(graphics, 0, 0, 0);
                org.slf4j.LoggerFactory.getLogger(TrainMusicPhotoClientChecks.class).info("Moving Lightroom native menu and screen, onboard light, and Harmonics icon/settings screen checks passed");
                return;
            }
            throw new AssertionError("A moving jukebox must have a visible settings face");
        } finally {
            buffer.release();
            client.level.removeEntity(train.getId(), net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
    }
}
