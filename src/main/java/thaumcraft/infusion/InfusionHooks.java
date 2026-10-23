package thaumcraft.infusion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.research.Warp;
import thaumcraft.research.ResearchConfig;
import thaumcraft.research.WarpType;

public final class InfusionHooks {
    private InfusionHooks() {
    }

    public static boolean isRunicArmor(ItemStack stack) {
        return stack.getItem() instanceof RunicArmor;
    }

    public static int baseRunicCharge(ItemStack stack) {
        return stack.getItem() instanceof RunicArmor armor ? armor.getRunicCharge(stack) : 0;
    }

    public static int getHardening(ItemStack stack) {
        if (!isRunicArmor(stack)) {
            return 0;
        }
        return InfusionKeys.HARDEN.getOrDefault(stack, 0);
    }

    public static int getFinalCharge(ItemStack stack) {
        if (!isRunicArmor(stack)) {
            return 0;
        }
        return baseRunicCharge(stack) + getHardening(stack);
    }

    public static void onInfusionCrafted(ServerPlayer player, ItemStack result) {
        int amount = Warp.getItemWarp(result);
        if (!ResearchConfig.WUSS_MODE && amount > 0) {
            Warp.add(player, amount, WarpType.STICKY);
        }
    }
}
