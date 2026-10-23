package thaumcraft.item.armor;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.registry.ModItems;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ArmorEvents {
    private static final AttributeModifier TRAVELLER_STEP = new AttributeModifier(Thaumcraft.id("traveller_step"), 0.4, AttributeModifier.Operation.ADD_VALUE);
    private static final EquipmentSlot[] SET_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS};

    private ArmorEvents() {
    }

    private static final Map<UUID, Boolean> FORWARD_INPUT = new HashMap<>();

    public static void setMovingForward(ServerPlayer player, boolean forward) {
        FORWARD_INPUT.put(player.getUUID(), forward);
    }

    @SubscribeEvent
    static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        FORWARD_INPUT.remove(event.getEntity().getUUID());
    }

    public static boolean movingForward(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return FORWARD_INPUT.getOrDefault(serverPlayer.getUUID(), false);
        }
        return player.zza > 0.0F;
    }

    private static void impulse(Player player, double bonus) {
        player.moveRelative((float) bonus, new Vec3(0.0, 0.0, 1.0));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        ModernArmorProtection.refreshFortressBonus(player);
        Hover.tick(player);
        tickBoots(player);
    }

    private static void tickBoots(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        boolean traveller = boots.is(ModItems.BOOTS_TRAVELLER.get());
        boolean flying = player.getAbilities().flying || player.isFallFlying();
        boolean forward = movingForward(player);
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step != null) {
            boolean active = step.hasModifier(TRAVELLER_STEP.id());
            if (traveller && !flying && forward && !player.isShiftKeyDown()) {
                if (!active) {
                    step.addTransientModifier(TRAVELLER_STEP);
                }
            } else if (active && (!traveller || flying || player.isShiftKeyDown())) {
                step.removeModifier(TRAVELLER_STEP.id());
            }
        }
        if (traveller && !flying && forward) {
            if (player.onGround()) {
                double bonus = 0.055;
                if (player.isInWater()) {
                    bonus /= 4.0;
                }
                impulse(player, bonus);
            } else {
                impulse(player, Hover.isHovering(Hover.getHarness(player)) ? 0.01 : 0.03);
            }
        }
        if (traveller && player.fallDistance > 0.0) {
            player.fallDistance -= 0.25;
        }
        if (!boots.isEmpty() && !flying && forward) {
            int haste = ArmorSupport.enchantmentLevel(player.level().registryAccess(), boots, ModEnchantments.HASTE);
            if (haste > 0) {
                double bonus = haste * 0.015;
                if (!player.onGround()) {
                    bonus /= 2.0;
                }
                if (player.isInWater()) {
                    bonus /= 2.0;
                }
                impulse(player, bonus);
            }
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player && entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.BOOTS_TRAVELLER.get())) {
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, motion.y + 0.275F, motion.z);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.onGround() && Hover.isHovering(Hover.getHarness(player))) {
            event.setNewSpeed(event.getOriginalSpeed() * 5.0F);
        }
    }

    /** Mask effects belong to an accepted hit, before runic absorption, as in TC4. */
    public static void onAcceptedDamage(LivingEntity victim, DamageSource source, float amount) {
        if (amount <= 0.0F) {
            return;
        }
        Entity attacker = source.getDirectEntity();
        if (attacker instanceof Player player && FortressArmorItem.getMask(player) == 2 && player.getRandom().nextFloat() < amount / 12.0F) {
            player.heal(1.0F);
        }
        if (victim instanceof Player player && FortressArmorItem.getMask(player) == 1) {
            if (source.getDirectEntity() instanceof LivingEntity direct && player.getRandom().nextFloat() < amount / 10.0F) {
                direct.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
            }
        }
    }

    public static double fortressSet(LivingEntity entity) {
        double set = 0.875;
        for (EquipmentSlot slot : SET_SLOTS) {
            ItemStack piece = entity.getItemBySlot(slot);
            if (piece.getItem() instanceof FortressArmorItem) {
                set += 0.125;
                if (FortressArmorItem.hasMask(piece)) {
                    set += 0.05;
                }
            }
        }
        return set;
    }

}
