package thaumcraft.client.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;

final class TcJei {
    private TcJei() {
    }

    static void accept(IIngredientAcceptor<?> acceptor, TcIngredient ingredient) {
        List<ItemStack> stacks = ingredient.displayStacks();
        if (!stacks.isEmpty()) {
            acceptor.addItemStacks(stacks);
        }
    }

    static void accept(IIngredientAcceptor<?> acceptor, TcStack stack) {
        ItemStack created = stack.create();
        if (!created.isEmpty()) {
            acceptor.addItemStack(created);
        }
    }
}
