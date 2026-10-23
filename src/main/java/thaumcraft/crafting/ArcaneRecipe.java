package thaumcraft.crafting;

import thaumcraft.aspect.AspectList;

public sealed interface ArcaneRecipe extends TcRecipe permits ShapedArcaneRecipe, ShapelessArcaneRecipe {
    AspectList aspects();

    TcStack result();
}
