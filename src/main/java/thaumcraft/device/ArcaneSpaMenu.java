package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.registry.ModMenus;

public class ArcaneSpaMenu extends AbstractContainerMenu {
    public static final int BUTTON_MIX = 1;

    private final BlockPos pos;
    private final @Nullable ArcaneSpaBlockEntity spa;

    public ArcaneSpaMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(ModMenus.ARCANE_SPA.get(), containerId);
        this.pos = pos;
        this.spa = inventory.player.level().getBlockEntity(pos) instanceof ArcaneSpaBlockEntity entity ? entity : null;
        Container container = spa != null ? spa : new SimpleContainer(1);
        addSlot(new Slot(container, 0, 65, 31) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SpaHooks.isBathSalts(stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public @Nullable ArcaneSpaBlockEntity spa() {
        return spa;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_MIX && spa != null) {
            spa.toggleMix();
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!SpaHooks.isBathSalts(stack) || !moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!SpaHooks.isBathSalts(stack) || !moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return spa != null && spa.stillValid(player);
    }
}
