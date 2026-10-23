package thaumcraft.item.armor;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import javax.annotation.Nullable;

public final class ArmorSupport {
    private ArmorSupport() {
    }

    public static @Nullable EquipmentSlot slotOf(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem armor ? armor.getEquipmentSlot() : null;
    }

    public static double defense(ItemStack stack, EquipmentSlot slot) {
        double defense = 0.0;
        for (var entry : stack.getAttributeModifiers().modifiers()) {
            if (entry.attribute().is(Attributes.ARMOR.unwrapKey().orElseThrow()) && entry.slot().test(slot)
                && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                defense += entry.modifier().amount();
            }
        }
        return defense;
    }

    public static void visDiscountLine(List<Component> builder, String suffix, int discount) {
        builder.add(Component.translatable("tc.visdiscount").append(suffix + ": " + discount + "%").withStyle(ChatFormatting.DARK_PURPLE));
    }

    public static void visDiscountLine(List<Component> builder, int discount) {
        visDiscountLine(builder, "", discount);
    }

    public static int enchantmentLevel(RegistryAccess access, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return access.lookupOrThrow(Registries.ENCHANTMENT)
            .get(key)
            .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
            .orElse(0);
    }

    public static void selfRepair(ItemStack stack, Entity owner) {
        if (owner.level().isClientSide() || !(owner instanceof LivingEntity) || !stack.isDamaged() || owner.tickCount % 20 != 0) {
            return;
        }
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
    }
}
