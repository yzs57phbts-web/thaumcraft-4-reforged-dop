package thaumcraft.aspect;

import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public interface AspectProvidingItem {
    default @Nullable AspectList getAspects(ItemStack stack) {
        AspectList aspects = AspectKeys.ASPECTS.get(stack);
        return aspects == null ? null : aspects.copy();
    }

    default void setAspects(ItemStack stack, AspectList aspects) {
        AspectKeys.ASPECTS.set(stack, aspects.copy());
    }
}
