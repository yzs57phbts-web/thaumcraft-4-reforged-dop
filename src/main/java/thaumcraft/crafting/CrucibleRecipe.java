package thaumcraft.crafting;

import thaumcraft.aspect.AspectList;

public record CrucibleRecipe(String key, String research, TcStack result, TcIngredient catalyst, AspectList aspects) implements TcRecipe {
}
