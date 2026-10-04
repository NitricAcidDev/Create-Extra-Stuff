package com.nitricacid.traininteractive.verification;

import com.nitricacid.traininteractive.TrainWorldAccess;
import com.nitricacid.traininteractive.client.TrainRayTrace;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.event.InputEvent;

public final class TrainHandUseClientChecks {
    private TrainHandUseClientChecks() {}

    public static void start(Minecraft client) {
        var hit = TrainRayTrace.find(client);
        if (hit == null) throw new AssertionError("Book test must target a train block");
        var world = ((TrainWorldAccess) hit.entity()).trainsInteractive$world();
        var state = world.state(hit.hit().getBlockPos());
        var blockEntity = world.blockEntity(hit.hit().getBlockPos());
        world.set(hit.hit().getBlockPos(), Blocks.STONE.defaultBlockState());
        client.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WRITABLE_BOOK));
        ContraptionHandlerClient.rightClickingOnContraptionsGetsHandledLocally(new InputEvent.InteractionKeyMappingTriggered(1, client.options.keyUse, InteractionHand.MAIN_HAND));
        if (!(client.screen instanceof BookEditScreen)) throw new AssertionError("Right-click over an ordinary train block must open the held book locally");
        client.setScreen(null);
        world.set(hit.hit().getBlockPos(), state);
        if (blockEntity != null) world.setBlockEntity(blockEntity);
        ContraptionHandlerClient.rightClickingOnContraptionsGetsHandledLocally(new InputEvent.InteractionKeyMappingTriggered(1, client.options.keyUse, InteractionHand.MAIN_HAND));
    }

    public static boolean done(Minecraft client) {
        if (!(client.screen instanceof BookEditScreen)) return false;
        client.setScreen(null);
        client.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        org.slf4j.LoggerFactory.getLogger(TrainHandUseClientChecks.class).info("Held books open over ordinary and native train blocks; client and server fallback checks passed");
        return true;
    }
}
