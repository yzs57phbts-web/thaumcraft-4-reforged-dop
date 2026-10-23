package thaumcraft.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.wand.Wands;

public class SlotLimitedByWand extends Slot {
    public SlotLimitedByWand(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return Wands.isWand(stack) && !Wands.isStaff(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}
