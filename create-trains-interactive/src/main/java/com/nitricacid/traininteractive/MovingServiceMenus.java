package com.nitricacid.traininteractive;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;

@net.neoforged.fml.common.EventBusSubscriber(modid = "create_trains_interactive")
public final class MovingServiceMenus {
    public static final int MAGIC = 0x54524E53;
    private record Entry(AbstractContraptionEntity entity, BlockPos pos, Block block, AbstractContainerMenu menu) {}
    private static final Map<UUID, Entry> MENUS = new java.util.concurrent.ConcurrentHashMap<>();
    private MovingServiceMenus() {}

    public static void opened(Player player, MovingTrainWorld world) {
        MENUS.put(player.getUUID(), new Entry(world.entity, world.interactingPos(),
                world.state(world.interactingPos()).getBlock(), player.containerMenu));
    }
    public static void closed(Player player) { MENUS.remove(player.getUUID()); }

    public static <T> T run(Player player, java.util.function.Supplier<T> action) {
        var entry = MENUS.get(player.getUUID());
        if (entry == null || entry.menu != player.containerMenu || !Boolean.TRUE.equals(valid(player))) return action.get();
        var world = ((TrainWorldAccess) entry.entity).trainsInteractive$world();
        try { return world.run(entry.pos, action); }
        finally { world.flush(true); }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void loggedOut(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        closed(event.getEntity());
    }

    public static Boolean valid(Player player) {
        if (player.level().isClientSide) return null;
        var entry = MENUS.get(player.getUUID());
        if (entry == null) return null;
        if (entry.menu != player.containerMenu) { closed(player); return null; }
        var entity = entry.entity;
        return entity.isAlive() && entity.level() == player.level() && entity.getContraption() != null
                && entity.getContraption().getBlocks().containsKey(entry.pos)
                && entity.getContraption().getBlocks().get(entry.pos).state().getBlock() == entry.block
                && entity.canInteractWithBlock(player, entry.pos, player.blockInteractionRange() + 4);
    }
}
