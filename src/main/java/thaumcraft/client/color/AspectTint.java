package thaumcraft.client.color;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;

public final class AspectTint implements ItemColor {
    public static final AspectTint INSTANCE = new AspectTint();

    private AspectTint() {
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        AspectList aspects = AspectKeys.ASPECTS.getOrDefault(stack, AspectList.EMPTY);
        if (aspects.isEmpty()) {
            return 0xFFFFFFFF;
        }
        return FastColor.ARGB32.opaque(aspects.getAspects().get(0).getColor());
    }
}
