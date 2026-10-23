package thaumcraft.item.armor;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import thaumcraft.Thaumcraft;

/** Vanilla physical armor and wear, plus TC4's explicitly scoped equipment bonuses. */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ModernArmorProtection {
    public static final TagKey<DamageType> MAGIC = TagKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("armor_magic"));
    private static final EquipmentSlot[] SET_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS};
    private static final net.minecraft.resources.ResourceLocation FORTRESS_BONUS = Thaumcraft.id("fortress_set_armor");

    private ModernArmorProtection() {
    }

    private static boolean correctSlot(ItemStack stack, EquipmentSlot slot) {
        return ArmorSupport.slotOf(stack) == slot;
    }

    private static int baseDefense(ItemStack stack) {
        return ((ArmorItem) stack.getItem()).getDefense();
    }

    /** Round only the set's extra defense; never multiply unrelated boots or armor. */
    public static int fortressBonus(Player player) {
        int pieces = 0;
        int defense = 0;
        for (EquipmentSlot slot : SET_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof FortressArmorItem && correctSlot(stack, slot)) {
                pieces++;
                defense += baseDefense(stack);
            }
        }
        int bonus = Math.round(defense * Math.max(0, pieces - 1) * 0.125F);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof FortressArmorItem && FortressArmorItem.hasMask(helmet)) {
            bonus++;
        }
        return bonus;
    }

    public static void refreshFortressBonus(Player player) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor == null) {
            return;
        }
        int bonus = fortressBonus(player);
        AttributeModifier current = armor.getModifier(FORTRESS_BONUS);
        if (current != null && current.amount() == bonus) {
            return;
        }
        if (current != null) {
            armor.removeModifier(FORTRESS_BONUS);
        }
        if (bonus > 0) {
            armor.addTransientModifier(new AttributeModifier(FORTRESS_BONUS, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            refreshFortressBonus(player);
        }
    }

    public static float magicProtection(LivingEntity wearer) {
        int fortressDefense = 0;
        int robeDefense = 0;
        int fortressPieces = 0;
        for (EquipmentSlot slot : SET_SLOTS) {
            ItemStack stack = wearer.getItemBySlot(slot);
            if (!correctSlot(stack, slot)) {
                continue;
            }
            if (stack.getItem() instanceof FortressArmorItem) {
                fortressDefense += baseDefense(stack);
                fortressPieces++;
            } else if (stack.getItem() instanceof VoidRobeArmorItem) {
                robeDefense += baseDefense(stack);
            }
        }
        double multiplier = 1.0;
        if (wearer instanceof Player && fortressPieces > 0) {
            multiplier = 0.875 + fortressPieces * 0.125;
            ItemStack helmet = wearer.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() instanceof FortressArmorItem && FortressArmorItem.hasMask(helmet)) {
                multiplier += 0.05;
            }
        }
        return Mth.clamp((float) ((fortressDefense * multiplier + robeDefense) / 35.0), 0.0F, 1.0F);
    }

    public static float fireExplosionProtection(LivingEntity wearer) {
        int defense = 0;
        for (EquipmentSlot slot : SET_SLOTS) {
            ItemStack stack = wearer.getItemBySlot(slot);
            if (stack.getItem() instanceof FortressArmorItem && correctSlot(stack, slot)) {
                defense += baseDefense(stack);
            }
        }
        return 0.25F * defense / 16.0F;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getNewDamage() <= 0.0F) {
            return;
        }
        float protection;
        if (event.getSource().is(MAGIC)) {
            protection = magicProtection(event.getEntity());
        } else if (event.getSource().is(DamageTypeTags.IS_FIRE) || event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            protection = fireExplosionProtection(event.getEntity());
        } else {
            return;
        }
        event.setNewDamage(event.getNewDamage() * (1.0F - protection));
    }
}
