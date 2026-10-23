package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public final class TcRecipes {
    private static final Map<String, TcRecipe> RECIPES = new LinkedHashMap<>();

    private TcRecipes() {
    }

    public static <T extends TcRecipe> T register(T recipe) {
        RECIPES.put(recipe.key(), recipe);
        return recipe;
    }

    public static @Nullable TcRecipe get(String key) {
        return RECIPES.get(key);
    }

    public static Collection<TcRecipe> all() {
        return Collections.unmodifiableCollection(RECIPES.values());
    }

    public static <T extends TcRecipe> List<T> all(Class<T> type) {
        List<T> matching = new ArrayList<>();
        for (TcRecipe recipe : RECIPES.values()) {
            if (type.isInstance(recipe)) {
                matching.add(type.cast(recipe));
            }
        }
        return matching;
    }
}
