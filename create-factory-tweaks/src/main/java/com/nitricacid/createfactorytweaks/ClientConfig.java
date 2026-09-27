package com.nitricacid.createfactorytweaks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = FactoryTweaks.MOD_ID, dist = Dist.CLIENT)
public final class ClientConfig {
    public ClientConfig(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new FactoryTweaksConfigScreen(mod, parent));
    }
}
