package com.nitricacid.traininteractive.verification;

import com.lukasabbe.BookshelfInspectorClient;
import com.lukasabbe.inspector.Inspector;
import com.nitricacid.traininteractive.client.BookshelfInspectorCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class InspectorClientChecks {
    private static boolean finished;

    public static void check(ClientTickEvent.Post event) throws Exception {
        if (finished || !Boolean.getBoolean("create_trains_interactive.verifyInspector")) return;
        finished = true;
        var client = Minecraft.getInstance();
        new Inspector().inspect(client);
        Class.forName("com.lukasabbe.network.client.BookShelfInventoryHandlerServer");
        var book = new ItemStack(Items.BOOK);
        book.set(DataComponents.CUSTOM_NAME, Component.literal("Moving library"));
        BookshelfInspectorCompat.show(BlockPos.ZERO, Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 4, book);
        if (!BookshelfInspectorClient.bookShelfData.isCurrentBookDataToggled
                || BookshelfInspectorClient.currentBookData.slotId != 4
                || !BookshelfInspectorClient.currentBookData.itemStack.getHoverName().getString().equals("Moving library"))
            throw new AssertionError("Inspector must receive the moving shelf's selected book");
        BookshelfInspectorCompat.show(BlockPos.ZERO, Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 5, ItemStack.EMPTY);
        if (BookshelfInspectorClient.bookShelfData.isCurrentBookDataToggled
                || !BookshelfInspectorClient.currentBookData.itemStack.isEmpty())
            throw new AssertionError("Empty slots must hide the Inspector overlay");
        org.slf4j.LoggerFactory.getLogger(InspectorClientChecks.class).info("Bookshelf Inspector client compatibility checks passed");
        if (!Boolean.getBoolean("create_trains_interactive.verifyGameplay")) client.stop();
    }
}
