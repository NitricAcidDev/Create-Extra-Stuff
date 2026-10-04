package com.nitricacid.traininteractive.verification;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.gui.overlay.ShakerOverlay;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.nitricacid.traininteractive.TrainWorldAccess;
import com.nitricacid.traininteractive.client.TrainRayTrace;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "create_trains_interactive", value = Dist.CLIENT)
public final class TrainClientGameplayChecks {
    private static int stage;
    private static int ticks;
    private static volatile int entityId = -1;
    private static boolean sawMissingStructure;
    @SubscribeEvent
    public static void check(ClientTickEvent.Post event) throws Exception {
        if (!Boolean.getBoolean("create_trains_interactive.verifyGameplay")) return;
        var client = Minecraft.getInstance();
        client.options.pauseOnLostFocus = false;
        if (stage >= 2 && client.screen != null) client.setScreen(null);
        if (ticks % 100 == 0) org.slf4j.LoggerFactory.getLogger(TrainClientGameplayChecks.class).info("Gameplay check stage {} entity {} screen {}", stage, entityId, client.screen);
        if (++ticks > 2400) throw new AssertionError("Timed out verifying oversized train recovery");
        if (stage == 0) {
            stage = 1;
            client.createWorldOpenFlows().createFreshLevel("train-verification-" + System.currentTimeMillis(),
                    new LevelSettings("Train verification", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                    new WorldOptions(0L, false, false), registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), client.screen);
        } else if (stage == 1 && client.player != null && client.level != null && client.getSingleplayerServer() != null) {
            stage = 2;
            client.getSingleplayerServer().execute(() -> {
                var server = client.getSingleplayerServer();
                var level = server.overworld();
                var c = new BearingContraption(false, Direction.EAST);
                c.anchor = BlockPos.ZERO;
                c.bounds = new AABB(-1, -1, -1, 2, 2, 2);
                var tag = new CompoundTag();
                tag.putString("id", "kaleidoscope_tavern:shaker");
                tag.putByteArray("VerificationOversizedData", new byte[1_200_000]);
                c.getBlocks().put(BlockPos.ZERO, new StructureBlockInfo(BlockPos.ZERO, ModBlocks.SHAKER.get().defaultBlockState(), tag));
                c.getBlocks().put(BlockPos.ZERO.below(), new StructureBlockInfo(BlockPos.ZERO.below(), Blocks.STONE.defaultBlockState(), null));
                c.getStorage().initialize();
                var entity = OrientedContraptionEntity.create(level, c, Direction.SOUTH);
                var player = server.getPlayerList().getPlayers().getFirst();
                entity.setPos(player.getX(), player.getY() + 2, player.getZ());
                var be = new ShakerBlockEntity(BlockPos.ZERO, ModBlocks.SHAKER.get().defaultBlockState());
                be.setLevel(level);
                be.getStorage().setStackInSlot(0, new ItemStack(Items.APPLE));
                // Preserve the artificial payload alongside native inventory data.
                var saved = be.saveWithFullMetadata(level.registryAccess());
                saved.putByteArray("VerificationOversizedData", new byte[1_200_000]);
                c.getBlocks().put(BlockPos.ZERO, new StructureBlockInfo(BlockPos.ZERO, ModBlocks.SHAKER.get().defaultBlockState(), saved));
                level.addFreshEntity(entity);
                entityId = entity.getId();
            });
        } else if (stage == 2 && entityId != -1 && client.level.getEntity(entityId) instanceof OrientedContraptionEntity entity) {
            if (entity.getContraption() == null) { sawMissingStructure = true; return; }
            if (!sawMissingStructure) throw new AssertionError("Regression fixture did not reproduce Create's oversized null spawn");
            stage = 3;
            var center = entity.toGlobalVector(new Vec3(.5, .3, .5), 1);
            var eye = entity.toGlobalVector(new Vec3(.5, .3, 2.5), 1);
            client.player.setPos(eye.x, eye.y - client.player.getEyeHeight(), eye.z);
            var look = center.subtract(eye).normalize();
            client.player.setYRot((float) (Math.atan2(-look.x, look.z) * 180 / Math.PI));
            client.player.setXRot(0);
            client.player.yRotO = client.player.getYRot(); client.player.xRotO = 0;
        } else if (stage == 3) {
            var target = TrainRayTrace.find(client);
            if (target == null || target.entity().getId() != entityId) return;
            var world = ((TrainWorldAccess) target.entity()).trainsInteractive$world();
            var be = (ShakerBlockEntity) world.blockEntity(target.hit().getBlockPos());
            if (!be.getStorage().getStackInSlot(0).is(Items.APPLE)) throw new AssertionError("Recovered shaker lost its ingredient");
            var previous = client.hitResult;
            var graphics = new net.minecraft.client.gui.GuiGraphics(client, client.renderBuffers().bufferSource());
            new ShakerOverlay().render(graphics, client.getTimer());
            graphics.flush();
            if (client.hitResult != previous || com.nitricacid.traininteractive.MovingTrainWorld.current() != null)
                throw new AssertionError("Overlay left the vanilla hit or train world context changed");
            CarriageVisualChecks.check(client.level);
            var attack = new net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered(0, client.options.keyAttack, net.minecraft.world.InteractionHand.MAIN_HAND);
            com.nitricacid.traininteractive.client.TrainMiningGuard.attack(attack);
            if (attack.isCanceled() || !com.nitricacid.traininteractive.client.TrainMiningGuard.allows(entityId, target.hit().getBlockPos())
                    || com.nitricacid.traininteractive.client.TrainMiningGuard.allows(entityId, target.hit().getBlockPos().below()))
                throw new AssertionError("Mining must lock to the first selected train block");
            var repeat = new net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered(0, client.options.keyAttack, net.minecraft.world.InteractionHand.MAIN_HAND);
            com.nitricacid.traininteractive.client.TrainMiningGuard.attack(repeat);
            if (!repeat.isCanceled()) throw new AssertionError("Holding attack must not restart mining or break the next block");
            com.nitricacid.traininteractive.client.TrainMiningGuard.released(event);
            if (!com.nitricacid.traininteractive.client.TrainMiningGuard.allows(entityId, target.hit().getBlockPos().below()))
                throw new AssertionError("Releasing attack must allow selecting another block");
            org.slf4j.LoggerFactory.getLogger(TrainClientGameplayChecks.class).info("Mining target and mouse-release checks passed");
            var pos = target.hit().getBlockPos();
            world.set(pos, net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("kaleidoscope_tavern:signature_cocktail")).defaultBlockState());
            var cocktail = (com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.SignatureCocktailBlockEntity) world.blockEntity(pos);
            cocktail.setEffects(java.util.List.of(new com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData.Entry(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 60, 1, 1)));
            cocktail.setColor(0x12ab34);
            world.flush(false);
            var serving = com.nitricacid.traininteractive.client.TavernDrinkTooltip.serving(target, client.player);
            if (com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem.getEffects(serving).size() != 1
                    || com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem.getColor(serving) != 0x12ab34)
                throw new AssertionError("Drink overlay must include native cocktail effects and colour");
            com.nitricacid.traininteractive.client.TrainDrinkOverlay.render(new net.neoforged.neoforge.client.event.RenderGuiEvent.Post(graphics, client.getTimer()));
            world.set(pos, net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("kaleidoscope_tavern:wine")).defaultBlockState());
            var drinks = (com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity) world.blockEntity(pos);
            var wine = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("kaleidoscope_tavern:wine")));
            com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem.setBrewLevel(wine, 4);
            drinks.addItem(wine);
            world.flush(false);
            var bottle = com.nitricacid.traininteractive.client.TavernDrinkTooltip.serving(target, client.player);
            if (com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem.getBrewLevel(bottle) != 4)
                throw new AssertionError("Drink overlay must retain the served bottle's quality");
            com.nitricacid.traininteractive.client.TrainDrinkOverlay.render(new net.neoforged.neoforge.client.event.RenderGuiEvent.Post(graphics, client.getTimer()));
            graphics.flush();
            org.slf4j.LoggerFactory.getLogger(TrainClientGameplayChecks.class).info("Moving drink tooltip effects, colour and bottle quality checks passed");
            org.slf4j.LoggerFactory.getLogger(TrainClientGameplayChecks.class).info("Oversized train recovered; moving shaker raycast, inventory and native overlay checks passed");
            stage = 4;
            client.stop();
        }
    }
}
