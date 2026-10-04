package com.nitricacid.traininteractive;

import java.util.OptionalDouble;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FurnitureSeats {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> EXTRA_SEATS;
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        EXTRA_SEATS = builder.comment("Extra seats: exact block ID=seat entity height, e.g. example:chair=0.65.",
                "Install matching settings on clients and server; reassemble contraptions after changes.")
            .defineListAllowEmpty("extraSeats", java.util.List.of(), () -> "example:chair=0.65", FurnitureSeats::validEntry);
        SPEC = builder.build();
    }

    private FurnitureSeats() {}

    private static boolean validEntry(Object entry) {
        if (!(entry instanceof String text)) return false;
        String[] parts = text.split("=", -1);
        if (parts.length != 2 || ResourceLocation.tryParse(parts[0]) == null) return false;
        try { double value = Double.parseDouble(parts[1]); return Double.isFinite(value) && value > 0 && value <= 2; }
        catch (NumberFormatException e) { return false; }
    }

    public static OptionalDouble height(BlockState state) {
        String name = state.getBlock().getClass().getName();
        if (name.equals("com.github.ysbbbbbb.kaleidoscopetavern.block.deco.SofaBlock")) return OptionalDouble.of(0.5125);
        if (name.equals("com.github.ysbbbbbb.kaleidoscopetavern.block.deco.BarStoolBlock")) return OptionalDouble.of(0.875);
        if (name.equals("com.bmt.kaleidoscope_world_liquor.block.ChairBlock")) return OptionalDouble.of(0.9);
        if (SPEC.isLoaded()) {
            String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            for (String entry : EXTRA_SEATS.get()) {
                String[] parts = entry.split("=", -1);
                if (parts[0].equals(id)) return OptionalDouble.of(Double.parseDouble(parts[1]));
            }
        }
        return OptionalDouble.empty();
    }
}
