package net.dadamalda.create_compatible_storage.storage_types;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import net.dadamalda.create_compatible_storage.CCSMountedStorageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class UncooperativeMountedStorage extends SimpleMountedStorage {
    public static final MapCodec<UncooperativeMountedStorage> CODEC = SimpleMountedStorage.codec(UncooperativeMountedStorage::new);

    protected UncooperativeMountedStorage(MountedItemStorageType<?> type, IItemHandler handler) {
        super(type, handler);
    }

    public UncooperativeMountedStorage(IItemHandler handler) {
        this(CCSMountedStorageTypes.UNCOOPERATIVE.get(), handler);
    }

    @Override
    protected net.minecraft.world.MenuProvider createMenuProvider(net.minecraft.network.chat.Component name,
            net.neoforged.neoforge.items.IItemHandlerModifiable handler,
            java.util.function.Predicate<net.minecraft.world.entity.player.Player> stillValid,
            java.util.function.Consumer<net.minecraft.world.entity.player.Player> onClose) {
        if (handler.getSlots() != 5) return super.createMenuProvider(name, handler, stillValid, onClose);
        var container = new com.simibubi.create.api.contraption.storage.item.menu.StorageInteractionWrapper(handler, stillValid, onClose);
        return new net.minecraft.world.SimpleMenuProvider((id, inv, player) ->
                new net.minecraft.world.inventory.HopperMenu(id, inv, container), name);
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if(!(be instanceof BaseContainerBlockEntity container) || container.getContainerSize() != wrapped.getSlots()) return;
        for (int i = 0; i < wrapped.getSlots(); i++) {
            container.setItem(i, wrapped.getStackInSlot(i).copy());
            be.setChanged();
        }
    }
}
