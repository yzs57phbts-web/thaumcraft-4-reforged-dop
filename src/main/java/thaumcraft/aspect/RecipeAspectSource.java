package thaumcraft.aspect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import javax.annotation.Nullable;

@FunctionalInterface
public interface RecipeAspectSource {
    List<RecipeAspectSource> SOURCES = new ArrayList<>();

    static void register(RecipeAspectSource source) {
        SOURCES.add(source);
    }

    @Nullable AspectList generate(Item item, Context context);

    interface Context {
        MinecraftServer server();

        List<Item> history();

        @Nullable AspectList tags(Item item);

        AspectList fromIngredients(List<Ingredient> ingredients, int outputCount);

        AspectList fromItems(List<Item> items, int outputCount);
    }
}
