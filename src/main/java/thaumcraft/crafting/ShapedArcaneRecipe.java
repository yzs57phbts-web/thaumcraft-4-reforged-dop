package thaumcraft.crafting;

import java.util.List;
import thaumcraft.aspect.AspectList;

public record ShapedArcaneRecipe(String key, String research, TcStack result, AspectList aspects, int width, int height, List<TcIngredient> grid) implements ArcaneRecipe {
    public ShapedArcaneRecipe {
        grid = List.copyOf(grid);
    }
}
