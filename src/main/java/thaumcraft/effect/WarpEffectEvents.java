package thaumcraft.effect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModEffects;
import thaumcraft.registry.ModItems;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class WarpEffectEvents {
    private static final Map<LivingEntity, Set<String>> UNCURED_EFFECTS = Collections.synchronizedMap(new WeakHashMap<>());

    private WarpEffectEvents() {
    }

    @SubscribeEvent
    static void finishedUsingItem(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !(entity instanceof Player player) || !player.hasEffect(ModEffects.UNNATURAL_HUNGER)) {
            return;
        }

        ItemStack consumed = event.getItem();
        if (consumed.is(Items.ROTTEN_FLESH) || consumed.is(ModItems.ZOMBIE_BRAIN)) {
            reduceUnnaturalHunger(player);
            player.sendSystemMessage(Component.translatable("warp.text.hunger.2").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
        } else if (consumed.has(DataComponents.FOOD)) {
            player.sendSystemMessage(Component.translatable("warp.text.hunger.1").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        }
    }

    @SubscribeEvent
    static void preventMilkCure(MobEffectEvent.Remove event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || !isUncurable(event.getEntity(), instance)) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.isUsingItem() && entity.getUseItem().is(Items.MILK_BUCKET)) {
            event.setCanceled(true);
        } else {
            clearUncurable(entity, instance);
        }
    }

    @SubscribeEvent
    static void effectExpired(MobEffectEvent.Expired event) {
        clearUncurable(event.getEntity(), event.getEffectInstance());
    }

    public static void addUncurableEffect(LivingEntity entity, MobEffectInstance instance) {
        MobEffectInstance existing = entity.getEffect(instance.getEffect());
        if (entity.addEffect(instance) && existing == null) {
            markUncurable(entity, instance);
        }
    }

    private static boolean isUncurable(LivingEntity entity, MobEffectInstance instance) {
        synchronized (UNCURED_EFFECTS) {
            return UNCURED_EFFECTS.getOrDefault(entity, Set.of()).contains(effectKey(instance));
        }
    }

    private static void markUncurable(LivingEntity entity, MobEffectInstance instance) {
        synchronized (UNCURED_EFFECTS) {
            Set<String> effectKeys = new HashSet<>(UNCURED_EFFECTS.getOrDefault(entity, Set.of()));
            effectKeys.add(effectKey(instance));
            UNCURED_EFFECTS.put(entity, effectKeys);
        }
    }

    private static void clearUncurable(LivingEntity entity, MobEffectInstance instance) {
        synchronized (UNCURED_EFFECTS) {
            Set<String> effectKeys = new HashSet<>(UNCURED_EFFECTS.getOrDefault(entity, Set.of()));
            effectKeys.remove(effectKey(instance));
            if (effectKeys.isEmpty()) {
                UNCURED_EFFECTS.remove(entity);
            } else {
                UNCURED_EFFECTS.put(entity, effectKeys);
            }
        }
    }

    private static String effectKey(MobEffectInstance instance) {
        return instance.getEffect().unwrapKey().map(key -> key.location().toString()).orElse("");
    }

    private static void reduceUnnaturalHunger(LivingEntity entity) {
        MobEffectInstance current = entity.getEffect(ModEffects.UNNATURAL_HUNGER);
        if (current == null) {
            return;
        }
        int amplifier = current.getAmplifier() - 1;
        int duration = current.getDuration() - 600;
        entity.removeEffect(ModEffects.UNNATURAL_HUNGER);
        if (duration > 0 && amplifier >= 0) {
            addUncurableEffect(entity, new MobEffectInstance(ModEffects.UNNATURAL_HUNGER, duration, amplifier, true, true, true));
        }
    }
}
