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
import thaumcraft.wand.focus.ExcavationFocus;

public class ArcaneBoreMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final @Nullable ArcaneBoreBlockEntity bore;

    public ArcaneBoreMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(ModMenus.ARCANE_BORE.get(), containerId);
        this.pos = pos;
        this.bore = inventory.player.level().getBlockEntity(pos) instanceof ArcaneBoreBlockEntity entity ? entity : null;
        Container container = bore != null ? bore : new SimpleContainer(2);
        addSlot(new Slot(container, ArcaneBoreBlockEntity.FOCUS_SLOT, 26, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ExcavationFocus;
            }
        });
        addSlot(new Slot(container, ArcaneBoreBlockEntity.PICKAXE_SLOT, 74, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ArcaneBoreBlockEntity.isPickaxe(stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 59 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 117));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public @Nullable ArcaneBoreBlockEntity bore() {
        return bore;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index <= 1) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof ExcavationFocus) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (ArcaneBoreBlockEntity.isPickaxe(stack) && !moveItemStackTo(stack, 1, 2, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return bore != null && bore.stillValid(player);
    }
}
