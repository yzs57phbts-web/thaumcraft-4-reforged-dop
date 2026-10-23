package thaumcraft.wand;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;

public interface VisDiscountGear {
    int getVisDiscount(ItemStack stack, Player player, Aspect aspect);
}
