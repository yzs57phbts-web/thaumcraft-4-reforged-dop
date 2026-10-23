package thaumcraft.crafting;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectList;

public interface DynamicArcaneRecipe {
    boolean matches(List<ItemStack> grid, Player player);

    ItemStack result(List<ItemStack> grid);

    AspectList aspects(List<ItemStack> grid);
}
