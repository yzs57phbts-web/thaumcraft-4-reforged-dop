package thaumcraft.item.gear;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface RepairableExtended extends Repairable {
    boolean doRepair(ItemStack stack, Player player, int enchantLevel);
}
