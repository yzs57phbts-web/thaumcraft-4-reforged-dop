package thaumcraft.client.jei;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.ArcaneRecipe;
import thaumcraft.crafting.ArcaneSceptreRecipe;
import thaumcraft.crafting.ArcaneWandRecipe;
import thaumcraft.crafting.DynamicArcaneRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.ShapelessArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.registry.ModItems;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;

record ArcaneDisplay(List<TcIngredient> ingredients, int width, boolean shapeless, ItemStack result, AspectList aspects) {
    ArcaneDisplay {
        ingredients = List.copyOf(ingredients);
    }

    static ArcaneDisplay of(ArcaneRecipe recipe) {
        if (recipe instanceof ShapedArcaneRecipe shaped) {
            return new ArcaneDisplay(shaped.grid(), shaped.width(), false, recipe.result().create(), recipe.aspects());
        }
        return new ArcaneDisplay(((ShapelessArcaneRecipe) recipe).ingredients(), 3, true, recipe.result().create(), recipe.aspects());
    }

    static List<ArcaneDisplay> wandRecipes() {
        List<ArcaneDisplay> recipes = new ArrayList<>();
        for (WandCap cap : WandRegistry.caps()) {
            if (!thaumcraft.modern.MetalIntegration.itemAvailable(cap.item().getPath())) continue;
            for (WandRod rod : WandRegistry.rods()) {
                ItemStack capStack = new ItemStack(BuiltInRegistries.ITEM.get(cap.item()));
                ItemStack rodStack = new ItemStack(BuiltInRegistries.ITEM.get(rod.item()));
                if (capStack.isEmpty() || rodStack.isEmpty()) {
                    continue;
                }
                for (boolean sceptre : new boolean[]{false, true}) {
                    List<TcIngredient> ingredients = new ArrayList<>();
                    List<ItemStack> grid = new ArrayList<>();
                    for (int slot = 0; slot < 9; slot++) {
                        ingredients.add(TcIngredient.EMPTY);
                        grid.add(ItemStack.EMPTY);
                    }
                    int[] caps = sceptre ? new int[]{1, 5, 6} : new int[]{2, 6};
                    for (int slot : caps) {
                        ingredients.set(slot, TcIngredient.item(cap.item().toString()));
                        grid.set(slot, capStack.copy());
                    }
                    ingredients.set(4, TcIngredient.item(rod.item().toString()));
                    grid.set(4, rodStack);
                    if (sceptre) {
                        TcIngredient charm = TcIngredient.item("thaumcraft:primal_charm");
                        ingredients.set(2, charm);
                        grid.set(2, new ItemStack(ModItems.PRIMAL_CHARM.get()));
                    }
                    DynamicArcaneRecipe recipe = sceptre ? new ArcaneSceptreRecipe() : new ArcaneWandRecipe();
                    ItemStack result = recipe.result(grid);
                    if (!result.isEmpty()) {
                        recipes.add(new ArcaneDisplay(ingredients, 3, false, result, recipe.aspects(grid)));
                    }
                }
            }
        }
        return recipes;
    }
}
