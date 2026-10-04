package com.nitricacid.traininteractive.verification;

import com.nitricacid.traininteractive.TrainEditingConfig;
import com.nitricacid.traininteractive.TrainWorldAccess;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("create_trains_interactive")
@PrefixGameTestTemplate(false)
public final class TrainEditingTests {
    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void serverWhitelistBlocksStructureEditsAndKeepsFilledShakers(GameTestHelper helper) {
        if (!net.neoforged.fml.ModList.get().isLoaded("createonthemove")) { helper.succeed(); return; }
        helper.assertTrue(TrainEditingConfig.allows(Blocks.SMOKER) && !TrainEditingConfig.allows(Blocks.STONE), "The default whitelist must allow cooking and protect structural blocks");
        helper.assertTrue(TrainEditingConfig.matches(ResourceLocation.parse("example:chair"), java.util.List.of("example:*"))
                && !TrainEditingConfig.matches(ResourceLocation.parse("minecraft:stone"), java.util.List.of("example:*")), "Namespace whitelist entries must match only their own mod");
        var c = new BearingContraption(false, Direction.EAST);
        c.getBlocks().put(BlockPos.ZERO, new StructureBlockInfo(BlockPos.ZERO, Blocks.STONE.defaultBlockState(), null));
        c.getBlocks().put(BlockPos.ZERO.south(), new StructureBlockInfo(BlockPos.ZERO.south(), Blocks.SMOKER.defaultBlockState(), null));
        c.anchor = BlockPos.ZERO; c.bounds = new AABB(0, 0, 0, 1, 1, 2); c.getStorage().initialize();
        var train = OrientedContraptionEntity.create(helper.getLevel(), c, Direction.SOUTH);
        var at = helper.absolutePos(new BlockPos(1, 2, 1));
        train.setPos(at.getX(), at.getY(), at.getZ());
        helper.getLevel().addFreshEntity(train);
        var player = new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "train-edit-test"));
        var near = train.toGlobalVector(new Vec3(.5, 1, 2), 1);
        player.setPos(near.x, near.y, near.z);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE, 3));
        helper.assertTrue(!net.woudlee.createonthemove.contraption.ContraptionBlockPlacer.tryPlaceBlock(player, train, BlockPos.ZERO, Direction.UP, InteractionHand.MAIN_HAND), "Server placement must reject a non-whitelisted block");
        helper.assertTrue(!c.getBlocks().containsKey(BlockPos.ZERO.above()) && player.getMainHandItem().getCount() == 3, "Rejected placement must not change the train or consume items");
        player.getAbilities().instabuild = true;
        net.woudlee.createonthemove.contraption.ContraptionBlockBreaker.breakBlock(player, train.getId(), BlockPos.ZERO);
        helper.assertTrue(c.getBlocks().containsKey(BlockPos.ZERO), "Creative mode must also be unable to break non-whitelisted structural blocks");
        net.woudlee.createonthemove.contraption.ContraptionBlockBreaker.breakBlock(player, train.getId(), BlockPos.ZERO.south());
        helper.assertTrue(!c.getBlocks().containsKey(BlockPos.ZERO.south()), "Whitelisted cooking blocks must remain breakable");
        player.getAbilities().instabuild = false;
        var shaker = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("kaleidoscope_tavern:shaker")));
        var ingredients = new net.neoforged.neoforge.items.ItemStackHandler(3);
        ingredients.setStackInSlot(0, new ItemStack(Items.APPLE));
        com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem.setStorage(shaker, ingredients);
        com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem.setResult(shaker, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("kaleidoscope_tavern:signature_cocktail"))));
        player.setItemInHand(InteractionHand.MAIN_HAND, shaker);
        helper.assertTrue(net.woudlee.createonthemove.contraption.ContraptionBlockPlacer.tryPlaceBlock(player, train, BlockPos.ZERO, Direction.UP, InteractionHand.MAIN_HAND), "On the Move's placement path must accept filled shakers");
        var be = (com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity) ((TrainWorldAccess) train).trainsInteractive$world().blockEntity(BlockPos.ZERO.above());
        helper.assertTrue(!be.getResult().isEmpty() && be.getStorage().getStackInSlot(0).is(Items.APPLE), "On the Move placement must retain the drink and ingredients");
        helper.assertTrue(shaker.isEmpty(), "Survival placement must consume exactly the placed shaker");
        var allowedBefore = java.util.List.copyOf(TrainEditingConfig.ALLOWED_BLOCKS.get());
        boolean enabledBefore = TrainEditingConfig.ENABLED.get();
        try {
            TrainEditingConfig.ALLOWED_BLOCKS.set(java.util.List.of("minecraft:stone"));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE, 2));
            helper.assertTrue(net.woudlee.createonthemove.contraption.ContraptionBlockPlacer.tryPlaceBlock(player, train, BlockPos.ZERO, Direction.WEST, InteractionHand.MAIN_HAND), "The server's customized whitelist must control actual placement");
            helper.assertTrue(c.getBlocks().containsKey(BlockPos.ZERO.west()) && player.getMainHandItem().getCount() == 1, "Allowed custom placement must consume one item");
            TrainEditingConfig.ALLOWED_BLOCKS.set(java.util.List.of());
            helper.assertTrue(!TrainEditingConfig.allows(Blocks.STONE), "An empty enabled whitelist must reject every block");
            TrainEditingConfig.ENABLED.set(false);
            helper.assertTrue(TrainEditingConfig.allows(Blocks.STONE), "Servers must be able to disable the restriction");
        } finally {
            TrainEditingConfig.ALLOWED_BLOCKS.set(allowedBefore);
            TrainEditingConfig.ENABLED.set(enabledBefore);
        }
        train.discard();
        helper.succeed();
    }
}
