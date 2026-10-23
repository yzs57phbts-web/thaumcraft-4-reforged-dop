package thaumcraft.crafting;

import java.util.List;
import thaumcraft.aspect.AspectList;

public record CompoundRecipe(String key, String research, AspectList aspects, int width, int height, int depth, List<TcStack> blocks) implements TcRecipe {
    public CompoundRecipe {
        blocks = List.copyOf(blocks);
    }
}
