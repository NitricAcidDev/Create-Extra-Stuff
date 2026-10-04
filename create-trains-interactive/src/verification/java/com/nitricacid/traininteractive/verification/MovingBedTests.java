package com.nitricacid.traininteractive.verification;

import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("create_trains_interactive")
@PrefixGameTestTemplate(false)
public final class MovingBedTests {
    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void movingBedRejectsDayAndMonstersBeforeSleeping(GameTestHelper helper) {
        if (!net.neoforged.fml.ModList.get().isLoaded("createonthemove")) { helper.succeed(); return; }
        var level = helper.getLevel();
        long previousTime = level.getDayTime();
        var c = new BearingContraption(false, Direction.EAST);
        var bed = Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH);
        c.getBlocks().put(BlockPos.ZERO, new StructureBlockInfo(BlockPos.ZERO, bed.setValue(BedBlock.PART, BedPart.FOOT), null));
        c.getBlocks().put(BlockPos.ZERO.south(), new StructureBlockInfo(BlockPos.ZERO.south(), bed.setValue(BedBlock.PART, BedPart.HEAD), null));
        c.anchor = BlockPos.ZERO;
        c.bounds = new AABB(0, 0, 0, 1, 1, 2);
        c.getStorage().initialize();
        var train = OrientedContraptionEntity.create(level, c, Direction.SOUTH);
        var anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        train.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        level.addFreshEntity(train);
        var player = new BedPlayer(level);
        var global = train.toGlobalVector(Vec3.atCenterOf(BlockPos.ZERO), 1);
        player.setPos(global.x + 1, global.y, global.z);
        var interaction = com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.REGISTRY.get(bed);
        helper.assertTrue(interaction != null, "Moving beds must use their registered interaction handler");
        net.minecraft.world.entity.monster.Zombie zombie = null;
        try {
            level.setDayTime(6000);
            level.updateSkyBrightness();
            interaction.handlePlayerInteraction(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, train);
            helper.assertTrue(!player.isSleeping() && player.sleepEntries == 0, "Daytime rejection must happen before entering bed");
            helper.assertTrue(player.lastMessage != null && player.lastMessage.getString().equals(net.minecraft.world.entity.player.Player.BedSleepingProblem.NOT_POSSIBLE_NOW.getMessage().getString()), "Daytime must show the normal no-sleep message");
            helper.assertTrue(!net.woudlee.createonthemove.contraption.ContraptionBedHandler.isSleepingOnContraption(player), "Rejected sleep must leave no moving-bed tracking");
            level.setDayTime(18000);
            level.updateSkyBrightness();
            zombie = EntityType.ZOMBIE.create(level);
            zombie.setPos(global.x + 2, global.y, global.z);
            level.addFreshEntity(zombie);
            interaction.handlePlayerInteraction(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, train);
            helper.assertTrue(!player.isSleeping() && player.sleepEntries == 0, "Nearby monsters must be rejected before entering bed");
            helper.assertTrue(player.lastMessage.getString().equals(net.minecraft.world.entity.player.Player.BedSleepingProblem.NOT_SAFE.getMessage().getString()), "Monsters must show the normal not-safe message");
            zombie.discard(); zombie = null;
            interaction.handlePlayerInteraction(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, train);
            helper.assertTrue(player.isSleeping() && player.sleepEntries == 1, "A safe moving bed must still allow sleeping at night");
            helper.assertTrue(net.woudlee.createonthemove.contraption.ContraptionBedHandler.isSleepingOnContraption(player), "Successful sleep must retain moving-bed tracking");
            helper.assertTrue(com.nitricacid.traininteractive.MovingBedSleep.vanillaResult(player, "outside") instanceof String, "The validation context must be restored after interacting");
            helper.succeed();
        } finally {
            if (zombie != null) zombie.discard();
            net.woudlee.createonthemove.contraption.ContraptionBedHandler.stopSleeping(player);
            if (player.isSleeping()) player.stopSleeping();
            train.discard();
            level.setDayTime(previousTime);
            level.updateSkyBrightness();
        }
    }
    private static final class BedPlayer extends net.neoforged.neoforge.common.util.FakePlayer {
        int sleepEntries;
        Component lastMessage;
        BedPlayer(net.minecraft.server.level.ServerLevel level) {
            super(level, new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "moving-bed-test"));
        }
        @Override public void startSleeping(BlockPos pos) { sleepEntries++; super.startSleeping(pos); }
        @Override public void displayClientMessage(Component message, boolean actionBar) { lastMessage = message; }
    }
}
