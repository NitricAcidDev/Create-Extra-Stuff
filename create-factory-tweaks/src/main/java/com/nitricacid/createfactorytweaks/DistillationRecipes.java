package com.nitricacid.createfactorytweaks;

import java.util.Map;
import java.util.Set;

/** The six distillation recipes shipped by TFMG, identified without depending on output amounts. */
public final class DistillationRecipes {
    private static final Map<String, Set<String>> OUTPUTS = Map.of(
            "crude_oil", Set.of("heavy_oil", "diesel", "kerosene", "naphtha", "gasoline", "lpg"),
            "crude_oil_light_distillation", Set.of("heavy_oil", "diesel", "gasoline"),
            "crude_oil_no_naphtha", Set.of("heavy_oil", "diesel", "kerosene", "gasoline", "lpg"),
            "heavy_oil", Set.of("heavy_oil", "lubrication_oil", "diesel", "kerosene", "naphtha"),
            "heavy_oil_light_distillation", Set.of("heavy_oil", "diesel", "lubrication_oil"),
            "heavy_oil_no_naphtha", Set.of("heavy_oil", "lubrication_oil", "diesel", "kerosene"));

    private DistillationRecipes() {}

    public static String identify(String input, Set<String> outputs) {
        String prefix = switch (input) {
            case "crude_oil" -> "crude_oil";
            case "heavy_oil" -> "heavy_oil";
            default -> null;
        };
        if (prefix == null) return null;
        for (var recipe : OUTPUTS.entrySet())
            if (recipe.getKey().startsWith(prefix) && recipe.getValue().equals(outputs)) return recipe.getKey();
        return null;
    }
}
