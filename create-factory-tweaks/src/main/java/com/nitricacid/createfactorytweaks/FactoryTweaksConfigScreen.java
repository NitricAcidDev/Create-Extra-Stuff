package com.nitricacid.createfactorytweaks;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public final class FactoryTweaksConfigScreen extends Screen {
    private final ModContainer container;
    private final Screen parent;

    public FactoryTweaksConfigScreen(ModContainer container, Screen parent) {
        super(Component.translatable("createfactorytweaks.configuration.title"));
        this.container = container;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(310, width - 32);
        int x = (width - buttonWidth) / 2;
        int y = height / 2 - 40;
        addRenderableWidget(Button.builder(Component.translatable("createfactorytweaks.configuration.blazeBurnerFuels"),
                button -> minecraft.setScreen(new ConfigurationScreen(container, this)))
                .bounds(x, y, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("createfactorytweaks.configuration.distillationPresets"),
                button -> minecraft.setScreen(new DistillationPresetsScreen(this)))
                .bounds(x, y + 26, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, height - 32, buttonWidth, 20).build());
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }

    private static final class DistillationPresetsScreen extends Screen {
        private final Screen parent;

        private DistillationPresetsScreen(Screen parent) {
            super(Component.translatable("createfactorytweaks.configuration.distillationPresets"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int buttonWidth = Math.min(310, width - 32);
            int x = (width - buttonWidth) / 2;
            int y = height / 2 - 40;
            Button vanilla = Button.builder(Component.translatable("createfactorytweaks.configuration.vanillaSelected"), button -> {})
                    .bounds(x, y, buttonWidth, 20)
                    .tooltip(Tooltip.create(Component.translatable("createfactorytweaks.configuration.vanilla.tooltip"))).build();
            vanilla.active = false;
            addRenderableWidget(vanilla);
            Button adjusted = Button.builder(Component.translatable("createfactorytweaks.configuration.adjusted"), button -> {})
                    .bounds(x, y + 26, buttonWidth, 20)
                    .tooltip(Tooltip.create(Component.translatable("createfactorytweaks.configuration.adjusted.tooltip"))).build();
            adjusted.active = false;
            addRenderableWidget(adjusted);
            addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                    .bounds(x, height - 32, buttonWidth, 20).build());
        }

        @Override
        public void onClose() { minecraft.setScreen(parent); }
    }
}
