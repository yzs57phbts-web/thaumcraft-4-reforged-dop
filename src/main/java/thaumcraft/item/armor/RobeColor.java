package thaumcraft.item.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public final class RobeColor {
    public static final int DEFAULT = 6961280;

    private RobeColor() {
    }

    public static int get(ItemStack stack) {
        DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
        return color == null ? DEFAULT : color.rgb();
    }

    public static void set(ItemStack stack, int rgb) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb, true));
    }

    public static void remove(ItemStack stack) {
        stack.remove(DataComponents.DYED_COLOR);
    }
}
