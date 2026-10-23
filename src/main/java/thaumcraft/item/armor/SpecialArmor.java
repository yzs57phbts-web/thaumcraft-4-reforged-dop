package thaumcraft.item.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface SpecialArmor {
    record Protection(int priority, double ratio, double maximum) {
    }

    Protection protection(ItemStack stack, LivingEntity wearer, DamageSource source, EquipmentSlot slot);

    default boolean damageOnAbsorption() {
        return true;
    }
}
