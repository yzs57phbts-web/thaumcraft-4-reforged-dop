package thaumcraft.crafting;

import java.util.List;
import thaumcraft.aspect.AspectList;

public record ShapelessArcaneRecipe(String key, String research, TcStack result, AspectList aspects, List<TcIngredient> ingredients) implements ArcaneRecipe {
    public ShapelessArcaneRecipe {
        ingredients = List.copyOf(ingredients);
    }
}
