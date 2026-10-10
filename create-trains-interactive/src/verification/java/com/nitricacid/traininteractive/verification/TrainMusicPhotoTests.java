package com.nitricacid.traininteractive.verification;

import com.nitricacid.traininteractive.*;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.inventory.LightroomMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class TrainMusicPhotoTests {
    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void lampsUpdateAndEmitLightWithoutChangingStationaryWorld(GameTestHelper helper) {
        var train = train(helper, Blocks.REDSTONE_LAMP.defaultBlockState());
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var at = BlockPos.ZERO;
        var lever = at.east();
        world.set(lever, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.POWERED, true));
        var stationary = helper.getLevel().getBlockState(lever);
        world.tickAt(helper.getLevel().getGameTime());
        helper.assertTrue(world.state(at).getValue(RedstoneLampBlock.LIT), "Onboard lever must light its adjacent lamp");
        helper.assertTrue(world.run(at, () -> helper.getLevel().getBrightness(LightLayer.BLOCK, at.above())) == 14, "Lamp must provide native light level 14 to the cell above");
        world.set(at.above(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(world.lighting().block(at.above()) == 0, "Opaque train blocks must block onboard light");
        world.set(lever, world.state(lever).setValue(LeverBlock.POWERED, false));
        long now = helper.getLevel().getGameTime();
        world.tickAt(now);
        helper.assertTrue(world.state(at).getValue(RedstoneLampBlock.LIT), "Lamp must retain its native four-tick off delay");
        world.tickAt(now + 4);
        helper.assertTrue(!world.state(at).getValue(RedstoneLampBlock.LIT) && world.lighting().block(at) == 0, "Lamp and cached light must turn off after the native delay");
        helper.assertTrue(helper.getLevel().getBlockState(lever) == stationary, "Train signals must not edit the stationary block at the same local coordinates");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void onboardDustPowersDistantLamp(GameTestHelper helper) {
        var train = train(helper, Blocks.REDSTONE_BLOCK.defaultBlockState());
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        for (int x = 0; x <= 4; x++) world.set(new BlockPos(x, -1, 0), Blocks.STONE.defaultBlockState());
        for (int x = 1; x < 4; x++) world.set(new BlockPos(x, 0, 0), Blocks.REDSTONE_WIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.RedStoneWireBlock.EAST, net.minecraft.world.level.block.state.properties.RedstoneSide.SIDE)
                .setValue(net.minecraft.world.level.block.RedStoneWireBlock.WEST, net.minecraft.world.level.block.state.properties.RedstoneSide.SIDE));
        var lamp = new BlockPos(4, 0, 0);
        world.set(lamp, Blocks.REDSTONE_LAMP.defaultBlockState());
        world.tickAt(helper.getLevel().getGameTime());
        helper.assertTrue(world.state(lamp).getValue(RedstoneLampBlock.LIT), "Onboard dust must transmit power to the lamp");
        world.set(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
        world.tickAt(helper.getLevel().getGameTime() + 1);
        world.tickAt(helper.getLevel().getGameTime() + 5);
        helper.assertTrue(!world.state(lamp).getValue(RedstoneLampBlock.LIT), "Removing onboard power must depower dust and lamp");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive", timeoutTicks = 200)
    public static void lightroomPrintsPhotosUnderAnOnboardLamp(GameTestHelper helper) {
        var train = train(helper, state("exposure:lightroom"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        world.set(BlockPos.ZERO.above(), Blocks.AIR.defaultBlockState());
        world.set(BlockPos.ZERO.above(2), Blocks.STONE.defaultBlockState());
        var be = (LightroomBlockEntity) world.blockEntity(BlockPos.ZERO);
        helper.assertTrue(!be.hasSufficientLightLevel(), "Roofed Lightroom must not receive fake stationary-world daylight");
        world.set(BlockPos.ZERO.above().east(), Blocks.REDSTONE_LAMP.defaultBlockState());
        world.set(BlockPos.ZERO.above().east(2), Blocks.REDSTONE_BLOCK.defaultBlockState());
        world.tickAt(helper.getLevel().getGameTime());
        helper.assertTrue(be.hasSufficientLightLevel(), "Onboard lamp must satisfy Exposure's actual light requirement");
        var filmItem = BuiltInRegistries.ITEM.stream().filter(item -> item instanceof io.github.mortuusars.exposure.world.item.DevelopedFilmItem).findFirst().orElseThrow();
        var film = new ItemStack(filmItem);
        var frame = Frame.EMPTY.toMutable().setIdentifier(io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier.texture(
                ResourceLocation.withDefaultNamespace("textures/block/stone.png"))).toImmutable();
        film.set(Exposure.DataComponents.FILM_FRAMES, java.util.List.of(frame));
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        var seat = world.global(net.minecraft.world.phys.Vec3.atCenterOf(BlockPos.ZERO));
        player.setPos(seat.x, seat.y, seat.z);
        world.run(BlockPos.ZERO, () -> {
            be.setLastPlayer(player);
            be.setItem(0, film); be.setItem(1, new ItemStack(Items.PAPER));
            be.setItem(2, new ItemStack(Items.CYAN_DYE)); be.setItem(3, new ItemStack(Items.MAGENTA_DYE));
            be.setItem(4, new ItemStack(Items.YELLOW_DYE)); be.setItem(5, new ItemStack(Items.BLACK_DYE));
            player.containerMenu = be.createMenu(3, player.getInventory(), player);
            MovingServiceMenus.opened(player, world);
            return null;
        });
        try {
            helper.assertTrue(player.containerMenu instanceof LightroomMenu, "Train Lightroom must retain Exposure's native menu");
            helper.assertTrue(Boolean.TRUE.equals(MovingServiceMenus.valid(player)), "Printing menu must follow the train within reach");
            helper.assertTrue(world.run(BlockPos.ZERO, be::canPrint), "Native film, paper, dyes and onboard lighting must allow printing");
            MovingServiceMenus.run(player, () -> player.containerMenu.clickMenuButton(player, 0));
            helper.assertTrue(world.run(BlockPos.ZERO, be::isPrinting), "Native print button must start an onboard printing process");
            int time = ((LightroomMenu) player.containerMenu).getData().get(1);
            for (int i = 0; i <= time; i++) world.tickAt(helper.getLevel().getGameTime() + i);
            helper.assertTrue(be.getItem(6).is(Exposure.Items.PHOTOGRAPH.get()), "Native printing ticks must produce a photograph on the train");
            world.flush(false);
            var serialized = world.contraption.getBlocks().get(BlockPos.ZERO).nbt();
            var restored = new LightroomBlockEntity(BlockPos.ZERO, world.state(BlockPos.ZERO));
            restored.loadWithComponents(serialized, helper.getLevel().registryAccess());
            helper.assertTrue(restored.getItem(6).is(Exposure.Items.PHOTOGRAPH.get()), "Printed photograph must survive saved train data");
        } finally { MovingServiceMenus.closed(player); player.containerMenu = player.inventoryMenu; }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void harmonicsModesUpdateNativeActorAndRejectInvalidRequests(GameTestHelper helper) {
        var train = train(helper, state("createharmonics:andesite_jukebox"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        var at = world.global(net.minecraft.world.phys.Vec3.atCenterOf(BlockPos.ZERO));
        player.setPos(at.x, at.y, at.z);
        var initial = world.blockEntity(BlockPos.ZERO).saveWithFullMetadata(helper.getLevel().registryAccess());
        initial.putInt("PlaybackTestClock", 4321);
        world.updateData(BlockPos.ZERO, initial); world.flush(false);
        var info = world.contraption.getBlocks().get(BlockPos.ZERO);
        var context = new com.simibubi.create.content.contraptions.behaviour.MovementContext(helper.getLevel(), info, world.contraption);
        world.contraption.getActors().add(org.apache.commons.lang3.tuple.MutablePair.of(info, context));
        var behaviour = HarmonicsControls.behaviour(world, BlockPos.ZERO);
        int modes = behaviour.get().getDeclaringClass().getEnumConstants().length;
        for (int mode = 0; mode < modes; mode++) {
            helper.assertTrue(HarmonicsControls.configure(player, train, BlockPos.ZERO, mode), "Native playback mode must be accepted: " + mode);
            helper.assertTrue(context.blockEntityData.getInt("ScrollValue") == mode, "Audio actor must receive each mode");
            helper.assertTrue(context.blockEntityData.getInt("PlaybackTestClock") == 4321, "Changing mode must preserve other actor data");
        }
        helper.assertTrue(!HarmonicsControls.configure(player, train, BlockPos.ZERO, -1)
                && !HarmonicsControls.configure(player, train, BlockPos.ZERO, modes), "Invalid mode packets must be rejected");
        player.setPos(at.x + 100, at.y, at.z);
        helper.assertTrue(!HarmonicsControls.configure(player, train, BlockPos.ZERO, 0), "Players out of range must not reconfigure music");
        helper.succeed();
    }

    public static OrientedContraptionEntity train(GameTestHelper helper, BlockState state) {
        var c = new BearingContraption(false, Direction.EAST);
        c.anchor = BlockPos.ZERO; c.bounds = new AABB(-1, -2, -1, 6, 4, 2);
        c.getBlocks().put(BlockPos.ZERO, new StructureBlockInfo(BlockPos.ZERO, state, null));
        c.getStorage().initialize();
        var entity = OrientedContraptionEntity.create(helper.getLevel(), c, Direction.SOUTH);
        var at = helper.absolutePos(new BlockPos(1, 3, 1)); entity.setPos(at.getX(), at.getY(), at.getZ());
        return entity;
    }
    private static BlockState state(String id) { return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).defaultBlockState(); }
}
