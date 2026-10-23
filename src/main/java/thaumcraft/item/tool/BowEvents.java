package thaumcraft.item.tool;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.projectile.PrimalArrow;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class BowEvents {
    private BowEvents() {
    }

    private static int level(Level level, ItemStack bow, ResourceKey<Enchantment> key) {
        Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
        return holder == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(holder, bow);
    }

    private static int findArrowSlot(Inventory inventory) {
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).getItem() instanceof PrimalArrowItem) {
                return slot;
            }
        }
        return -1;
    }

    @SubscribeEvent
    static void onArrowLoose(ArrowLooseEvent event) {
        Player player = event.getEntity();
        ItemStack bow = event.getBow();
        Level level = event.getLevel();
        int slot = findArrowSlot(player.getInventory());
        if (slot < 0 || !(bow.getItem() instanceof BowItem)) {
            return;
        }
        boolean bone = bow.getItem() instanceof BoneBowItem;
        float pow = bone ? BoneBowItem.getBonePowerForTime(event.getCharge()) : BowItem.getPowerForTime(event.getCharge());
        if (pow < 0.1) {
            return;
        }
        float damage = bone ? 2.5F : 2.0F;
        int type = ((PrimalArrowItem) player.getInventory().getItem(slot).getItem()).getArrowType();
        if (!level.isClientSide()) {
            PrimalArrow arrow = new PrimalArrow(level, player, pow * damage, type, bow);
            if (bone) {
                arrow.addDamage(0.5);
            } else if (pow == 1.0F) {
                arrow.setCritArrow(true);
            }
            int power = level(level, bow, Enchantments.POWER);
            if (power > 0) {
                arrow.addDamage(power * 0.5 + 0.5);
            }
            int punch = level(level, bow, Enchantments.PUNCH);
            if (type == 3) {
                punch++;
            }
            arrow.setKnockback(punch);
            if (level(level, bow, Enchantments.FLAME) > 0) {
                arrow.igniteForSeconds(100.0F);
            }
            bow.hurtAndBreak(1, player, ((player.getUsedItemHand()) == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND));
            boolean infinite = level(level, bow, Enchantments.INFINITY) > 0 && level.getRandom().nextFloat() < 0.33F;
            if (!player.isCreative() || !infinite) {
                player.getInventory().removeItem(slot, 1);
            }
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + pow * 0.5F);
        event.setCanceled(true);
    }
}
