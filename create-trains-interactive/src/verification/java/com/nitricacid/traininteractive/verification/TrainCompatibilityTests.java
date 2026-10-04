package com.nitricacid.traininteractive.verification;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.nitricacid.traininteractive.CabinetDataPacket;
import com.nitricacid.traininteractive.FurnitureSeats;
import com.nitricacid.traininteractive.TavernCompat;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("create_trains_interactive")
@PrefixGameTestTemplate(false)
public final class TrainCompatibilityTests {
    private static final BlockPos BLOCK = new BlockPos(1, 2, 1);

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void onTheMoveCoexistsWithRailwaysAndBookshelves(GameTestHelper helper) {
        if (net.neoforged.fml.ModList.get().isLoaded("createonthemove")) {
            var registry = MovingInteractionBehaviour.REGISTRY;
            helper.assertTrue(registry.get(net.minecraft.world.level.block.Blocks.CHISELED_BOOKSHELF)
                    instanceof com.nitricacid.traininteractive.ChiseledBookshelfInteraction,
                    "On the Move must preserve bookshelf interactions and Inspector synchronization");
            helper.assertTrue(registry.get(net.minecraft.world.level.block.Blocks.FURNACE).getClass().getName()
                    .equals("net.woudlee.createonthemove.interaction.FurnaceInteractionBehaviour"),
                    "The universal provider must not suppress On the Move's specialized furnace handler");
            helper.assertTrue(registry.get(net.minecraft.world.level.block.Blocks.ANVIL).getClass().getName()
                    .equals("net.woudlee.createonthemove.interaction.AnvilInteractionBehaviour"),
                    "Unclaimed workstations must retain On the Move interactions");
            if (net.neoforged.fml.ModList.get().isLoaded("railways")) {
                for (var block : java.util.List.of(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE,
                        net.minecraft.world.level.block.Blocks.STONECUTTER,
                        net.minecraft.world.level.block.Blocks.CARTOGRAPHY_TABLE,
                        net.minecraft.world.level.block.Blocks.LOOM)) {
                    helper.assertTrue(registry.get(block).getClass().getName().startsWith("com.railwayteam.railways."),
                            "Steam 'n' Rails must retain its existing workstation handler: " + block);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void onTheMoveGuardPreservesExplicitHandlersWithoutSuppressingProviders(GameTestHelper helper) {
        var registry = com.simibubi.create.api.registry.SimpleRegistry.<Object, Object>create();
        Object existingKey = new Object(), freshKey = new Object();
        Object existing = new Object(), universal = new Object(), specialized = new Object();
        registry.register(existingKey, existing);
        registry.registerProvider(key -> universal);
        // Prime provider resolution before installing the more specific direct registration.
        helper.assertTrue(registry.get(freshKey) == universal, "Provider must resolve before registration");
        com.nitricacid.traininteractive.OnTheMoveCompat.registerIfUnclaimed(registry, existingKey, specialized);
        com.nitricacid.traininteractive.OnTheMoveCompat.registerIfUnclaimed(registry, freshKey, specialized);
        helper.assertTrue(registry.get(existingKey) == existing, "Existing explicit handler must remain unchanged");
        helper.assertTrue(registry.get(freshKey) == specialized, "Specialized handler must override provider fallback");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void farmersCabinetSurvivesMountedInventoryChanges(GameTestHelper helper) throws Exception {
        roundTripInventory(helper, "farmersdelight:oak_cabinet");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void bothFarmersBasketsSurviveMountedInventoryChanges(GameTestHelper helper) throws Exception {
        roundTripInventory(helper, "farmersdelight:wooden_basket");
        roundTripInventory(helper, "farmersdelight:bamboo_basket");
        helper.succeed();
    }

    private static void roundTripInventory(GameTestHelper helper, String id) throws Exception {
        helper.setBlock(BLOCK, block(id).defaultBlockState());
        BaseContainerBlockEntity be = (BaseContainerBlockEntity) helper.getBlockEntity(BLOCK);
        be.setItem(0, new ItemStack(Items.DIAMOND, 7));
        var entity = assemble(helper);
        var contraption = entity.getContraption();
        var items = contraption.getStorage().getMountedItems();
        helper.assertTrue(items.getSlots() == be.getContainerSize(), "Every inventory slot must be mounted: " + id);
        var player = new MenuTestPlayer(helper.getLevel());
        Vec3 menuPos = entity.toGlobalVector(Vec3.atCenterOf(BlockPos.ZERO), 0);
        player.setPos(menuPos.x, menuPos.y, menuPos.z);
        helper.assertTrue(entity.handlePlayerInteraction(player, BlockPos.ZERO, Direction.NORTH, InteractionHand.MAIN_HAND), "Must open a moving storage menu: " + id);
        helper.assertTrue(player.containerMenu.slots.size() == be.getContainerSize() + 36, "Moving menu must expose exactly the real inventory size");
        helper.assertTrue(player.containerMenu.stillValid(player), "Menu must remain usable near the moving storage");
        if (id.endsWith("oak_cabinet")) {
            var title = (net.minecraft.network.chat.contents.TranslatableContents) player.openedTitle.getContents();
            var name = (net.minecraft.network.chat.Component) title.getArgs()[0];
            helper.assertTrue(((net.minecraft.network.chat.contents.TranslatableContents) name.getContents()).getKey().equals("container.farmersdelight.cabinet"), "Moving cabinet must use Farmer's Delight's current translation key");
        }
        player.closeContainer();
        helper.assertTrue(items.extractItem(0, 3, false).getCount() == 3, "Must extract from mounted storage");
        helper.assertTrue(items.insertItem(1, new ItemStack(Items.EMERALD, 5), false).isEmpty(), "Must insert into mounted storage");
        var saved = contraption.writeNBT(helper.getLevel().registryAccess(), false);
        Contraption loaded = Contraption.fromNBT(helper.getLevel(), saved, false);
        loaded.addBlocksToWorld(helper.getLevel(), new StructureTransform(helper.absolutePos(BLOCK), 0, 0, 0));
        BaseContainerBlockEntity restored = (BaseContainerBlockEntity) helper.getBlockEntity(BLOCK);
        helper.assertTrue(restored.getItem(0).is(Items.DIAMOND) && restored.getItem(0).getCount() == 4, "Must restore changed diamond count");
        helper.assertTrue(restored.getItem(1).is(Items.EMERALD) && restored.getItem(1).getCount() == 5, "Must restore newly inserted items");
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void allFurnitureColoursBecomePersistentSeats(GameTestHelper helper) throws Exception {
        int count = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            if (FurnitureSeats.height(block.defaultBlockState()).isEmpty()) continue;
            helper.setBlock(BLOCK, block.defaultBlockState());
            var entity = assemble(helper);
            var contraption = entity.getContraption();
            helper.assertTrue(contraption.getSeats().size() == 1, "Exactly one seat for " + block);
            var chicken = EntityType.CHICKEN.create(helper.getLevel());
            entity.addSittingPassenger(chicken, 0);
            helper.assertTrue(chicken.getVehicle() == entity, "Passenger must ride the contraption");
            Vec3 seated = entity.getPassengerPosition(chicken, 1);
            helper.assertTrue(seated != null && Double.isFinite(seated.y), "Seat must have a valid passenger position");
            Contraption loaded = Contraption.fromNBT(helper.getLevel(), contraption.writeNBT(helper.getLevel().registryAccess(), false), false);
            helper.assertTrue(loaded.getSeats().size() == 1, "Seats must survive world reload");
            chicken.stopRiding();
            entity.discard();
            count++;
        }
        helper.assertTrue(count == 48, "Must cover 16 sofas, 16 Tavern stools and 16 World Liquor chairs; got " + count);
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void bottleCabinetPreservesRulesAndChangesThroughReload(GameTestHelper helper) throws Exception {
        helper.setBlock(BLOCK, block("kaleidoscope_tavern:bar_cabinet").defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        var entity = assemble(helper);
        var info = entity.getContraption().getBlocks().get(BlockPos.ZERO);
        var cabinet = new BarCabinetBlockEntity(BlockPos.ZERO, info.state());
        cabinet.loadWithComponents(info.nbt(), helper.getLevel().registryAccess());
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        var hit = new BlockHitResult(new Vec3(0.75, 0.5, 0), Direction.NORTH, BlockPos.ZERO, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT));
        helper.assertTrue(!TavernCompat.CabinetInteraction.interact(player, info, cabinet, hit), "Cabinet must reject non-bottles");
        var bottle = bottle();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bottle, 2));
        helper.assertTrue(TavernCompat.CabinetInteraction.interact(player, info, cabinet, hit), "Cabinet must accept a drink");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "Insertion must consume one bottle");
        var tag = cabinet.saveWithFullMetadata(helper.getLevel().registryAccess());
        CabinetDataPacket.apply(entity, BlockPos.ZERO, tag);
        Contraption loaded = Contraption.fromNBT(helper.getLevel(), entity.getContraption().writeNBT(helper.getLevel().registryAccess(), false), false);
        loaded.addBlocksToWorld(helper.getLevel(), new StructureTransform(helper.absolutePos(BLOCK), 0, 0, 0));
        var restored = (BarCabinetBlockEntity) helper.getBlockEntity(BLOCK);
        helper.assertTrue(ItemStack.matches(restored.getLeftItem(), cabinet.getLeftItem()), "Displayed bottles must survive disassembly and reload");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.assertTrue(TavernCompat.CabinetInteraction.interact(player, info, restored, hit), "Empty hand must retrieve bottle");
        helper.assertTrue(player.getMainHandItem().is(bottle), "Must return the same drink");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void worldLiquorExistingCabinetHandlersRemainActive(GameTestHelper helper) {
        for (String wood : new String[]{"oak", "spruce", "birch", "dark_oak", "cherry"}) {
            for (String kind : new String[]{"bar_cabinet", "cellar_cabinet"}) {
                var handler = MovingInteractionBehaviour.REGISTRY.get(block("kaleidoscope_world_liquor:" + wood + "_" + kind));
                helper.assertTrue(handler != null && handler.getClass().getName().startsWith("com.bmt."), "Existing World Liquor interactions must remain active");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void tavernRacksRespectSlotsAndBottleLimits(GameTestHelper helper) throws Exception {
        for (String id : new String[]{"cellar_cabinet", "holder", "circular_rack", "tilted_rack"}) {
            helper.setBlock(BLOCK, block("kaleidoscope_tavern:" + id).defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
            var entity = assemble(helper);
            var info = entity.getContraption().getBlocks().get(BlockPos.ZERO);
            var be = (StorageBlockEntity)((net.minecraft.world.level.block.EntityBlock)info.state().getBlock()).newBlockEntity(BlockPos.ZERO, info.state());
            be.loadWithComponents(info.nbt(), helper.getLevel().registryAccess());
            var player = FakePlayerFactory.getMinecraft(helper.getLevel());
            var hit = new BlockHitResult(new Vec3(0.5, 0.5, 0), Direction.NORTH, BlockPos.ZERO, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bottle(), 3));
            helper.assertTrue(TavernCompat.CabinetInteraction.interact(player, info, be, hit), "Rack must accept a regular bottle: " + id);
            helper.assertTrue(player.getMainHandItem().getCount() == 2, "Rack must consume only one bottle");
            helper.assertTrue(!TavernCompat.CabinetInteraction.interact(player, info, be, hit), "Occupied rack slot must reject another bottle");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.assertTrue(TavernCompat.CabinetInteraction.interact(player, info, be, hit), "Empty hand must retrieve the stored bottle");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void passengerHeightMatchesStationaryFurniture(GameTestHelper helper) throws Exception {
        String[] blocks = {"kaleidoscope_tavern:white_sofa", "kaleidoscope_tavern:white_bar_stool", "kaleidoscope_world_liquor:bar_stool_white"};
        double[] nativeHeights = {0.5125, 0.875, 0.9};
        for (int i = 0; i < blocks.length; i++) {
            helper.setBlock(BLOCK, block(blocks[i]).defaultBlockState());
            var entity = assemble(helper);
            var player = FakePlayerFactory.getMinecraft(helper.getLevel());
            entity.getContraption().getSeatMapping().put(player.getUUID(), 0);
            var nativeSeat = new com.github.ysbbbbbb.kaleidoscopetavern.entity.SitEntity(helper.getLevel(), helper.absolutePos(BLOCK), nativeHeights[i]);
            double expected = nativeSeat.getPassengerRidingPosition(player).y - player.getVehicleAttachmentPoint(nativeSeat).y;
            helper.assertTrue(Math.abs(entity.getPassengerPosition(player, 1).y - expected) < 0.00001, "Moving seat must match its native furniture height: " + blocks[i]);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void assembledCabinetsHandleActualClicks(GameTestHelper helper) throws Exception {
        for (String kind : new String[]{"bar_cabinet", "glass_bar_cabinet"}) {
            helper.setBlock(BLOCK, block("kaleidoscope_tavern:" + kind).defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
            var entity = assemble(helper);
            var player = new MenuTestPlayer(helper.getLevel());
            Vec3 eye = entity.toGlobalVector(new Vec3(0.75, 0.5, -2), 1);
            player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
            player.setYRot(0); player.setXRot(0);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bottle(), 2));
            helper.assertTrue(entity.handlePlayerInteraction(player, BlockPos.ZERO, Direction.NORTH, InteractionHand.MAIN_HAND), "Actual contraption click must place a bottle in " + kind);
            helper.assertTrue(player.getMainHandItem().getCount() == 1, "Actual click must consume exactly one bottle");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.assertTrue(entity.handlePlayerInteraction(player, BlockPos.ZERO, Direction.NORTH, InteractionHand.MAIN_HAND), "Actual contraption click must retrieve the bottle");
            helper.assertTrue(player.getMainHandItem().is(bottle()), "Retrieved bottle must match the inserted drink");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void chiseledBookshelvesKeepBooksAndClickedSlots(GameTestHelper helper) throws Exception {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            helper.setBlock(BLOCK, net.minecraft.world.level.block.Blocks.CHISELED_BOOKSHELF.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
            ((net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity) helper.getBlockEntity(BLOCK)).clearContent();
            var entity = assemble(helper);
            var player = new MenuTestPlayer(helper.getLevel());
            for (int slot = 0; slot < 6; slot++) {
                aimAtBookshelfSlot(player, entity, facing, slot);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
                helper.assertTrue(!entity.handlePlayerInteraction(player, BlockPos.ZERO, facing, InteractionHand.MAIN_HAND), "Shelves must reject non-books: " + facing + " " + slot);
                var book = new ItemStack(Items.ENCHANTED_BOOK, 2);
                book.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Book " + slot));
                player.setItemInHand(InteractionHand.MAIN_HAND, book);
                helper.assertTrue(entity.handlePlayerInteraction(player, BlockPos.ZERO, facing, InteractionHand.MAIN_HAND), "Each front slot must accept a book: " + facing + " " + slot);
                helper.assertTrue(book.getCount() == 1, "Insertion must consume exactly one book");
                var info = entity.getContraption().getBlocks().get(BlockPos.ZERO);
                helper.assertTrue(info.state().getValue(net.minecraft.world.level.block.ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(slot)), "Inserted slot must render occupied");
            }
            aimAtBookshelfSlot(player, entity, facing.getOpposite(), 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOOK));
            helper.assertTrue(!entity.handlePlayerInteraction(player, BlockPos.ZERO, facing.getOpposite(), InteractionHand.MAIN_HAND), "Shelf back must not accept clicks");
            aimAtBookshelfSlot(player, entity, facing, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.assertTrue(entity.handlePlayerInteraction(player, BlockPos.ZERO, facing, InteractionHand.MAIN_HAND), "Occupied slot must return its book");
            helper.assertTrue(player.getInventory().items.stream().anyMatch(s -> s.is(Items.ENCHANTED_BOOK) && "Book 2".equals(s.getHoverName().getString())), "Retrieved book must preserve its components");
            var saved = entity.getContraption().writeNBT(helper.getLevel().registryAccess(), false);
            var loaded = Contraption.fromNBT(helper.getLevel(), saved, false);
            loaded.addBlocksToWorld(helper.getLevel(), new StructureTransform(helper.absolutePos(BLOCK), 0, 0, 0));
            var shelf = (net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity) helper.getBlockEntity(BLOCK);
            helper.assertTrue(shelf.count() == 5 && shelf.getItem(2).isEmpty(), "Changed books must survive reload and disassembly");
            helper.assertTrue(shelf.getLastInteractedSlot() == 2, "Last clicked slot must survive disassembly");
            for (int slot = 0; slot < 6; slot++) {
                if (slot != 2) helper.assertTrue(shelf.getItem(slot).getHoverName().getString().equals("Book " + slot), "Each book must remain in its clicked slot");
                helper.assertTrue(shelf.getBlockState().getValue(net.minecraft.world.level.block.ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(slot)) == (slot != 2), "Restored shelf must render the right occupied slots");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void inspectorReceivesBooksFromNewAndReloadedTrains(GameTestHelper helper) throws Exception {
        helper.setBlock(BLOCK, net.minecraft.world.level.block.Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        var shelf = (net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity) helper.getBlockEntity(BLOCK);
        var named = new ItemStack(Items.BOOK);
        named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Train library"));
        var enchanted = new ItemStack(Items.ENCHANTED_BOOK);
        var sharpness = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
        enchanted.enchant(sharpness, 3);
        var written = new ItemStack(Items.WRITTEN_BOOK);
        written.set(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT, new net.minecraft.world.item.component.WrittenBookContent(net.minecraft.server.network.Filterable.passThrough("Journey"), "Train author", 0, java.util.List.of(net.minecraft.server.network.Filterable.passThrough(net.minecraft.network.chat.Component.literal("Page"))), true));
        shelf.setItem(0, named); shelf.setItem(1, enchanted); shelf.setItem(2, written);
        var entity = assemble(helper);
        var contraption = entity.getContraption();
        for (int pass = 0; pass < 2; pass++) {
            if (pass == 1) {
                var saved = contraption.writeNBT(helper.getLevel().registryAccess(), false);
                contraption = Contraption.fromNBT(helper.getLevel(), saved, false);
                ((net.dadamalda.create_compatible_storage.mixin.ContraptionUpdateTagsAccess) contraption).trainsInteractive$updateTags().put(BlockPos.ZERO, new net.minecraft.nbt.CompoundTag());
            }
            var sent = contraption.writeNBT(helper.getLevel().registryAccess(), true);
            var received = Contraption.fromNBT(helper.getLevel(), sent, true);
            var info = received.getBlocks().get(BlockPos.ZERO);
            helper.assertTrue(ItemStack.isSameItemSameComponents(named, com.nitricacid.traininteractive.MovingBookshelfContents.read(info, helper.getLevel().registryAccess(), 0)), "Initial/late tracking must send custom book names");
            var receivedEnchanted = com.nitricacid.traininteractive.MovingBookshelfContents.read(info, helper.getLevel().registryAccess(), 1);
            helper.assertTrue(receivedEnchanted.get(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS).getLevel(sharpness) == 3, "Inspector must receive enchantments and their levels");
            helper.assertTrue(com.nitricacid.traininteractive.MovingBookshelfContents.read(info, helper.getLevel().registryAccess(), 2).get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).author().equals("Train author"), "Inspector must receive written book authors");
            helper.assertTrue(com.nitricacid.traininteractive.MovingBookshelfContents.read(info, helper.getLevel().registryAccess(), 5).isEmpty(), "Empty slots must not show stale books");
            var clientEntity = OrientedContraptionEntity.create(helper.getLevel(), received, Direction.SOUTH);
            var changed = info.nbt().copy();
            var books = net.minecraft.core.NonNullList.withSize(6, ItemStack.EMPTY);
            net.minecraft.world.ContainerHelper.loadAllItems(changed, books, helper.getLevel().registryAccess());
            books.set(0, ItemStack.EMPTY);
            net.minecraft.world.ContainerHelper.saveAllItems(changed, books, true, helper.getLevel().registryAccess());
            CabinetDataPacket.apply(clientEntity, BlockPos.ZERO, changed);
            helper.assertTrue(com.nitricacid.traininteractive.MovingBookshelfContents.read(clientEntity.getContraption().getBlocks().get(BlockPos.ZERO), helper.getLevel().registryAccess(), 0).isEmpty(), "Live shelf updates must clear removed books");
        }
        helper.succeed();
    }

    private static void aimAtBookshelfSlot(MenuTestPlayer player, OrientedContraptionEntity entity, Direction facing, int slot) {
        double u = new double[]{0.1875, 0.5, 0.84375}[slot % 3];
        double y = slot < 3 ? 0.75 : 0.25;
        Vec3 target = switch (facing) {
            case NORTH -> new Vec3(1 - u, y, 0);
            case SOUTH -> new Vec3(u, y, 1);
            case WEST -> new Vec3(0, y, u);
            case EAST -> new Vec3(1, y, 1 - u);
            default -> throw new IllegalArgumentException();
        };
        Vec3 eye = entity.toGlobalVector(target.add(Vec3.atLowerCornerOf(facing.getNormal()).scale(2)), 1);
        player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
        player.setYRot(facing.getOpposite().toYRot()); player.setXRot(0);
    }

    private static OrientedContraptionEntity assemble(GameTestHelper helper) throws Exception {
        var contraption = new BearingContraption(false, Direction.EAST);
        helper.assertTrue(contraption.assemble(helper.getLevel(), helper.absolutePos(BLOCK).west()), "Block must assemble");
        contraption.removeBlocksFromWorld(helper.getLevel(), BlockPos.ZERO);
        var entity = OrientedContraptionEntity.create(helper.getLevel(), contraption, Direction.SOUTH);
        entity.setPos(helper.absolutePos(BLOCK).getX(), helper.absolutePos(BLOCK).getY(), helper.absolutePos(BLOCK).getZ());
        return entity;
    }

    private static Block block(String id) {
        var key = ResourceLocation.parse(id);
        if (!BuiltInRegistries.BLOCK.containsKey(key)) throw new AssertionError("Missing installed mod block " + id);
        return BuiltInRegistries.BLOCK.get(key);
    }

    private static net.minecraft.world.item.Item bottle() {
        for (var item : BuiltInRegistries.ITEM) {
            if (item instanceof com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem) return item;
        }
        throw new AssertionError("No Tavern drink registered");
    }

    // NeoForge FakePlayer normally refuses menus. Exercise real menu construction without a client handshake.
    private static final class MenuTestPlayer extends net.neoforged.neoforge.common.util.FakePlayer {
        net.minecraft.network.chat.Component openedTitle;
        MenuTestPlayer(net.minecraft.server.level.ServerLevel level) {
            super(level, new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "storage-test"));
        }
        @Override
        public java.util.OptionalInt openMenu(net.minecraft.world.MenuProvider provider,
                java.util.function.Consumer<net.minecraft.network.RegistryFriendlyByteBuf> extraData) {
            if (provider == null) return java.util.OptionalInt.empty();
            openedTitle = provider.getDisplayName();
            var menu = provider.createMenu(1, getInventory(), this);
            if (menu == null) return java.util.OptionalInt.empty();
            containerMenu = menu;
            return java.util.OptionalInt.of(1);
        }
    }
}
