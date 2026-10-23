package thaumcraft.crafting;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ResearchGate;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

public class ArcaneSceptreRecipe implements DynamicArcaneRecipe {
    private static final int FIRST_CAP = 1;
    private static final int SECOND_CAP = 5;
    private static final int THIRD_CAP = 6;
    private static final int ROD = 4;
    private static final int FOCUS = 2;
    private static final int[] EMPTY_SLOTS = {0, 3, 7, 8};

    private static boolean layoutMatches(List<ItemStack> grid) {
        for (int slot : EMPTY_SLOTS) {
            if (!grid.get(slot).isEmpty()) {
                return false;
            }
        }
        ItemStack firstCap = grid.get(FIRST_CAP);
        return !firstCap.isEmpty()
            && !grid.get(SECOND_CAP).isEmpty()
            && !grid.get(THIRD_CAP).isEmpty()
            && !grid.get(ROD).isEmpty()
            && grid.get(FOCUS).is(ModItems.PRIMAL_CHARM.get())
            && firstCap.is(grid.get(SECOND_CAP).getItem())
            && firstCap.is(grid.get(THIRD_CAP).getItem());
    }

    @Override
    public boolean matches(List<ItemStack> grid, Player player) {
        if (!ResearchGate.isComplete(player, "SCEPTRE") || !layoutMatches(grid)) {
            return false;
        }
        WandCap cap = ArcaneWandRecipe.findCap(grid.get(FIRST_CAP));
        WandRod rod = ArcaneWandRecipe.findRod(grid.get(ROD));
        return cap != null && rod != null && ResearchGate.isComplete(player, cap.research()) && ResearchGate.isComplete(player, rod.research());
    }

    @Override
    public ItemStack result(List<ItemStack> grid) {
        if (!layoutMatches(grid)) {
            return ItemStack.EMPTY;
        }
        WandCap cap = ArcaneWandRecipe.findCap(grid.get(FIRST_CAP));
        WandRod rod = ArcaneWandRecipe.findRod(grid.get(ROD));
        if (cap == null || rod == null) {
            return ItemStack.EMPTY;
        }
        return Wands.create(cap, rod, true);
    }

    @Override
    public AspectList aspects(List<ItemStack> grid) {
        AspectList aspects = new AspectList();
        if (!layoutMatches(grid)) {
            return aspects;
        }
        WandCap cap = ArcaneWandRecipe.findCap(grid.get(FIRST_CAP));
        WandRod rod = ArcaneWandRecipe.findRod(grid.get(ROD));
        if (cap != null && rod != null) {
            int cost = (int) (cap.craftCost() * rod.craftCost() * 1.5F);
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                aspects.add(aspect, cost);
            }
        }
        return aspects;
    }
}
