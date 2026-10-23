package thaumcraft.wand;

import net.minecraft.world.item.ItemStack;

public interface AddonWand {
    int getVisCapacity(ItemStack stack);

    default boolean canReceiveVis(ItemStack stack) {
        return true;
    }
}
