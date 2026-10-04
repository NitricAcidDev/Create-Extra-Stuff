package com.nitricacid.traininteractive.verification;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.SignatureCocktailBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem;
import com.nitricacid.traininteractive.MovingTrainWorld;
import com.nitricacid.traininteractive.NativeServiceInteraction;
import com.nitricacid.traininteractive.TrainWorldAccess;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@GameTestHolder("create_trains_interactive")
@PrefixGameTestTemplate(false)
public final class TrainServiceTests {
    private static final BlockPos POS = new BlockPos(1, 2, 1);

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void trapdoorsUseCreateInteractionDespiteUniversalProvider(GameTestHelper helper) throws Exception {
        for (var block : java.util.List.of(Blocks.OAK_TRAPDOOR, Blocks.COPPER_TRAPDOOR,
                com.simibubi.create.AllBlocks.TRAIN_TRAPDOOR.get(), com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR.get())) {
            var handler = com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.REGISTRY.get(block);
            helper.assertTrue(handler instanceof com.simibubi.create.content.contraptions.behaviour.TrapdoorMovingInteraction,
                    "The universal provider must not mask Create's trapdoor handler: " + BuiltInRegistries.BLOCK.getKey(block));
            var train = assemble(helper, block.defaultBlockState());
            var player = new MenuPlayer(helper.getLevel());
            aim(player, train, BlockPos.ZERO);
            helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "A closed moving trapdoor must open");
            helper.assertTrue(train.getContraption().getBlocks().get(BlockPos.ZERO).state().getValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN), "Moving trapdoor must retain its open state");
            helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "An open moving trapdoor must close");
            helper.assertTrue(!train.getContraption().getBlocks().get(BlockPos.ZERO).state().getValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN), "Moving trapdoor must retain its closed state");
            if (net.neoforged.fml.ModList.get().isLoaded("createonthemove"))
                helper.assertTrue(!com.nitricacid.traininteractive.TrainEditingConfig.allows(block), "Trapdoor use must not make it breakable by default");
            train.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void nativePlacementSoundsReachPlacerAtTrainCoordinates(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:shaker"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        aim(player, train, BlockPos.ZERO);
        var packets = new java.util.ArrayList<net.minecraft.network.protocol.game.ClientboundSoundPacket>();
        player.setShiftKeyDown(true);
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(helper.getLevel().getServer(),
                new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND) {
                    @Override public void setListenerForServerboundHandshake(net.minecraft.network.PacketListener listener) {}
                }, player, net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false)) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if (packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound) packets.add(sound);
            }
        };
        var playerList = helper.getLevel().getServer().getPlayerList();
        var playersField = net.minecraft.server.players.PlayerList.class.getDeclaredField("players");
        playersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var players = (java.util.List<net.minecraft.server.level.ServerPlayer>) playersField.get(playerList);
        players.add(player);
        try {
            world.set(BlockPos.ZERO.below(), Blocks.STONE.defaultBlockState());
            for (var id : java.util.List.of("kaleidoscope_tavern:empty_bottle", "kaleidoscope_tavern:empty_glassware", "kaleidoscope_tavern:wine",
                    "kaleidoscope_world_liquor:absolut_vodka", "kaleidoscope_tavern:shaker", "farmersdelight:skillet")) {
                world.set(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(id)));
                packets.clear();
                var hit = new BlockHitResult(new Vec3(.5, 0, .5), Direction.UP, BlockPos.ZERO.below(), false);
                helper.assertTrue(world.run(hit.getBlockPos(), () -> NativeServiceInteraction.interact(player, InteractionHand.MAIN_HAND, hit, world)), "Native train placement must succeed: " + id);
                helper.assertTrue(packets.size() == 1, "The placing player must receive exactly one native placement sound: " + id + " received " + packets.size());
                var packet = packets.getFirst();
                var expected = world.global(Vec3.atCenterOf(BlockPos.ZERO));
                helper.assertTrue(new Vec3(packet.getX(), packet.getY(), packet.getZ()).distanceTo(expected) < .22, "Placement sound must follow the train's world coordinates: " + id);
                helper.assertTrue(packet.getSound().value() == world.state(BlockPos.ZERO).getSoundType().getPlaceSound(), "Placement must retain the item's native sound: " + id);
            }
        } finally {
            players.remove(player);
            train.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void playerOverflowDropsKeepWorldCoordinates(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:shaker"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        aim(player, train, BlockPos.ZERO);
        var dropped = world.run(BlockPos.ZERO, () -> player.drop(new ItemStack(Items.APPLE), false));
        helper.assertTrue(dropped != null && dropped.position().distanceTo(player.position()) < 2,
                "Inventory overflow must drop at the passenger's world position exactly once; player=" + player.position() + " drop=" + (dropped == null ? null : dropped.position()));
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingStoveCooksAndDropsFoodBesideTheTrain(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("farmersdelight:stove").setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        aim(player, train, BlockPos.ZERO);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "The train's stove must accept raw beef");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "The stove must consume only the inserted beef");
        for (int i = 0; i < 610; i++) world.tick();
        var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(train.position().add(-3, -3, -3), train.position().add(3, 3, 3)));
        helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(Items.COOKED_BEEF)), "Cooked food must drop beside the moving kitchen");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingFeastServesNativeFoodPortions(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("farmersdelight:roast_chicken_block"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        var feastPos = com.simibubi.create.api.contraption.BlockMovementChecks.isBrittle(state("farmersdelight:roast_chicken_block")) ? BlockPos.ZERO.above() : BlockPos.ZERO;
        aim(player, train, feastPos);
        var feast = (vectorwing.farmersdelight.common.block.FeastBlock) world.state(feastPos).getBlock();
        int portions = world.state(feastPos).getValue(feast.getServingsProperty());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        helper.assertTrue(train.handlePlayerInteraction(player, feastPos, Direction.SOUTH, InteractionHand.MAIN_HAND), "A bowl must take a meal from the train's feast");
        helper.assertTrue(world.state(feastPos).getValue(feast.getServingsProperty()) == portions - 1, "Serving must decrease exactly one portion");
        helper.assertTrue(!find(player, item("farmersdelight:roast_chicken")).isEmpty(), "Passenger must receive the native roast chicken serving");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingBarrelAcceptsIngredientsAndKeepsBrewing(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:barrel"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        long segments = train.getContraption().getBlocks().values().stream().filter(info -> info.state().is(state("kaleidoscope_tavern:barrel").getBlock())).count();
        helper.assertTrue(segments == 27, "All 27 segments of a native barrel must assemble together; captured " + segments);
        var player = new MenuPlayer(helper.getLevel());
        var controller = BlockPos.ZERO.east();
        var lid = controller.above(2);
        aim(player, train, lid);
        var barrel = (com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity) world.blockEntity(controller);
        world.run(controller, () -> { barrel.getFluid().fill(new net.neoforged.neoforge.fluids.FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("kaleidoscope_tavern:grape_juice")), 4000), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE); return null; });
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
        helper.assertTrue(train.handlePlayerInteraction(player, lid, Direction.SOUTH, InteractionHand.MAIN_HAND), "The moving barrel's lid must accept ingredients");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "Native barrel input must consume its ingredient");
        world.run(controller, () -> barrel.closeLid(player));
        int offset = barrel.hashCode() % 97 + 97;
        helper.runAfterDelay(97 - Math.floorMod(helper.getLevel().getGameTime() + offset, 97), () -> {
            world.tick();
            helper.assertTrue(barrel.getBrewLevel() > 0 && !barrel.getOutput().getStackInSlot(0).isEmpty(), "Closed barrel must start its native brandy recipe");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void pickBlockCopiesShakerDataWithoutRemovingIt(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:shaker"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var shaker = (ShakerBlockEntity) world.blockEntity(BlockPos.ZERO);
        var named = new ItemStack(Items.APPLE);
        named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Bar ingredient"));
        shaker.getStorage().setStackInSlot(0, named);
        world.flush(false);
        var player = new MenuPlayer(helper.getLevel());
        var hit = new BlockHitResult(new Vec3(.5, .5, .5), Direction.SOUTH, BlockPos.ZERO, false);
        var basic = com.nitricacid.traininteractive.TrainPickedItem.create(train, hit, player, false);
        helper.assertTrue(basic.is(item("kaleidoscope_tavern:shaker")), "Middle-click must pick the native shaker item");
        var copied = com.nitricacid.traininteractive.TrainPickedItem.create(train, hit, player, true);
        var data = copied.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        helper.assertTrue(data != null, "Creative Ctrl-pick must copy the native block entity data");
        var restored = new ShakerBlockEntity(BlockPos.ZERO, world.state(BlockPos.ZERO));
        restored.loadWithComponents(data.copyTag(), helper.getLevel().registryAccess());
        helper.assertTrue(restored.getStorage().getStackInSlot(0).getHoverName().getString().equals("Bar ingredient"), "Ctrl-pick must preserve ingredient components");
        helper.assertTrue(!world.state(BlockPos.ZERO).isAir() && shaker.getStorage().getStackInSlot(0).getCount() == 1,
                "Pick Block must leave the original block and ingredients untouched");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingFreezerAcceptsWaterProcessesAndServesAfterReload(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_world_liquor:freezer"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        aim(player, train, BlockPos.ZERO);
        player.setShiftKeyDown(true);
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "Shift-use must open the train's freezer");
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "The moving freezer must accept a water bucket");
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Filling the freezer must return the bucket");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        for (int i = 0; i < 25; i++) world.tick();
        var progress = world.blockEntity(BlockPos.ZERO).saveWithFullMetadata(helper.getLevel().registryAccess()).getInt("Progress");
        helper.assertTrue(progress > 0, "Closing a filled freezer must start its native ice recipe");
        var restored = Contraption.fromNBT(helper.getLevel(), train.getContraption().writeNBT(helper.getLevel().registryAccess(), false), false);
        var clone = OrientedContraptionEntity.create(helper.getLevel(), restored, Direction.SOUTH);
        clone.setPos(train.getX(), train.getY(), train.getZ());
        var restoredWorld = ((TrainWorldAccess) clone).trainsInteractive$world();
        for (int i = progress; i < 1800; i++) restoredWorld.tick();
        var tag = restoredWorld.blockEntity(BlockPos.ZERO).saveWithFullMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(tag.getInt("OutputCount") == 3, "Freezer must finish its recipe after train reload");
        aim(player, clone, BlockPos.ZERO);
        player.setShiftKeyDown(true);
        clone.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        for (int i = 0; i < 3; i++) clone.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND);
        helper.assertTrue(find(player, Items.ICE).getCount() == 3, "Passenger must collect all three native ice servings");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingGlasswareHolderKeepsNativeSlotInteractions(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:glassware_holder"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        var holder = (com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.GlasswareHolderBlockEntity) world.blockEntity(BlockPos.ZERO);
        for (int slot = 0; slot < 4; slot++) {
            var hit = new BlockHitResult(new Vec3((slot % 2 == 0 ? .25 : .75), .5, (slot < 2 ? .25 : .75)), Direction.UP, BlockPos.ZERO, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("kaleidoscope_tavern:empty_glassware")));
            helper.assertTrue(world.run(BlockPos.ZERO, () -> NativeServiceInteraction.interact(player, InteractionHand.MAIN_HAND, hit, world)), "Must insert into each holder quadrant");
            helper.assertTrue(holder.getItems().getStackInSlot(slot).getCount() == 1, "Only the clicked glassware slot must change");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            world.run(BlockPos.ZERO, () -> NativeServiceInteraction.interact(player, InteractionHand.MAIN_HAND, hit, world));
            helper.assertTrue(holder.getItems().getStackInSlot(slot).isEmpty() && player.getMainHandItem().is(item("kaleidoscope_tavern:empty_glassware")), "Empty hand must retrieve the selected glass");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingShakerIngredientsPickupMixPourAndServe(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:shaker"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        aim(player, train, BlockPos.ZERO);
        for (int i = 0; i < 3; i++) {
            var potion = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
            potion.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new PotionContents(java.util.Optional.empty(), java.util.Optional.empty(),
                    java.util.List.of(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 1200))));
            player.setItemInHand(InteractionHand.MAIN_HAND, potion);
            helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "Must add a potion to the moving shaker");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Exactly one ingredient must be consumed");
        }
        var shaker = (ShakerBlockEntity) world.blockEntity(BlockPos.ZERO);
        helper.assertTrue(shaker.getStorage().getStackInSlot(2).is(Items.POTION), "All three native ingredient slots must be populated");
        player.setItemInHand(InteractionHand.MAIN_HAND, PotionContents.createItemStack(Items.POTION, Potions.HEALING));
        train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "A full shaker must not consume a fourth ingredient");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "An empty hand must pick up the shaker");
        helper.assertTrue(world.state(BlockPos.ZERO).isAir(), "Pickup must remove the train's shaker block");
        ItemStack carried = find(player, item("kaleidoscope_tavern:shaker"));
        helper.assertTrue(!carried.isEmpty() && ShakerItem.getStorage(carried).getStackInSlot(2).is(Items.POTION), "Pickup must retain ingredients as native item components");
        var shakerItem = (ShakerItem) carried.getItem();
        shakerItem.releaseUsing(carried, helper.getLevel(), player, shakerItem.getUseDuration(carried, player) - 80);
        helper.assertTrue(ShakerItem.hasResult(carried), "Native in-hand shaking must produce a cocktail");
        world.set(BlockPos.ZERO, state("kaleidoscope_tavern:empty_glassware"));
        player.setItemInHand(InteractionHand.MAIN_HAND, carried);
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "Must pour a prepared shaker into moving glassware");
        helper.assertTrue(world.state(BlockPos.ZERO).is(BuiltInRegistries.BLOCK.get(ResourceLocation.parse("kaleidoscope_tavern:signature_cocktail"))), "The train's empty glass must become a cocktail");
        var cocktail = (SignatureCocktailBlockEntity) world.blockEntity(BlockPos.ZERO);
        helper.assertTrue(!cocktail.getEffects().isEmpty(), "Potion effects must survive native cocktail mixing");
        int color = cocktail.getColor();
        helper.assertTrue(!ShakerItem.hasResult(carried) && !ShakerItem.hasStorage(carried), "Pouring must clear the shaker's result and ingredients once");
        world.flush(true);
        var restored = Contraption.fromNBT(helper.getLevel(), train.getContraption().writeNBT(helper.getLevel().registryAccess(), false), false);
        var clone = OrientedContraptionEntity.create(helper.getLevel(), restored, Direction.SOUTH);
        clone.setPos(train.getX(), train.getY(), train.getZ());
        var restoredWorld = ((TrainWorldAccess) clone).trainsInteractive$world();
        var restoredCocktail = (SignatureCocktailBlockEntity) restoredWorld.blockEntity(BlockPos.ZERO);
        helper.assertTrue(restoredCocktail.getColor() == color && !restoredCocktail.getEffects().isEmpty(), "Cocktail effects and colour must survive a train reload");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.assertTrue(clone.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "Prepared cocktails must be picked up for serving");
        var served = find(player, item("kaleidoscope_tavern:signature_cocktail"));
        helper.assertTrue(SignatureCocktailBlockItem.getColor(served) == color && SignatureCocktailBlockItem.hasEffects(served), "Served drink must retain its colour and effects");
        helper.assertTrue(restoredWorld.state(BlockPos.ZERO).isAir(), "Serving removes exactly one drink block");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingShakerKeepsQualityRestrictionsAndReturnsContainers(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:shaker"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        aim(player, train, BlockPos.ZERO);
        var wine = new ItemStack(item("kaleidoscope_tavern:wine"));
        com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem.setBrewLevel(wine, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, wine);
        train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND);
        helper.assertTrue(wine.getCount() == 1, "Low quality wine must not be consumed");
        var shaker = (ShakerBlockEntity) world.blockEntity(BlockPos.ZERO);
        helper.assertTrue(shaker.getStorage().getStackInSlot(0).isEmpty(), "Low quality wine must not enter the shaker");
        com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem.setBrewLevel(wine,
                com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem.MIN_BREW_LEVEL_FOR_SHAKER);
        train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND);
        helper.assertTrue(!shaker.getStorage().getStackInSlot(0).isEmpty(), "Native quality-qualified wine must enter the shaker");
        helper.assertTrue(!find(player, item("kaleidoscope_tavern:empty_bottle")).isEmpty(), "Adding wine must return its native bottle container");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void nativeTrainCallsNeverModifyStationaryBlocks(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:empty_glassware"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var original = helper.getLevel().getBlockState(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(BlockPos.ZERO, Blocks.DIAMOND_BLOCK.defaultBlockState());
        try {
            world.run(BlockPos.ZERO, () -> {
                helper.assertTrue(helper.getLevel().getBlockState(BlockPos.ZERO).getBlock() == world.state(BlockPos.ZERO).getBlock(), "Native block reads must resolve the train");
                helper.getLevel().setBlockAndUpdate(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
                helper.assertTrue(!helper.getLevel().setBlockAndUpdate(new BlockPos(10000, 100, 10000), Blocks.GOLD_BLOCK.defaultBlockState()), "Out-of-train writes must be rejected");
                return null;
            });
            helper.assertTrue(helper.getLevel().getBlockState(BlockPos.ZERO).is(Blocks.DIAMOND_BLOCK), "Stationary world block must remain unchanged");
            helper.assertTrue(MovingTrainWorld.active(helper.getLevel()) == null, "Native routing must end after the action");
            try { world.run(BlockPos.ZERO, () -> { throw new IllegalStateException("scope-test"); }); }
            catch (IllegalStateException expected) { /* Context must still be restored. */ }
            helper.assertTrue(MovingTrainWorld.active(helper.getLevel()) == null, "Native routing must also end after an exception");
        } finally { helper.getLevel().setBlockAndUpdate(BlockPos.ZERO, original); }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingCookingPotCooksServesAndKeepsMenuValid(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("farmersdelight:cooking_pot"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        world.set(BlockPos.ZERO.below(), state("farmersdelight:stove").setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true));
        var pot = (CookingPotBlockEntity) world.blockEntity(BlockPos.ZERO);
        pot.getInventory().setStackInSlot(0, new ItemStack(item("farmersdelight:tomato")));
        pot.getInventory().setStackInSlot(1, new ItemStack(item("farmersdelight:tomato")));
        world.flush(true);
        for (int i = 0; i < 105; i++) world.tick();
        helper.assertTrue(pot.getMeal().is(item("farmersdelight:tomato_sauce")), "A pot heated by the train's stove must cook its native recipe");
        var player = new MenuPlayer(helper.getLevel());
        aim(player, train, BlockPos.ZERO);
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "Empty-handed use must open the cooking pot menu");
        helper.assertTrue(player.containerMenu.getClass().getSimpleName().equals("CookingPotMenu"), "Must use Farmer's Delight's native cooking menu");
        helper.assertTrue(player.containerMenu.stillValid(player), "Cooking menu must validate against a moving train");
        train.setPos(train.getX() + 20, train.getY(), train.getZ());
        aim(player, train, BlockPos.ZERO);
        helper.assertTrue(player.containerMenu.stillValid(player), "Menu must follow the train while its passenger moves with it");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "A bowl must take a serving from the moving pot");
        helper.assertTrue(!find(player, item("farmersdelight:tomato_sauce")).isEmpty(), "The serving must be delivered to the passenger");
        player.setPos(player.getX() + 30, player.getY(), player.getZ());
        helper.assertTrue(!player.containerMenu.stillValid(player), "Menu must close when the passenger leaves interaction range");
        com.nitricacid.traininteractive.MovingServiceMenus.closed(player);
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingCuttingBoardUsesNativeToolsAndDropsNearTrain(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("farmersdelight:cutting_board"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        var boardPos = BlockPos.ZERO.above();
        aim(player, train, boardPos);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.OAK_LOG));
        helper.assertTrue(train.handlePlayerInteraction(player, boardPos, Direction.SOUTH, InteractionHand.MAIN_HAND), "Place a log on the train's cutting board");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        helper.assertTrue(train.handlePlayerInteraction(player, boardPos, Direction.SOUTH, InteractionHand.MAIN_HAND), "Native axe stripping must work on a moving cutting board");
        var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(train.position().add(-3, -3, -3), train.position().add(3, 3, 3)));
        helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(Items.STRIPPED_OAK_LOG)), "Cutting result must drop beside the train, not at local coordinates");
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "The native tool must take normal durability damage");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingTapExtractsAfterDelayAndSurvivesReload(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:tap").setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        world.set(BlockPos.ZERO.south(), Blocks.WATER_CAULDRON.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL_CAULDRON, 3));
        world.set(BlockPos.ZERO.below(), state("kaleidoscope_tavern:empty_bottle"));
        var player = new MenuPlayer(helper.getLevel());
        aim(player, train, BlockPos.ZERO);
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "Tap must open on the train");
        helper.assertTrue(world.state(BlockPos.ZERO.below()).getBlock() == state("kaleidoscope_tavern:empty_bottle").getBlock(), "Extraction must retain the native delay");
        var restored = Contraption.fromNBT(helper.getLevel(), train.getContraption().writeNBT(helper.getLevel().registryAccess(), false), false);
        var clone = OrientedContraptionEntity.create(helper.getLevel(), restored, Direction.SOUTH);
        clone.setPos(train.getX(), train.getY(), train.getZ());
        var restoredWorld = ((TrainWorldAccess) clone).trainsInteractive$world();
        restoredWorld.tickAt(helper.getLevel().getGameTime() + 31);
        helper.assertTrue(restoredWorld.state(BlockPos.ZERO.below()).getBlock() == state("kaleidoscope_tavern:water_bottle").getBlock(), "Pending tap extraction must survive a train save/reload");
        helper.assertTrue(!restoredWorld.state(BlockPos.ZERO).getValue(com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock.OPEN), "Native tap must close after extraction");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void filledShakerPlacesAndKeepsDrinkOnTrainAndStationaryCounter(GameTestHelper helper) throws Exception {
        var train = assemble(helper, state("kaleidoscope_tavern:empty_glassware"));
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var player = new MenuPlayer(helper.getLevel());
        var shaker = new ItemStack(item("kaleidoscope_tavern:shaker"));
        var ingredients = new net.neoforged.neoforge.items.ItemStackHandler(3);
        ingredients.setStackInSlot(0, new ItemStack(Items.APPLE));
        ShakerItem.setStorage(shaker, ingredients);
        ShakerItem.setResult(shaker, new ItemStack(item("kaleidoscope_tavern:signature_cocktail")));
        var stationaryShaker = shaker.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, shaker);
        aim(player, train, BlockPos.ZERO);
        world.set(BlockPos.ZERO.below(), Blocks.STONE.defaultBlockState());
        world.set(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        var hit = new BlockHitResult(new Vec3(.5, 0, .5), Direction.UP, BlockPos.ZERO.below(), false);
        helper.assertTrue(world.run(hit.getBlockPos(), () -> NativeServiceInteraction.interact(player, InteractionHand.MAIN_HAND, hit, world)), "Filled shakers must place on train counters");
        helper.assertTrue(world.state(BlockPos.ZERO).getBlock() == state("kaleidoscope_tavern:shaker").getBlock(), "The filled shaker must become a train block");
        var placed = (ShakerBlockEntity) world.blockEntity(BlockPos.ZERO);
        helper.assertTrue(placed.getResult().is(item("kaleidoscope_tavern:signature_cocktail")) && placed.getStorage().getStackInSlot(0).is(Items.APPLE), "Train placement must preserve the prepared drink and ingredients");
        helper.assertTrue(shaker.isEmpty(), "Survival placement must consume the held shaker");
        world.flush(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        aim(player, train, BlockPos.ZERO);
        helper.assertTrue(train.handlePlayerInteraction(player, BlockPos.ZERO, Direction.SOUTH, InteractionHand.MAIN_HAND), "Placed shakers must be retrievable from trains");
        var retrieved = find(player, item("kaleidoscope_tavern:shaker"));
        helper.assertTrue(ShakerItem.hasResult(retrieved) && ShakerItem.getStorage(retrieved).getStackInSlot(0).is(Items.APPLE), "Pickup must retain the placed drink and ingredients");
        var counter = helper.absolutePos(POS.below());
        helper.getLevel().setBlockAndUpdate(counter, Blocks.STONE.defaultBlockState());
        var empty = counter.above();
        helper.getLevel().setBlockAndUpdate(empty, Blocks.AIR.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, stationaryShaker);
        var stationaryHit = new BlockHitResult(Vec3.atCenterOf(counter), Direction.UP, counter, false);
        stationaryShaker.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stationaryShaker, stationaryHit));
        helper.assertTrue(helper.getLevel().getBlockState(empty).getBlock() == state("kaleidoscope_tavern:shaker").getBlock(), "Filled shakers must also place on stationary counters");
        var stationary = (ShakerBlockEntity) helper.getLevel().getBlockEntity(empty);
        helper.assertTrue(stationary.getResult().is(item("kaleidoscope_tavern:signature_cocktail")) && stationary.getStorage().getStackInSlot(0).is(Items.APPLE), "Stationary placement must preserve the drink and ingredients");
        helper.succeed();
    }

    private static OrientedContraptionEntity assemble(GameTestHelper helper, BlockState state) throws Exception {
        helper.setBlock(POS.below(), Blocks.STONE);
        helper.setBlock(POS, state);
        if (state.getBlock() instanceof com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock barrel)
            barrel.setPlacedBy(helper.getLevel(), helper.absolutePos(POS), state, null, ItemStack.EMPTY);
        var c = new BearingContraption(false, Direction.EAST);
        boolean brittle = com.simibubi.create.api.contraption.BlockMovementChecks.isBrittle(state);
        var anchor = brittle ? POS.below() : state.getBlock() instanceof com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock ? POS.west() : POS;
        helper.assertTrue(c.assemble(helper.getLevel(), helper.absolutePos(anchor).west()), "Bar/kitchen block must assemble");
        helper.assertTrue(c.getBlocks().containsKey(brittle ? BlockPos.ZERO.above() : BlockPos.ZERO), "The bar/kitchen block must be captured");
        c.removeBlocksFromWorld(helper.getLevel(), BlockPos.ZERO);
        var train = OrientedContraptionEntity.create(helper.getLevel(), c, Direction.SOUTH);
        var at = helper.absolutePos(anchor);
        train.setPos(at.getX(), at.getY(), at.getZ());
        return train;
    }
    private static void aim(MenuPlayer player, OrientedContraptionEntity train, BlockPos pos) {
        var world = ((TrainWorldAccess) train).trainsInteractive$world();
        var target = world.run(pos, () -> world.state(pos).getShape(train.level(), pos).bounds().getCenter()).add(Vec3.atLowerCornerOf(pos));
        var eye = train.toGlobalVector(target.add(0, 0, 2), 1);
        player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
        var look = train.applyRotation(new Vec3(0, 0, -1), 1);
        player.setYRot((float) (Math.atan2(-look.x, look.z) * 180 / Math.PI)); player.setXRot(0);
    }
    private static BlockState state(String id) { return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).defaultBlockState(); }
    private static net.minecraft.world.item.Item item(String id) { return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)); }
    private static ItemStack find(MenuPlayer player, net.minecraft.world.item.Item item) {
        for (var stack : player.getInventory().items) if (stack.is(item)) return stack;
        return ItemStack.EMPTY;
    }
    private static final class MenuPlayer extends net.neoforged.neoforge.common.util.FakePlayer {
        MenuPlayer(net.minecraft.server.level.ServerLevel level) {
            super(level, new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "service-test"));
        }
        @Override public java.util.OptionalInt openMenu(net.minecraft.world.MenuProvider provider,
                java.util.function.Consumer<net.minecraft.network.RegistryFriendlyByteBuf> extraData) {
            if (provider == null) return java.util.OptionalInt.empty();
            var menu = provider.createMenu(1, getInventory(), this);
            if (menu == null) return java.util.OptionalInt.empty();
            containerMenu = menu;
            return java.util.OptionalInt.of(1);
        }
    }
}
