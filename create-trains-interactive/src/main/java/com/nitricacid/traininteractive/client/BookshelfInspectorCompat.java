package com.nitricacid.traininteractive.client;

import com.lukasabbe.BookshelfInspectorClient;
import com.lukasabbe.data.BookData;
import com.lukasabbe.data.BookShelfData;
import com.nitricacid.traininteractive.MovingBookshelfContents;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.dadamalda.create_compatible_storage.mixin.ChiseledBookshelfAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class BookshelfInspectorCompat {
    private static boolean inspectingContraption;

    private BookshelfInspectorCompat() {}

    public static boolean inspect(Minecraft client) {
        if (client.level == null || client.player == null || client.getCameraEntity() == null
                || client.screen != null || !BookshelfInspectorClient.modAvailable) {
            reset();
            return false;
        }
        var camera = client.getCameraEntity();
        var origin = camera.getEyePosition(1);
        var stationaryHit = camera.pick(5, 1, false);
        double nearest = stationaryHit.getType() == HitResult.Type.MISS ? 5 : stationaryHit.getLocation().distanceTo(origin);
        var target = origin.add(camera.getViewVector(1).scale(nearest));
        var bounds = new AABB(origin, target).inflate(16);
        AbstractContraptionEntity bestEntity = null;
        BlockHitResult bestHit = null;
        for (var ref : ContraptionHandler.loadedContraptions.get(client.level).values()) {
            var entity = ref.get();
            if (entity == null || !entity.isAlive() || entity.getContraption() == null || !entity.getBoundingBox().intersects(bounds)) continue;
            var hit = ContraptionHandlerClient.rayTraceContraption(origin, target, entity);
            if (hit == null) continue;
            double distance = entity.toGlobalVector(hit.getLocation(), 1).distanceTo(origin);
            if (distance >= nearest) continue;
            nearest = distance;
            bestEntity = entity;
            bestHit = hit;
        }
        if (bestHit == null) {
            reset();
            return false;
        }
        var info = bestEntity.getContraption().getBlocks().get(bestHit.getBlockPos());
        // The nearest contraption block also occludes stationary bookshelves behind the train.
        inspectingContraption = true;
        if (info == null || !info.state().is(Blocks.CHISELED_BOOKSHELF)) {
            showEmpty();
            return true;
        }
        var slot = ((ChiseledBookshelfAccess) info.state().getBlock()).trainsInteractive$getHitSlot(bestHit, info.state());
        if (slot.isEmpty()) {
            showEmpty();
            return true;
        }
        show(info.pos(), info.state(), slot.getAsInt(), MovingBookshelfContents.read(info, client.level.registryAccess(), slot.getAsInt()));
        return true;
    }

    public static boolean isInspectingContraption() {
        return inspectingContraption;
    }

    public static void show(BlockPos pos, BlockState state, int slot, ItemStack book) {
        var data = BookshelfInspectorClient.bookShelfData;
        data.latestPos = pos;
        data.latestBlockState = state;
        data.currentSlotInt = slot;
        data.requestSent = false;
        data.isCurrentBookDataToggled = !book.isEmpty();
        BookshelfInspectorClient.currentBookData = book.isEmpty() ? BookData.empty() : new BookData(book.copy(), pos, slot);
    }

    private static void showEmpty() {
        BookshelfInspectorClient.bookShelfData.isCurrentBookDataToggled = false;
        BookshelfInspectorClient.bookShelfData.requestSent = false;
        BookshelfInspectorClient.currentBookData = BookData.empty();
    }

    private static void reset() {
        if (!inspectingContraption) return;
        inspectingContraption = false;
        BookshelfInspectorClient.bookShelfData = new BookShelfData();
        BookshelfInspectorClient.currentBookData = BookData.empty();
    }
}
