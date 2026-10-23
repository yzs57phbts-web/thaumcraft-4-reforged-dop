package thaumcraft.wand;

import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.item.bauble.VisAmuletItem;

public final class VisStorage {
    private VisStorage() {
    }

    public static boolean isStorage(ItemStack stack) {
        return Wands.isWand(stack) || stack.getItem() instanceof VisAmuletItem;
    }

    public static int getMaxVis(ItemStack stack) {
        if (stack.getItem() instanceof VisAmuletItem amulet) {
            return amulet.getMaxVis();
        }
        return Wands.getMaxVis(stack);
    }

    public static int getVis(ItemStack stack, Aspect aspect) {
        return Wands.getAllVisData(stack).get(aspect);
    }

    public static void storeVis(ItemStack stack, Aspect aspect, int amount) {
        WandKeys.VIS.set(stack, Wands.getAllVisData(stack).with(aspect, amount));
    }

    public static AspectList getAllVis(ItemStack stack) {
        return Wands.getAllVisData(stack).toAspectList();
    }

    public static AspectList getAspectsWithRoom(ItemStack stack) {
        AspectList result = new AspectList();
        int max = getMaxVis(stack);
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (getVis(stack, aspect) < max) {
                result.add(aspect, 1);
            }
        }
        return result;
    }

    public static int addRealVis(ItemStack stack, Aspect aspect, int amount, boolean doit) {
        if (!aspect.isPrimal()) {
            return 0;
        }
        int max = getMaxVis(stack);
        int store = getVis(stack, aspect) + amount;
        int leftover = Math.max(store - max, 0);
        if (doit) {
            storeVis(stack, aspect, Math.min(store, max));
        }
        return leftover;
    }

    public static int addVis(ItemStack stack, Aspect aspect, int amount, boolean doit) {
        return addRealVis(stack, aspect, amount * 100, doit) / 100;
    }
}
