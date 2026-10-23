package thaumcraft.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.item.ResearchNotesItem;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.registry.ModMenus;

public class ResearchTableMenu extends AbstractContainerMenu {
    public static final int BUTTON_DUPLICATE = 5;
    private static final int TABLE_SLOTS = 2;

    private final BlockPos pos;
    private final Container container;
    private final ResearchTableBlockEntity table;

    public ResearchTableMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(ModMenus.RESEARCH_TABLE.get(), containerId);
        this.pos = pos;
        this.table = inventory.player.level().getBlockEntity(pos) instanceof ResearchTableBlockEntity entity ? entity : null;
        this.container = table != null ? table : new SimpleContainer(TABLE_SLOTS);
        addSlot(new Slot(container, ResearchTableBlockEntity.SCRIBE_SLOT, 14, 10) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ScribingToolsItem;
            }
        });
        addSlot(new Slot(container, ResearchTableBlockEntity.NOTE_SLOT, 70, 10) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ResearchNotesItem;
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 48 + column * 18, 175 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 48 + column * 18, 233));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public ResearchTableBlockEntity table() {
        return table;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 1) {
            return true;
        }
        if (id == BUTTON_DUPLICATE) {
            if (table != null && player instanceof ServerPlayer serverPlayer) {
                table.duplicate(serverPlayer);
            }
            return true;
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
        if (index < TABLE_SLOTS) {
            if (!moveItemStackTo(stack, TABLE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, TABLE_SLOTS, false)) {
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
        return table != null && table.stillValid(player);
    }
}
