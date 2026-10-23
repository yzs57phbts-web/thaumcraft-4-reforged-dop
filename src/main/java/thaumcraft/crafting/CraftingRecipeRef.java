package thaumcraft.crafting;

import net.minecraft.resources.ResourceLocation;

public record CraftingRecipeRef(String key, String research, ResourceLocation recipe, TcStack result) implements TcRecipe {
}
