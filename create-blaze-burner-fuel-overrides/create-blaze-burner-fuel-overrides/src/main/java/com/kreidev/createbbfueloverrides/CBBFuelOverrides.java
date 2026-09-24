package com.kreidev.createbbfueloverrides;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

@Mod(CBBFuelOverrides.MOD_ID)
public class CBBFuelOverrides {
    public static final String MOD_ID = "createbbfueloverrides";

    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();

    public CBBFuelOverrides(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, FuelConfig.SPEC);
    }

    public static ResourceLocation resLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
