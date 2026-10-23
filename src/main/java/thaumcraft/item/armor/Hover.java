package thaumcraft.item.armor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.compat.ItemStackTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.essentia.FilledJarItem;
import thaumcraft.item.bauble.HoverGirdleItem;
import thaumcraft.platform.Baubles;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.registry.ModSounds;

public final class Hover {
    public static final int EFFICIENCY = 360;
    private static final int BELT_SLOT = 3;
    private static final AttributeModifier FLIGHT = new AttributeModifier(Thaumcraft.id("hover_flight"), 1.0, AttributeModifier.Operation.ADD_VALUE);

    private Hover() {
    }

    public static ItemStack getHarness(Player player) {
        ItemStack armor = player.getItemBySlot(EquipmentSlot.CHEST);
        return armor.getItem() instanceof HoverHarnessItem ? armor : ItemStack.EMPTY;
    }

    public static boolean isHovering(ItemStack armor) {
        return ArmorKeys.HOVER.getOrDefault(armor, false);
    }

    public static void setHovering(ItemStack armor, boolean hover) {
        if (isHovering(armor) != hover) {
            ArmorKeys.HOVER.set(armor, hover);
        }
    }

    public static ItemStack getJar(ItemStack armor) {
        ItemStackTemplate template = ArmorKeys.HARNESS_JAR.get(armor);
        return template == null ? ItemStack.EMPTY : template.create();
    }

    public static void setJar(ItemStack armor, ItemStack jar) {
        if (jar.isEmpty()) {
            ArmorKeys.HARNESS_JAR.remove(armor);
        } else {
            ArmorKeys.HARNESS_JAR.set(armor, ItemStackTemplate.fromNonEmptyStack(jar));
        }
    }

    public static int getFuel(ItemStack jar) {
        if (jar.isEmpty() || !(jar.getItem() instanceof FilledJarItem)) {
            return 0;
        }
        AspectList aspects = AspectKeys.ASPECTS.get(jar);
        return aspects == null ? 0 : aspects.getAmount(Aspect.ENERGY);
    }

    public static boolean isFuel(ItemStack jar) {
        return getFuel(jar) > 0;
    }

    public static int getHarnessFuel(ItemStack armor) {
        return getFuel(getJar(armor));
    }

    public static boolean hasGirdle(Player player) {
        return Baubles.get(player, BELT_SLOT).getItem() instanceof HoverGirdleItem;
    }

    public static boolean toggle(ServerPlayer player) {
        ItemStack armor = getHarness(player);
        if (armor.isEmpty()) {
            return false;
        }
        boolean hover = isHovering(armor);
        if (!hover && getHarnessFuel(armor) <= 0) {
            return false;
        }
        setHovering(armor, !hover);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), hover ? ModSounds.HHOFF.get() : ModSounds.HHON.get(), SoundSource.PLAYERS, 0.33F, 1.0F);
        return !hover;
    }

    public static boolean expendCharge(Player player, ItemStack armor) {
        ItemStack jar = getJar(armor);
        int fuel = getFuel(jar);
        if (fuel <= 0) {
            return false;
        }
        float modifier = hasGirdle(player) ? 0.8F : 1.0F;
        int charge = ArmorKeys.HOVER_CHARGE.getOrDefault(armor, 0);
        if (charge < EFFICIENCY * modifier) {
            ArmorKeys.HOVER_CHARGE.set(armor, charge + 1);
            return true;
        }
        ArmorKeys.HOVER_CHARGE.set(armor, 0);
        fuel--;
        AspectKeys.ASPECTS.set(jar, fuel > 0 ? new AspectList().add(Aspect.ENERGY, fuel) : new AspectList());
        setJar(armor, jar);
        return fuel > 0;
    }

    public static void tick(Player player) {
        ItemStack armor = player.isCreative() || player.isSpectator() ? ItemStack.EMPTY : getHarness(player);
        boolean hover = !armor.isEmpty() && isHovering(armor);
        updateFlight(player, hover);
        if (armor.isEmpty()) {
            return;
        }
        if (player.level().isClientSide()) {
            if (hover && player.isLocalPlayer()) {
                slowHorizontal(player, armor);
            }
            return;
        }
        if (hover && expendCharge(player, armor)) {
            player.fallDistance = 0.0F;
            if (player.tickCount % 24 == 0) {
                player.level().playSound(
                    null, player.getX(), player.getY(), player.getZ(), ModSounds.JACOBS.get(), SoundSource.PLAYERS, 0.05F, 1.0F + player.getRandom().nextFloat() * 0.05F
                );
            }
        } else {
            if (hover) {
                setHovering(armor, false);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.HHOFF.get(), SoundSource.PLAYERS, 0.33F, 1.0F);
            }
            player.fallDistance *= 0.75F;
        }
    }

    private static void updateFlight(Player player, boolean hover) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight == null) {
            return;
        }
        boolean active = flight.hasModifier(FLIGHT.id());
        if (hover && !active) {
            flight.addTransientModifier(FLIGHT);
        } else if (!hover && active) {
            flight.removeModifier(FLIGHT.id());
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        if (hover && player.level().isClientSide() && player.isLocalPlayer()) {
            player.getAbilities().flying = true;
        }
    }

    private static void slowHorizontal(Player player, ItemStack armor) {
        if (getHarnessFuel(armor) <= 0) {
            return;
        }
        int haste = ArmorSupport.enchantmentLevel(player.level().registryAccess(), armor, ModEnchantments.HASTE);
        float modifier = 0.7F + 0.075F * haste;
        if (hasGirdle(player)) {
            modifier += 0.21F;
        }
        if (modifier > 1.0F) {
            modifier = 1.0F;
        }
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x * modifier, motion.y, motion.z * modifier);
    }
}
