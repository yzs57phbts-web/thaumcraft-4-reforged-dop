package thaumcraft.client.research;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crafting.*;
import thaumcraft.research.*;

/** Reuses the book's real recipe rendering in addon screens without copying its layout. */
public final class RecipePageRenderer {
    public record Link(ResearchItem research, ResearchPage page) {}

    public static Link find(Player player, ItemStack stack) {
        var link = PageIndex.find(player, stack);
        return link == null ? null : new Link(link.research(), link.page());
    }

    public static boolean render(GuiGraphics graphics, String key, int x, int y, int cycle) {
        var renderer = new PageRenderer(Minecraft.getInstance(), new ResearchItem("RECIPE_DISPLAY", "ARTIFICE"));
        renderer.begin(graphics, -1000, -1000);
        renderer.cycle = cycle;
        switch (TcRecipes.get(key)) {
            case InfusionRecipe ignored -> PageRecipes.infusion(renderer, 0, x, y, new ResearchPage.Infusion(java.util.List.of(key)));
            case ArcaneRecipe ignored -> PageRecipes.arcane(renderer, 0, x, y, new ResearchPage.Arcane(java.util.List.of(key)));
            case CraftingRecipeRef ignored -> PageRecipes.crafting(renderer, 0, x, y, new ResearchPage.Crafting(java.util.List.of(key)));
            case CrucibleRecipe ignored -> PageRecipes.crucible(renderer, 0, x, y, new ResearchPage.Crucible(java.util.List.of(key)));
            case null, default -> { return false; }
        }
        return true;
    }
}
