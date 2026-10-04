package com.nitricacid.traininteractive.verification;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket;
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
public final class TrainHandUseTests {
    @GameTest(template = "empty", templateNamespace = "create_trains_interactive")
    public static void failedTrainActionOpensBookAndUsesFoodWithoutEditing(GameTestHelper helper) {
        var c = new BearingContraption(false, Direction.EAST);
        c.anchor = BlockPos.ZERO;
        c.bounds = new AABB(0, 0, 0, 1, 1, 1);
        c.getStorage().initialize();
        var train = OrientedContraptionEntity.create(helper.getLevel(), c, Direction.SOUTH);
        var at = helper.absolutePos(new BlockPos(1, 2, 1));
        train.setPos(at.getX(), at.getY(), at.getZ());
        helper.getLevel().addFreshEntity(train);
        var opens = new int[1];
        var player = new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "book-train-test")) {
            @Override public void openItemGui(ItemStack stack, InteractionHand hand) { opens[0]++; }
        };
        var target = train.toGlobalVector(new Vec3(.5, .2, .5), 1);
        var fallbacks = new java.util.ArrayList<com.nitricacid.traininteractive.TrainHandUseFallback>();
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(helper.getLevel().getServer(),
                new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND) {
                    @Override public void setListenerForServerboundHandshake(net.minecraft.network.PacketListener listener) {}
                }, player, net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false)) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom
                        && custom.payload() instanceof com.nitricacid.traininteractive.TrainHandUseFallback fallback) fallbacks.add(fallback);
            }
        };
        player.setPos(target.x, target.y - player.getEyeHeight(), target.z + 2);
        player.setYRot(180); player.setXRot(0);
        try {
            for (var block : java.util.List.of(Blocks.STONE, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("kaleidoscope_tavern:empty_glassware")))) {
                c.getBlocks().put(BlockPos.ZERO, new StructureBlockInfo(BlockPos.ZERO, block.defaultBlockState(), null));
                var handler = MovingInteractionBehaviour.REGISTRY.get(block);
                if (handler == null) c.getInteractors().remove(BlockPos.ZERO); else c.getInteractors().put(BlockPos.ZERO, handler);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WRITABLE_BOOK));
                int before = opens[0];
                int requestsBefore = fallbacks.size();
                new ContraptionInteractionPacket(train, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.SOUTH).handle(player);
                helper.assertTrue(fallbacks.size() == requestsBefore + 1 && fallbacks.getLast().hand() == InteractionHand.MAIN_HAND
                        && fallbacks.getLast().item().equals(ResourceLocation.parse("minecraft:writable_book")), "The server must request exactly one normal client book use after a failed train action");
                // The real client GUI check verifies this handshake; emulate the
                // resulting vanilla item-use request here to check server effects.
                player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
                helper.assertTrue(opens[0] == before + 1, "Failed train interaction must open the held book exactly once: " + BuiltInRegistries.BLOCK.getKey(block));
                helper.assertTrue(c.getBlocks().size() == 1 && c.getBlocks().get(BlockPos.ZERO).state().is(block), "Opening a book must not edit or remove the targeted block");
                player.getFoodData().setFoodLevel(10);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
                new ContraptionInteractionPacket(train, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.SOUTH).handle(player);
                helper.assertTrue(fallbacks.getLast().item().equals(ResourceLocation.parse("minecraft:apple")), "Failed train interaction must request ordinary food use");
                player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
                helper.assertTrue(player.isUsingItem() && player.getUseItem().is(Items.APPLE), "Failed interaction must also allow normal food use");
                player.stopUsingItem();
                int after = opens[0];
                int requestsAfter = fallbacks.size();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WRITABLE_BOOK));
                helper.assertTrue(com.nitricacid.traininteractive.TrainHandUse.afterInteraction(true, player, train, BlockPos.ZERO, InteractionHand.MAIN_HAND) && opens[0] == after && fallbacks.size() == requestsAfter,
                        "A successful train action must not also use the item in hand");
            }
        } finally { train.discard(); }
        helper.succeed();
    }
}
