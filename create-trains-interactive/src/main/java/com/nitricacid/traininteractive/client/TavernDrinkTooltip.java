package com.nitricacid.traininteractive.client;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.SignatureCocktailBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem;
import com.nitricacid.traininteractive.TrainPickedItem;
import com.nitricacid.traininteractive.TrainWorldAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TavernDrinkTooltip {
    private TavernDrinkTooltip() {}
    public static ItemStack serving(TrainRayTrace.Target target, Player player) {
        var world = ((TrainWorldAccess) target.entity()).trainsInteractive$world();
        return world.run(target.hit().getBlockPos(), () -> {
            var be = world.blockEntity(target.hit().getBlockPos());
            if (be instanceof DrinkBlockEntity drinks) {
                for (int i = drinks.getItems().size() - 1; i >= 0; i--)
                    if (!drinks.getItems().get(i).isEmpty()) return drinks.getItems().get(i).copy();
            }
            var stack = TrainPickedItem.create(target.entity(), target.hit(), player, false);
            if (be instanceof SignatureCocktailBlockEntity cocktail) {
                SignatureCocktailBlockItem.setEffects(stack, cocktail.getEffects());
                SignatureCocktailBlockItem.setColor(stack, cocktail.getColor());
            }
            return stack;
        });
    }
}
