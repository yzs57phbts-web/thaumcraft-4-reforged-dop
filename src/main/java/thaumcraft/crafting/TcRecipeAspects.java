package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.RecipeAspectSource;

public final class TcRecipeAspects {
    private static boolean registered;

    private TcRecipeAspects() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        RecipeAspectSource.register(TcRecipeAspects::fromCrucible);
        RecipeAspectSource.register(TcRecipeAspects::fromArcane);
        RecipeAspectSource.register(TcRecipeAspects::fromInfusion);
    }

    private static @Nullable Item representative(TcIngredient ingredient, RecipeAspectSource.Context context, boolean requireTags) {
        for (ItemStack stack : ingredient.displayStacks()) {
            if (!requireTags) {
                return stack.getItem();
            }
            AspectList tags = context.tags(stack.getItem());
            if (tags != null && tags.size() > 0) {
                return stack.getItem();
            }
        }
        return null;
    }

    private static void addSquareRoots(AspectList out, AspectList vis, int outputCount) {
        for (Aspect aspect : vis.getAspects()) {
            out.add(aspect, (int) (Math.sqrt(vis.getAmount(aspect)) / outputCount));
        }
    }

    private static AspectList clean(AspectList list) {
        for (Aspect aspect : list.copy().getAspects()) {
            if (list.getAmount(aspect) <= 0) {
                list.remove(aspect);
            }
        }
        return list;
    }

    private static @Nullable AspectList fromCrucible(Item item, RecipeAspectSource.Context context) {
        for (CrucibleRecipe recipe : TcRecipes.all(CrucibleRecipe.class)) {
            if (recipe.result().resolve() != item) {
                continue;
            }
            Item catalyst = representative(recipe.catalyst(), context, false);
            AspectList out = new AspectList();
            if (catalyst != null) {
                AspectList base = context.tags(catalyst);
                if (base != null) {
                    for (Aspect aspect : base.getAspects()) {
                        out.add(aspect, base.getAmount(aspect));
                    }
                }
            }
            addSquareRoots(out, recipe.aspects(), Math.max(1, recipe.result().count()));
            return clean(out);
        }
        return null;
    }

    private static @Nullable AspectList fromArcane(Item item, RecipeAspectSource.Context context) {
        AspectList found = null;
        for (ArcaneRecipe recipe : TcRecipes.all(ArcaneRecipe.class)) {
            if (recipe.result().resolve() != item) {
                continue;
            }
            List<TcIngredient> inputs = switch (recipe) {
                case ShapedArcaneRecipe shaped -> shaped.grid();
                case ShapelessArcaneRecipe shapeless -> shapeless.ingredients();
            };
            List<Item> items = new ArrayList<>();
            for (TcIngredient input : inputs) {
                if (input == TcIngredient.EMPTY) {
                    continue;
                }
                Item resolved = representative(input, context, !(input instanceof TcIngredient.Stack));
                if (resolved != null) {
                    items.add(resolved);
                }
            }
            int count = Math.max(1, recipe.result().count());
            AspectList out = context.fromItems(items, count);
            addSquareRoots(out, recipe.aspects(), count);
            found = clean(out);
        }
        return found;
    }

    private static @Nullable AspectList fromInfusion(Item item, RecipeAspectSource.Context context) {
        for (InfusionRecipe recipe : TcRecipes.all(InfusionRecipe.class)) {
            if (recipe.result().resolve() != item || !recipe.result().data().isEmpty()) {
                continue;
            }
            List<Item> items = new ArrayList<>();
            Item central = representative(recipe.central(), context, false);
            if (central != null) {
                items.add(central);
            }
            for (TcIngredient component : recipe.components()) {
                Item resolved = representative(component, context, false);
                if (resolved != null) {
                    items.add(resolved);
                }
            }
            int count = Math.max(1, recipe.result().count());
            AspectList out = context.fromItems(items, count);
            addSquareRoots(out, recipe.aspects(), count);
            return clean(out);
        }
        return null;
    }
}
