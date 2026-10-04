package com.nitricacid.traininteractive.client;

import com.nitricacid.traininteractive.MovingTrainWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = "create_trains_interactive", value = Dist.CLIENT)
public final class TrainDrinkOverlay {
    private TrainDrinkOverlay() {}
    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var client = Minecraft.getInstance();
        if (client.player == null || client.screen != null || client.options.hideGui || client.player.isSpectator()) return;
        var target = TrainRayTrace.find(client);
        if (target == null) return;
        var info = target.entity().getContraption().getBlocks().get(target.hit().getBlockPos());
        if (info == null || !MovingTrainWorld.isServiceBlock(info.state())
                || !net.neoforged.fml.ModList.get().isLoaded("kaleidoscope_tavern")) return;
        // Use the native serving item: quality, signature effects and add-on tooltips stay intact.
        var stack = TavernDrinkTooltip.serving(target, client.player);
        if (stack.isEmpty()) return;
        boolean drink = false;
        for (Class<?> type = stack.getItem().getClass(); type != null; type = type.getSuperclass()) {
            if (type.getName().startsWith("com.github.ysbbbbbb.kaleidoscopetavern.item.")
                    && java.util.Set.of("DrinkBlockItem", "BottleBlockItem", "CocktailBlockItem").contains(type.getSimpleName())) drink = true;
        }
        if (!drink) return;
        var graphics = event.getGuiGraphics();
        graphics.renderTooltip(client.font, stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(client.level),
                client.player, TooltipFlag.NORMAL), java.util.Optional.empty(), graphics.guiWidth() / 2 + 12, graphics.guiHeight() / 2 + 22);
    }
}
