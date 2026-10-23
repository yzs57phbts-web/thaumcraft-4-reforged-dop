package thaumcraft.menu;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.TravelingTrunk;
import thaumcraft.registry.ModMenus;

public class TravelingTrunkMenu extends AbstractContainerMenu {
    private final @Nullable TravelingTrunk trunk;
    private final SimpleContainer contents;
    private final int rows;
    public TravelingTrunkMenu(int id, Inventory inventory, int entityId) {
        super(ModMenus.TRAVELING_TRUNK.get(), id);
        trunk = inventory.player.level().getEntity(entityId) instanceof TravelingTrunk entity ? entity : null;
        rows = trunk == null ? 3 : trunk.getRows();
        contents = trunk == null ? new SimpleContainer(rows * 9) : trunk.inventory();
        for (int row = 0; row < rows; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(contents, col + row * 9, 8 + col * 18, 15 + row * 23));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 176));
        if (trunk != null && !inventory.player.level().isClientSide()) { trunk.setOpen(true); trunk.playSound(SoundEvents.CHEST_OPEN, 0.5F, 0.9F + trunk.getRandom().nextFloat() * 0.1F); }
    }
    public @Nullable TravelingTrunk trunk() { return trunk; }
    public int numberOfRows() { return rows; }
    @Override public boolean stillValid(Player player) { return trunk != null && trunk.canOpen(player) && trunk.inventory() == contents; }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player) || button != 1) return false;
        trunk.setStay(!trunk.getStay());
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int end = rows * 9;
        if (index < end ? !moveItemStackTo(stack, end, slots.size(), true) : !moveItemStackTo(stack, 0, end, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }
    @Override public void removed(Player player) { super.removed(player); if (trunk != null && !player.level().isClientSide()) { trunk.setOpen(false); trunk.playSound(SoundEvents.CHEST_CLOSE, 0.5F, 0.9F + trunk.getRandom().nextFloat() * 0.1F); } }
}
