package thaumcraft.platform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.item.armor.ArmorSupport;
import thaumcraft.item.armor.SpecialArmor;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class SpecialArmorDamage {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private record Piece(ItemStack stack, EquipmentSlot slot, SpecialArmor armor, SpecialArmor.Protection protection) {
    }

    private SpecialArmorDamage() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        List<Piece> pieces = new ArrayList<>();
        double excludedArmor = 0.0;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof SpecialArmor armor && ArmorSupport.slotOf(stack) == slot) {
                pieces.add(new Piece(stack, slot, armor, armor.protection(stack, entity, event.getSource(), slot)));
                excludedArmor += ArmorSupport.defense(stack, slot);
            }
        }
        if (pieces.isEmpty()) {
            return;
        }
        pieces.sort(Comparator.comparingInt((Piece piece) -> piece.protection().priority()).reversed());
        float ordinaryArmor = (float) Math.max(0.0, entity.getArmorValue() - excludedArmor);
        event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> {
            double remaining = container.getNewDamage();
            for (int priority = Math.max(0, pieces.getFirst().protection().priority()); priority >= 0 && remaining > 0.0; priority--) {
                double ordinaryReduction = 0.0;
                if (priority == 0 && ordinaryArmor > 0.0F && !container.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
                    ordinaryReduction = remaining - CombatRules.getDamageAfterAbsorb(
                        entity, (float) remaining, container.getSource(), ordinaryArmor, (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS)
                    );
                }
                double absorbed = ordinaryReduction;
                for (Piece piece : pieces) {
                    if (piece.protection().priority() == priority) {
                        absorbed += Math.min(remaining * piece.protection().ratio(), piece.protection().maximum());
                    }
                }
                double scale = absorbed > remaining ? remaining / absorbed : 1.0;
                for (Piece piece : pieces) {
                    if (piece.protection().priority() != priority || !piece.armor().damageOnAbsorption() || container.getSource().is(DamageTypes.DROWN)) {
                        continue;
                    }
                    double share = Math.min(remaining * piece.protection().ratio(), piece.protection().maximum()) * scale;
                    if (share > 0.0) {
                        piece.stack().hurtAndBreak(Math.max(1, (int) share), entity, piece.slot());
                    }
                }
                remaining = Math.max(0.0, remaining - absorbed);
            }
            return (float) (container.getNewDamage() - remaining);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onArmorHurt(ArmorHurtEvent event) {
        for (EquipmentSlot slot : event.getArmorMap().keySet()) {
            if (event.getArmorItemStack(slot).getItem() instanceof SpecialArmor) {
                event.setNewDamage(slot, 0.0F);
            }
        }
    }
}
