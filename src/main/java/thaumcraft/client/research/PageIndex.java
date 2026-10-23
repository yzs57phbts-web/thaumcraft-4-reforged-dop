package thaumcraft.client.research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.crafting.ArcaneRecipe;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchPage;

final class PageIndex {
    private static @Nullable Map<Item, List<Entry>> index;

    private PageIndex() {
    }

    record Reference(ResearchItem research, ResearchPage page) {
    }

    private record Entry(ResearchItem research, ResearchPage page, boolean crucible) {
    }

    static @Nullable Reference find(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        List<Entry> entries = index().get(stack.getItem());
        if (entries == null) {
            return null;
        }
        for (Entry entry : entries) {
            if (ResearchManager.isComplete(player, entry.research().key())) {
                return new Reference(entry.research(), entry.page());
            }
            if (!entry.crucible()) {
                return null;
            }
        }
        return null;
    }

    private static Map<Item, List<Entry>> index() {
        if (index != null) {
            return index;
        }
        Map<Item, List<Entry>> built = new HashMap<>();
        for (ResearchItem research : ResearchCategories.allResearch()) {
            for (ResearchPage page : research.pages()) {
                switch (page) {
                    case ResearchPage.Crucible crucible -> {
                        for (String key : crucible.recipes()) {
                            if (TcRecipes.get(key) instanceof CrucibleRecipe recipe) {
                                add(built, recipe.result(), new Entry(research, page, true));
                            }
                        }
                    }
                    case ResearchPage.Crafting crafting -> addSingle(built, research, page, crafting.recipes());
                    case ResearchPage.Arcane arcane -> addSingle(built, research, page, arcane.recipes());
                    case ResearchPage.Infusion infusion -> addSingle(built, research, page, infusion.recipes());
                    case ResearchPage.Smelting smelting -> add(built, smelting.output(), new Entry(research, page, false));
                    default -> {
                    }
                }
            }
        }
        index = built;
        return built;
    }

    private static void addSingle(Map<Item, List<Entry>> built, ResearchItem research, ResearchPage page, List<String> keys) {
        for (String key : keys) {
        TcRecipe recipe = TcRecipes.get(key);
        TcStack result = switch (recipe) {
            case CraftingRecipeRef reference -> reference.result();
            case ArcaneRecipe arcane -> arcane.result();
            case InfusionRecipe infusion -> infusion.result();
            case null, default -> null;
        };
        if (result != null) {
            add(built, result, new Entry(research, page, false));
        }
        }
    }

    private static void add(Map<Item, List<Entry>> built, TcStack stack, Entry entry) {
        Item item = stack.resolve();
        if (item != null) {
            built.computeIfAbsent(item, ignored -> new ArrayList<>()).add(entry);
        }
    }
}
