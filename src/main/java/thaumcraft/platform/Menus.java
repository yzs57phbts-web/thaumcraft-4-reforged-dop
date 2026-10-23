package thaumcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public final class Menus {
    private Menus() {
    }

    @FunctionalInterface
    public interface PosFactory<M extends AbstractContainerMenu> {
        M create(int containerId, Inventory inventory, BlockPos pos);
    }

    public static <M extends AbstractContainerMenu> MenuType<M> withPos(PosFactory<M> factory) {
        return IMenuTypeExtension.create((containerId, inventory, buffer) -> factory.create(containerId, inventory, buffer.readBlockPos()));
    }

    @FunctionalInterface
    public interface EntityFactory<M extends AbstractContainerMenu> {
        M create(int containerId, Inventory inventory, int entityId);
    }

    public static <M extends AbstractContainerMenu> MenuType<M> withEntity(EntityFactory<M> factory) {
        return IMenuTypeExtension.create((id, inventory, buffer) -> factory.create(id, inventory, buffer.readVarInt()));
    }

    public static void openEntity(ServerPlayer player, MenuProvider provider, int entityId) {
        player.openMenu(provider, buffer -> buffer.writeVarInt(entityId));
    }

    public static <M extends AbstractContainerMenu> MenuType<M> simple(MenuType.MenuSupplier<M> factory) {
        return new MenuType<>(factory, FeatureFlags.VANILLA_SET);
    }

    public static void open(ServerPlayer player, MenuProvider provider) {
        player.openMenu(provider);
    }

    public static void open(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, pos);
    }
}
