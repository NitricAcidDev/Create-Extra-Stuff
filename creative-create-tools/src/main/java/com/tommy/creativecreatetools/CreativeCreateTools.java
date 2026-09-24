package com.tommy.creativecreatetools;

import com.tommy.creativecreatetools.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod("creativecreatetools")
public final class CreativeCreateTools {
    private static final ResourceLocation WRENCH = ResourceLocation.fromNamespaceAndPath("create", "wrench");
    private static final ResourceLocation STAFF = ResourceLocation.fromNamespaceAndPath("simulated", "creative_physics_staff");
    private static final ResourceLocation OLD_STAFF = ResourceLocation.fromNamespaceAndPath("simulated", "physics_staff");
    private static int lastWrenchSlot = -1;
    private static int lastStaffSlot = -1;

    public CreativeCreateTools() {
        ModConfig.load();
        if (FMLEnvironment.dist == Dist.CLIENT) ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class, () -> (container, parent) -> ModConfig.createConfigScreen(parent));
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance(); Player p = mc.player;
        if (p == null || mc.level == null || !p.isCreative()) return;
        // Allow refilling while the inventory is open, but never while the
        // player is actively holding a mouse button and moving a stack.
        if (mc.screen != null && (mc.mouseHandler.isLeftPressed() || mc.mouseHandler.isRightPressed())) return;
        rememberLastSlots(p);
        ModConfig c = ModConfig.get();
        if (c.refillWrench && !contains(p, WRENCH)) place(p, WRENCH, lastWrenchSlot);
        if (c.refillPhysicsStaff && !contains(p, STAFF, OLD_STAFF)) {
            if (!place(p, STAFF, lastStaffSlot)) place(p, OLD_STAFF, lastStaffSlot);
        }
    }

    private static void rememberLastSlots(Player p) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (matches(stack, WRENCH)) lastWrenchSlot = i;
            if (matches(stack, STAFF, OLD_STAFF)) lastStaffSlot = i;
        }
    }

    private static boolean contains(Player p, ResourceLocation... ids) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) if (matches(p.getInventory().getItem(i), ids)) return true;
        return matches(p.containerMenu.getCarried(), ids);
    }
    private static boolean matches(ItemStack s, ResourceLocation... ids) {
        if (s.isEmpty()) return false; ResourceLocation id = BuiltInRegistries.ITEM.getKey(s.getItem());
        for (ResourceLocation wanted : ids) if (wanted.equals(id)) return true; return false;
    }
    private static boolean place(Player p, ResourceLocation id, int preferred) {
        if (contains(p, id)) return true;
        var item = BuiltInRegistries.ITEM.getOptional(id); if (item.isEmpty()) return false;
        int slot = preferred >= 0 && preferred < 36 && p.getInventory().getItem(preferred).isEmpty() ? preferred : firstEmpty(p);
        if (slot < 0 || contains(p, id)) return false;
        p.getInventory().setItem(slot, new ItemStack(item.get())); return true;
    }
    private static int firstEmpty(Player p) {
        for (int i = 9; i < 36; i++) if (p.getInventory().getItem(i).isEmpty()) return i;
        for (int i = 0; i < 9; i++) if (p.getInventory().getItem(i).isEmpty()) return i; return -1;
    }
}
