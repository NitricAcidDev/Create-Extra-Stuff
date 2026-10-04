package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/** Uses the block's own pick result and vanilla creative block-entity copying. */
public final class TrainPickedItem {
    private TrainPickedItem() {}

    public static ItemStack create(AbstractContraptionEntity entity, BlockHitResult hit, Player player, boolean copyData) {
        var world = ((TrainWorldAccess) entity).trainsInteractive$world();
        return world.run(hit.getBlockPos(), () -> {
            var stack = world.state(hit.getBlockPos()).getCloneItemStack(hit, entity.level(), hit.getBlockPos(), player);
            if (stack.isEmpty() || !copyData) return stack;
            var be = world.blockEntity(hit.getBlockPos());
            if (be != null) {
                var tag = be.saveCustomAndMetadata(entity.level().registryAccess());
                be.removeComponentsFromTag(tag);
                BlockItem.setBlockEntityData(stack, be.getType(), tag);
                stack.applyComponents(be.collectComponents());
            }
            return stack;
        });
    }
}
