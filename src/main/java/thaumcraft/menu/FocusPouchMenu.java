package thaumcraft.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.registry.ModMenus;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusPouchItem;

public class FocusPouchMenu extends AbstractContainerMenu {
    public static final int POUCH_SLOTS = FocusPouchItem.SIZE;
    public static final int INVENTORY_START = POUCH_SLOTS;
    public static final int HOTBAR_START = INVENTORY_START + 27;
    public static final int SLOT_END = HOTBAR_START + 9;

    private final SimpleContainer pouchContainer = new SimpleContainer(POUCH_SLOTS) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.getItem() instanceof FocusItem;
        }
    };
    private final @Nullable ItemStack pouch;
    private final int blockedSlot;

    public FocusPouchMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null);
    }

    public FocusPouchMenu(int containerId, Inventory inventory, @Nullable ItemStack pouch) {
        super(ModMenus.FOCUS_POUCH.get(), containerId);
        this.pouch = pouch;
        this.blockedSlot = HOTBAR_START + inventory.selected;
        if (pouch != null) {
            List<ItemStack> contents = FocusPouchItem.getInventory(pouch);
            for (int slot = 0; slot < POUCH_SLOTS; slot++) {
                pouchContainer.setItem(slot, contents.get(slot));
            }
        }
        for (int slot = 0; slot < POUCH_SLOTS; slot++) {
            addSlot(new FocusSlot(pouchContainer, slot, 37 + slot % 6 * 18, 51 + slot / 6 * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 151 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 209));
        }
    }

    public int getBlockedSlot() {
        return blockedSlot;
    }

    private void save() {
        if (pouch == null) {
            return;
        }
        List<ItemStack> contents = new ArrayList<>(POUCH_SLOTS);
        for (int slot = 0; slot < POUCH_SLOTS; slot++) {
            contents.add(pouchContainer.getItem(slot));
        }
        FocusPouchItem.setInventory(pouch, contents);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            save();
            player.getInventory().setChanged();
        }
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, ClickType containerInput, Player player) {
        if (slotIndex == blockedSlot) {
            return;
        }
        if (containerInput == ClickType.SWAP && buttonNum == blockedSlot - HOTBAR_START) {
            return;
        }
        super.clicked(slotIndex, buttonNum, containerInput, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == blockedSlot) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (!(stack.getItem() instanceof FocusItem)) {
            return ItemStack.EMPTY;
        }
        ItemStack original = stack.copy();
        if (index < POUCH_SLOTS) {
            if (!moveItemStackTo(stack, POUCH_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, POUCH_SLOTS, false)) {
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
        return pouch == null || player.getInventory().getSelected().getItem() instanceof FocusPouchItem;
    }

    private static final class FocusSlot extends Slot {
        private FocusSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof FocusItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
