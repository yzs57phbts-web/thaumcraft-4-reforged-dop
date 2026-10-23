package thaumcraft.wand;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface RodUpdate {
    void onUpdate(ItemStack stack, Player player);
}
