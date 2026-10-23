package thaumcraft.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.arcane.DeconstructionTableBlockEntity;

public class SlotLimitedHasAspects extends Slot {
    public SlotLimitedHasAspects(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return DeconstructionTableBlockEntity.hasAspects(stack);
    }
}
