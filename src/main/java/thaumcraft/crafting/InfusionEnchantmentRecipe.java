package thaumcraft.crafting;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;

public record InfusionEnchantmentRecipe(String key, String research, ResourceLocation enchantment, int instability, AspectList aspects, List<TcIngredient> components) implements TcRecipe {
    public InfusionEnchantmentRecipe {
        components = List.copyOf(components);
    }
}
