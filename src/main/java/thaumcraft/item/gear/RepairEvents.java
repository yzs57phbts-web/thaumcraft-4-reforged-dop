package thaumcraft.item.gear;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.item.armor.ArmorSupport;
import thaumcraft.item.armor.HoverHarnessItem;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.wand.WandManager;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class RepairEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private RepairEvents() {
    }

    public static void doRepair(ItemStack stack, Player player) {
        int level = Math.min(2, ArmorSupport.enchantmentLevel(player.level().registryAccess(), stack, ModEnchantments.REPAIR));
        if (level <= 0) {
            return;
        }
        AspectList tags = AspectHelper.getObjectTags(stack);
        if (tags == null || tags.size() == 0) {
            return;
        }
        AspectList primals = AspectHelper.reduceToPrimals(tags);
        AspectList cost = new AspectList();
        for (Aspect aspect : primals.getAspects()) {
            cost.merge(aspect, (int) Math.sqrt(primals.getAmount(aspect) * 2) * level);
        }
        boolean allowed = !(stack.getItem() instanceof RepairableExtended extended) || extended.doRepair(stack, player, level);
        if (allowed && WandManager.consumeVisFromInventory(player, cost)) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - level));
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0 || player.isCreative()) {
            return;
        }
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isDamaged() && stack.getItem() instanceof Repairable && !(stack.getItem() instanceof HoverHarnessItem)) {
                doRepair(stack, player);
            }
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isDamaged() && stack.getItem() instanceof Repairable) {
                doRepair(stack, player);
            }
        }
    }
}
