package thaumcraft.menu;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.registry.ModMenus;

public class GolemMenu extends AbstractContainerMenu {
    private final @Nullable GolemBase golem;
    private final SimpleContainer filters;
    private int scroll;
    public GolemMenu(int id, Inventory inventory, int entityId) {
        super(ModMenus.GOLEM.get(), id);
        golem = inventory.player.level().getEntity(entityId) instanceof GolemBase entity ? entity : null;
        filters = golem == null ? new SimpleContainer(1) : golem.inventory();
        if (golem != null && !inventory.player.level().isClientSide()) golem.openConfiguration();
        boolean client = inventory.player.level().isClientSide();
        SimpleContainer visibleFilters = new SimpleContainer(6);
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return scroll; }
            @Override public void set(int value) { scroll = Math.clamp(value, 0, maxScroll()); }
        });
        if (golem != null && GolemBase.hasInventory(golem.getCore())) for (int slot = 0; slot < Math.min(6, filters.getContainerSize()); slot++) {
            final int visible = slot;
            addSlot(new Slot(filters, slot, 100 + slot / 2 * 28, 16 + slot % 2 * 31) {
                @Override public ItemStack getItem() { return client ? visibleFilters.getItem(visible) : filters.getItem(visible + scroll * 6); }
                @Override public void set(ItemStack stack) { if (client) visibleFilters.setItem(visible, stack); else filters.setItem(visible + scroll * 6, stack); }
                @Override public boolean mayPickup(Player player) { return false; }
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }
    public @Nullable GolemBase golem() { return golem; }
    public int currentScroll() { return scroll; }
    public int maxScroll() { return Math.max(0, (filters.getContainerSize() + 5) / 6 - 1); }
    public int filterSlots() { return slots.size() - 36; }
    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        if (!stillValid(player)) return;
        if (slot >= 0 && slot < filterSlots()) {
            if (type == ClickType.PICKUP && (button == 0 || button == 1)) {
                ItemStack held = getCarried();
                int actual = slot + scroll * 6;
                if (held.isEmpty()) {
                    ItemStack filter = filters.getItem(actual);
                    if (button == 1 && filter.getCount() > 1) filters.setItem(actual, filter.copyWithCount(filter.getCount() - 1));
                    else filters.setItem(actual, ItemStack.EMPTY);
                } else {
                    int amount = golem != null && golem.getCore() == 0 ? (button == 1 ? 1 : held.getCount()) : 1;
                    filters.setItem(actual, held.copyWithCount(amount));
                }
                broadcastChanges();
            }
            return;
        }
        if (type == ClickType.QUICK_MOVE || type == ClickType.PICKUP_ALL) return;
        super.clicked(slot, button, type, player);
    }
    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player) || golem == null) return false;
        if (button == 66 && scroll > 0) scroll--;
        else if (button == 67 && scroll < maxScroll()) scroll++;
        else if (button >= 50 && button <= 57) {
            int toggle = button - 50;
            int core = golem.getCore();
            boolean allowed = toggle == 0 && (core == 0 || core == 8)
                || toggle <= 2 && core == 8
                || toggle >= 1 && toggle <= 4 && core == 4 && golem.getUpgradeAmount(4) > 0
                || toggle >= 5 && golem.getUpgradeAmount(5) > 0 && (core == 0 || core == 1 || core == 2 || core == 8 || core == 10);
            if (!allowed) return false;
            golem.setToggle(toggle, !golem.getToggles()[toggle]);
        }
        else if (button >= 0 && button < filters.getContainerSize() * 2 && GolemBase.hasInventory(golem.getCore()) && golem.getUpgradeAmount(4) > 0) {
            int count = filters.getContainerSize();
            int slot = button % count;
            int color = golem.getColors(slot) + (button < count ? -1 : 1);
            golem.setColors(slot, color < -1 ? 15 : color > 15 ? -1 : color);
        } else return false;
        broadcastChanges();
        return true;
    }
    @Override public boolean stillValid(Player player) { return golem != null && golem.canOpen(player) && golem.inventory() == filters; }
    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
    @Override public void removed(Player player) { super.removed(player); if (golem != null && !player.level().isClientSide()) golem.closeConfiguration(); }
}
