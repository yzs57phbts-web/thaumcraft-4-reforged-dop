package thaumcraft.device;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;

public final class SpaHooks {
    private SpaHooks() {
    }

    public static boolean isBathSalts(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.BATH_SALTS.get());
    }

    public static Block mixedFluidBlock() {
        return ModBlocks.PURIFYING_FLUID.get();
    }
}
