package thaumcraft.platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface Bauble {
    BaubleType getBaubleType(ItemStack stack);

    default void onWornTick(ItemStack stack, LivingEntity wearer) {
    }

    default void onEquipped(ItemStack stack, LivingEntity wearer) {
    }

    default void onUnequipped(ItemStack stack, LivingEntity wearer) {
    }

    default boolean canEquip(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    default boolean canUnequip(ItemStack stack, LivingEntity wearer) {
        return true;
    }
}
