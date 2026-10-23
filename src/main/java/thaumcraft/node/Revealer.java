package thaumcraft.node;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface Revealer {
    boolean showNodes(ItemStack stack, LivingEntity entity);
}
