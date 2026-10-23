package thaumcraft.item.gear;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface Goggles {
    boolean showIngamePopups(ItemStack stack, LivingEntity entity);
}
