package com.nitricacid.createfactorytweaks;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public final class FactoryTweaksConfigScreen extends Screen {
    private final ModContainer container;
    private final Screen parent;

    public FactoryTweaksConfigScreen(ModContainer container, Screen parent) {
        super(Component.translatable("createfactorytweaks.configuration.title"));
        this.container = container;
        this.parent = parent;
    }

    private static boolean canEdit(Screen screen) {
        return FuelConfig.SPEC.isLoaded() && (screen.getMinecraft().getConnection() == null
                || screen.getMinecraft().hasSingleplayerServer());
    }

    private static ConfigurationScreen settings(ModContainer container, Screen parent, boolean distillation) {
        return new ConfigurationScreen(container, parent, (screen, type, config, title) ->
                new SelectedSettingsScreen(screen, type, config, title, distillation));
    }

    private static final class SelectedSettingsScreen extends ConfigurationScreen.ConfigurationSectionScreen {
        private final boolean distillation;

        private SelectedSettingsScreen(Screen parent, ModConfig.Type type, ModConfig config,
                                       Component title, boolean distillation) {
            super(parent, type, config, title, (context, key, original) -> {
                if (context.keylist().isEmpty()) {
                    if (key.equals("lockDefaults")) return null;
                    if (distillation) return key.equals("distillationOutputs") ? original : null;
                    return key.equals("distillationOutputs") ? null : original;
                }
                if (distillation && context.keylist().size() == 1 && key.equals("preset")) return null;
                if (FuelConfig.locked() && (context.keylist().size() >= 2 || distillation)) {
                    Button disabled = Button.builder(Component.translatable("createfactorytweaks.configuration.lockedValue"), b -> {})
                            .width(Button.DEFAULT_WIDTH).build();
                    disabled.active = false;
                    return new Element(original.name(), original.tooltip(), disabled);
                }
                return original;
            });
            this.distillation = distillation;
        }

        @Override
        protected Element createSection(String key, UnmodifiableConfig subconfig, UnmodifiableConfig subsection) {
            if (context.keylist().isEmpty() && key.equals("distillationOutputs") != distillation) return null;
            return super.createSection(key, subconfig, subsection);
        }
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(310, width - 32);
        int x = (width - buttonWidth) / 2;
        int y = height / 2 - 53;
        Button lock = Button.builder(lockLabel(), button -> {
                    FuelConfig.LOCK_DEFAULTS.set(!FuelConfig.LOCK_DEFAULTS.get());
                    FuelConfig.SPEC.save();
                    button.setMessage(lockLabel());
                }).bounds(x, y, buttonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("createfactorytweaks.configuration.lockDefaults.tooltip"))).build();
        lock.active = canEdit(this);
        addRenderableWidget(lock);
        Button fuels = Button.builder(Component.translatable("createfactorytweaks.configuration.blazeBurnerFuels"),
                button -> minecraft.setScreen(settings(container, this, false)))
                .bounds(x, y + 26, buttonWidth, 20).build();
        fuels.active = canEdit(this) && !FuelConfig.locked();
        addRenderableWidget(fuels);
        addRenderableWidget(Button.builder(Component.translatable("createfactorytweaks.configuration.distillationPresets"),
                button -> minecraft.setScreen(new DistillationPresetsScreen(container, this)))
                .bounds(x, y + 52, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, height - 32, buttonWidth, 20).build());
    }

    private Component lockLabel() {
        return Component.translatable("createfactorytweaks.configuration.lockDefaults")
                .append(": ")
                .append(Component.translatable(FuelConfig.locked() ? "options.on" : "options.off"));
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }

    private static final class DistillationPresetsScreen extends Screen {
        private final ModContainer container;
        private final Screen parent;

        private DistillationPresetsScreen(ModContainer container, Screen parent) {
            super(Component.translatable("createfactorytweaks.configuration.distillationPresets"));
            this.container = container;
            this.parent = parent;
        }

        private void select(FuelConfig.DistillationPreset preset) {
            FuelConfig.DISTILLATION_PRESET.set(preset);
            FuelConfig.SPEC.save();
            minecraft.setScreen(new DistillationPresetsScreen(container, parent));
        }

        @Override
        protected void init() {
            int buttonWidth = Math.min(310, width - 32);
            int x = (width - buttonWidth) / 2;
            int y = height / 2 - 53;
            boolean editable = canEdit(this) && !FuelConfig.locked();
            FuelConfig.DistillationPreset selected = FuelConfig.effectivePreset();
            Button vanilla = Button.builder(Component.translatable(selected == FuelConfig.DistillationPreset.VANILLA
                            ? "createfactorytweaks.configuration.vanillaSelected" : "createfactorytweaks.configuration.vanilla"),
                    button -> select(FuelConfig.DistillationPreset.VANILLA))
                    .bounds(x, y, buttonWidth, 20)
                    .tooltip(Tooltip.create(Component.translatable("createfactorytweaks.configuration.vanilla.tooltip"))).build();
            vanilla.active = editable && selected != FuelConfig.DistillationPreset.VANILLA;
            addRenderableWidget(vanilla);
            Button adjusted = Button.builder(Component.translatable(selected == FuelConfig.DistillationPreset.ADJUSTED
                            ? "createfactorytweaks.configuration.adjustedSelected" : "createfactorytweaks.configuration.adjusted"),
                    button -> select(FuelConfig.DistillationPreset.ADJUSTED))
                    .bounds(x, y + 26, buttonWidth, 20)
                    .tooltip(Tooltip.create(Component.translatable("createfactorytweaks.configuration.adjusted.tooltip"))).build();
            adjusted.active = editable && selected != FuelConfig.DistillationPreset.ADJUSTED;
            addRenderableWidget(adjusted);
            addRenderableWidget(Button.builder(Component.translatable("createfactorytweaks.configuration.custom"),
                    button -> minecraft.setScreen(new CustomDistillationScreen(container, this)))
                    .bounds(x, y + 52, buttonWidth, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                    .bounds(x, height - 32, buttonWidth, 20).build());
        }

        @Override
        public void onClose() { minecraft.setScreen(parent); }
    }

    private static final class CustomDistillationScreen extends Screen {
        private final ModContainer container;
        private final Screen parent;

        private CustomDistillationScreen(ModContainer container, Screen parent) {
            super(Component.translatable("createfactorytweaks.configuration.custom"));
            this.container = container;
            this.parent = parent;
        }

        @Override
        protected void init() {
            int buttonWidth = Math.min(310, width - 32);
            int x = (width - buttonWidth) / 2;
            int y = height / 2 - 40;
            boolean editable = canEdit(this) && !FuelConfig.locked();
            Button useCustom = Button.builder(Component.translatable("createfactorytweaks.configuration.useCustom"), button -> {
                        FuelConfig.DISTILLATION_PRESET.set(FuelConfig.DistillationPreset.CUSTOM);
                        FuelConfig.SPEC.save();
                        minecraft.setScreen(new CustomDistillationScreen(container, parent));
                    }).bounds(x, y, buttonWidth, 20).build();
            useCustom.active = editable && FuelConfig.effectivePreset() != FuelConfig.DistillationPreset.CUSTOM;
            addRenderableWidget(useCustom);
            Button editor = Button.builder(Component.translatable("createfactorytweaks.configuration.customEditor"),
                    button -> minecraft.setScreen(settings(container, this, true)))
                    .bounds(x, y + 26, buttonWidth, 20).build();
            editor.active = editable;
            addRenderableWidget(editor);
            addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                    .bounds(x, height - 32, buttonWidth, 20).build());
        }

        @Override
        public void onClose() { minecraft.setScreen(parent); }
    }
}
