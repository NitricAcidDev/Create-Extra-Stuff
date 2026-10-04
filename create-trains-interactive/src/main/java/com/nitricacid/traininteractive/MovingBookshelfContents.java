package com.nitricacid.traininteractive;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public final class MovingBookshelfContents {
    private MovingBookshelfContents() {}

    public static ItemStack read(StructureBlockInfo info, HolderLookup.Provider registries, int slot) {
        if (info == null || !info.state().is(Blocks.CHISELED_BOOKSHELF) || info.nbt() == null || slot < 0 || slot >= 6)
            return ItemStack.EMPTY;
        var shelf = new ChiseledBookShelfBlockEntity(info.pos(), info.state());
        shelf.loadWithComponents(info.nbt(), registries);
        return shelf.getItem(slot).copy();
    }
}
