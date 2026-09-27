package com.nitricacid.createfactorytweaks;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

@Mod(FactoryTweaks.MOD_ID)
public class FactoryTweaks {
    public static final String MOD_ID = "createfactorytweaks";

    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();

    public FactoryTweaks(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, FuelConfig.SPEC);
    }

    public static ResourceLocation resLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
