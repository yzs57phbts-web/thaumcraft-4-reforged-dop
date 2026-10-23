package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.infusion.InfusionHooks;
import thaumcraft.infusion.InfusionKeys;
import thaumcraft.research.ResearchGate;

public final class InfusionRunicAugmentRecipe {
    public static final String KEY = "RunicAugment";
    public static final String RESEARCH = "RUNICAUGMENTATION";
    public static final String DISPLAY_PREFIX = "ConfigResearch.RunicAugment";

    private InfusionRunicAugmentRecipe() {
    }

    public static boolean isDisplayRecipe(String key) {
        return key.startsWith(DISPLAY_PREFIX);
    }

    public static boolean matches(List<ItemStack> input, ItemStack central, Player player) {
        if (!ResearchGate.isComplete(player, RESEARCH)) {
            return false;
        }
        if (!InfusionHooks.isRunicArmor(central)) {
            return false;
        }
        return InfusionCrafting.matchesComponents(getComponents(central), input);
    }

    public static List<TcIngredient> getComponents(ItemStack central) {
        List<TcIngredient> components = new ArrayList<>();
        components.add(TcIngredient.item("minecraft:diamond"));
        components.add(TcIngredient.item("minecraft:shield"));
        components.add(TcIngredient.item("minecraft:shield"));
        components.add(TcIngredient.item("thaumcraft:salis_mundus"));
        int charge = InfusionHooks.getFinalCharge(central);
        for (int index = 0; index < charge; index++) {
            components.add(TcIngredient.item("thaumcraft:salis_mundus"));
        }
        return components;
    }

    public static ItemStack getOutput(ItemStack central) {
        ItemStack output = central.copy();
        InfusionKeys.HARDEN.set(output, InfusionHooks.getHardening(central) + 1);
        return output;
    }

    public static AspectList getAspects(ItemStack central) {
        AspectList aspects = new AspectList();
        int vis = (int) (32.0 * Math.pow(2.0, InfusionHooks.getFinalCharge(central)));
        if (vis > 0) {
            aspects.add(Aspect.ARMOR, vis / 2);
            aspects.add(Aspect.MAGIC, vis / 2);
            aspects.add(Aspect.ENERGY, vis);
        }
        return aspects;
    }

    public static int getInstability(ItemStack central) {
        return 5 + InfusionHooks.getFinalCharge(central) / 2;
    }
}
