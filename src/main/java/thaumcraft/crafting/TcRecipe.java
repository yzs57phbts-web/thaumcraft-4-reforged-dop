package thaumcraft.crafting;

public sealed interface TcRecipe permits ArcaneRecipe, CrucibleRecipe, InfusionRecipe, InfusionEnchantmentRecipe, CompoundRecipe, CraftingRecipeRef {
    String key();

    String research();
}
