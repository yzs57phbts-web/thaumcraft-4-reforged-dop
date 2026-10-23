package thaumcraft.crafting;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.research.ResearchGate;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

public class ArcaneWandRecipe implements DynamicArcaneRecipe {
    private static final int FIRST_CAP = 6;
    private static final int SECOND_CAP = 2;
    private static final int ROD = 4;
    private static final int[] EMPTY_SLOTS = {0, 3, 1, 7, 5, 8};

    static @Nullable WandCap findCap(ItemStack stack) {
        for (WandCap cap : WandRegistry.caps()) {
            if (stack.is(BuiltInRegistries.ITEM.get(cap.item()))) {
                return cap;
            }
        }
        return null;
    }

    static @Nullable WandRod findRod(ItemStack stack) {
        for (WandRod rod : WandRegistry.rods()) {
            if (stack.is(BuiltInRegistries.ITEM.get(rod.item()))) {
                return rod;
            }
        }
        return null;
    }

    private static boolean layoutMatches(List<ItemStack> grid) {
        for (int slot : EMPTY_SLOTS) {
            if (!grid.get(slot).isEmpty()) {
                return false;
            }
        }
        ItemStack firstCap = grid.get(FIRST_CAP);
        ItemStack secondCap = grid.get(SECOND_CAP);
        return !firstCap.isEmpty() && !secondCap.isEmpty() && !grid.get(ROD).isEmpty() && firstCap.is(secondCap.getItem());
    }

    @Override
    public boolean matches(List<ItemStack> grid, Player player) {
        if (!layoutMatches(grid)) {
            return false;
        }
        WandCap cap = findCap(grid.get(FIRST_CAP));
        WandRod rod = findRod(grid.get(ROD));
        return cap != null && rod != null && ResearchGate.isComplete(player, cap.research()) && ResearchGate.isComplete(player, rod.research());
    }

    @Override
    public ItemStack result(List<ItemStack> grid) {
        if (!layoutMatches(grid)) {
            return ItemStack.EMPTY;
        }
        WandCap cap = findCap(grid.get(FIRST_CAP));
        WandRod rod = findRod(grid.get(ROD));
        if (cap == null || rod == null || rod.tag().equals("wood") && cap.tag().equals("iron")) {
            return ItemStack.EMPTY;
        }
        return Wands.create(cap, rod, false);
    }

    @Override
    public AspectList aspects(List<ItemStack> grid) {
        AspectList aspects = new AspectList();
        if (!layoutMatches(grid)) {
            return aspects;
        }
        WandCap cap = findCap(grid.get(FIRST_CAP));
        WandRod rod = findRod(grid.get(ROD));
        if (cap != null && rod != null) {
            int cost = cap.craftCost() * rod.craftCost();
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                aspects.add(aspect, cost);
            }
        }
        return aspects;
    }
}
