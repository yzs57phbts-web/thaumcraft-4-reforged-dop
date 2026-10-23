package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.research.ResearchManager;

public final class CrucibleCrafting {
    private CrucibleCrafting() {
    }

    public static @Nullable CrucibleRecipe find(@Nullable Player player, AspectList aspects, ItemStack catalyst) {
        int highest = 0;
        CrucibleRecipe found = null;
        ItemStack single = catalyst.copyWithCount(1);
        for (CrucibleRecipe recipe : TcRecipes.all(CrucibleRecipe.class)) {
            if (isResearched(player, recipe) && matches(recipe, aspects, single)) {
                int result = recipe.aspects().size();
                if (result > highest) {
                    highest = result;
                    found = recipe;
                }
            }
        }
        return found;
    }

    public static boolean isResearched(@Nullable Player player, CrucibleRecipe recipe) {
        if (recipe.research().isEmpty()) {
            return true;
        }
        return player != null && ResearchManager.isComplete(player, recipe.research());
    }

    public static boolean catalystMatches(CrucibleRecipe recipe, ItemStack catalyst) {
        return !catalyst.isEmpty() && recipe.catalyst().test(catalyst);
    }

    public static boolean matches(CrucibleRecipe recipe, @Nullable AspectList aspects, ItemStack catalyst) {
        if (!catalystMatches(recipe, catalyst) || aspects == null) {
            return false;
        }
        for (Aspect aspect : recipe.aspects().getAspects()) {
            if (aspects.getAmount(aspect) < recipe.aspects().getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }

    public static AspectList removeMatching(CrucibleRecipe recipe, AspectList aspects) {
        AspectList remaining = aspects.copy();
        for (Aspect aspect : recipe.aspects().getAspects()) {
            remaining.remove(aspect, recipe.aspects().getAmount(aspect));
        }
        return remaining;
    }

    public static List<CrucibleRecipe> available(Player player, ItemStack catalyst) {
        List<CrucibleRecipe> recipes = new ArrayList<>();
        for (CrucibleRecipe recipe : TcRecipes.all(CrucibleRecipe.class)) {
            if (isResearched(player, recipe) && catalystMatches(recipe, catalyst)) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    public static AspectList dissolveTags(ItemStack stack) {
        return AspectHelper.getBonusTags(stack, AspectHelper.getObjectTags(stack));
    }

    public static boolean hasDissolveTags(ItemStack stack) {
        return !stack.isEmpty() && !dissolveTags(stack).isEmpty();
    }
}
