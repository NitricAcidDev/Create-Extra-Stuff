package net.dadamalda.create_compatible_storage.mixin;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarCabinetBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock")
public interface TavernCabinetAccess {
    @Invoker(value = "onClick", remap = false)
    boolean trainsInteractive$onClick(BarCabinetBlockEntity cabinet, Player player, ItemStack held, boolean left);
}
