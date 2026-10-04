package com.nitricacid.traininteractive;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class TrainEditingConfig {
    public static final List<String> DEFAULT_BLOCKS = List.of("kaleidoscope_tavern:*", "kaleidoscope_world_liquor:*", "farmersdelight:*",
            "minecraft:furnace", "minecraft:smoker", "minecraft:campfire", "minecraft:soul_campfire");
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_BLOCKS;
    static {
        var builder = new ModConfigSpec.Builder();
        ENABLED = builder.comment("Restrict block placement and breaking on Create contraptions when On the Move is installed.",
                "Applies to all players, including creative mode. Does not restrict using existing blocks.")
                .define("restrictTrainEditing", true);
        ALLOWED_BLOCKS = builder.comment("Blocks players may place or break on contraptions.",
                "Use exact block IDs or namespace:* entries. An empty list allows no blocks while the restriction is enabled.")
                .defineListAllowEmpty("allowedTrainBlocks", DEFAULT_BLOCKS, () -> "example:block", TrainEditingConfig::validEntry);
        SPEC = builder.build();
    }
    private TrainEditingConfig() {}
    private static boolean validEntry(Object value) {
        if (!(value instanceof String entry)) return false;
        return ResourceLocation.tryParse(entry.endsWith(":*") ? entry.substring(0, entry.length() - 1) + "placeholder" : entry) != null;
    }
    public static boolean allows(Block block) {
        if (!ModList.get().isLoaded("createonthemove")) return true;
        if (SPEC.isLoaded() && !ENABLED.get()) return true;
        var id = BuiltInRegistries.BLOCK.getKey(block);
        return matches(id, SPEC.isLoaded() ? ALLOWED_BLOCKS.get() : DEFAULT_BLOCKS);
    }
    public static boolean matches(ResourceLocation id, List<? extends String> entries) {
        return entries.contains(id.toString()) || entries.contains(id.getNamespace() + ":*");
    }
    public static void rejected(Player player) {
        if (!player.level().isClientSide) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("create_trains_interactive.editing.not_allowed"), true);
    }
}
