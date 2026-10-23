package thaumcraft.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.arcane.FocalManipulatorBlockEntity;
import thaumcraft.registry.ModMenus;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.focus.FocusItem;

public class FocalManipulatorMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final @Nullable FocalManipulatorBlockEntity table;

    public FocalManipulatorMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(ModMenus.FOCAL_MANIPULATOR.get(), containerId);
        this.pos = pos;
        this.table = inventory.player.level().getBlockEntity(pos) instanceof FocalManipulatorBlockEntity entity ? entity : null;
        Container container = table != null ? table : new SimpleContainer(1);
        addSlot(new Slot(container, 0, 88, 60) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof FocusItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 16 + column * 18, 151 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 16 + column * 18, 209));
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    public @Nullable FocalManipulatorBlockEntity table() {
        return table;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && table != null && !table.startCraft(id, player) && player.level() != null) {
            player.level().playSound(null, pos, ModSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index != 0) {
            if (stack.getItem() instanceof FocusItem) {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 1 && index < 28) {
                if (!moveItemStackTo(stack, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 28 && index < 37 && !moveItemStackTo(stack, 1, 28, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 1, 37, false)) {
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
        return table != null && table.stillValid(player);
    }
}
