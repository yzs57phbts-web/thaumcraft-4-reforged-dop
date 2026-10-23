package thaumcraft.crafting;

import java.util.List;
import thaumcraft.aspect.AspectList;

public record InfusionRecipe(String key, String research, TcStack result, int instability, AspectList aspects, TcIngredient central, List<TcIngredient> components) implements TcRecipe {
    public InfusionRecipe {
        components = List.copyOf(components);
    }
}
