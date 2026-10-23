package thaumcraft.research;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import thaumcraft.Thaumcraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.WarpingGear;
import thaumcraft.network.WarpMessagePayload;
import thaumcraft.platform.Baubles;
import thaumcraft.platform.ModNetwork;
import thaumcraft.research.ResearchConfig;

public final class Warp {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static final Map<ResourceLocation, Integer> WARP_ITEMS = new java.util.concurrent.ConcurrentHashMap<>(Map.of(
        Thaumcraft.id("focus_nine_hells"), 1,
        Thaumcraft.id("compass_stone"), 1,
        Thaumcraft.id("liquid_death_bucket"), 1,
        Thaumcraft.id("bottled_taint"), 1,
        Thaumcraft.id("focus_primal"), 1
    ));

    private Warp() {
    }

    public static void registerItem(ResourceLocation item, int warp) {
        WARP_ITEMS.put(item, warp);
    }

    public static int get(Player player, WarpType type) {
        return Knowledge.get(player).getWarp(type);
    }

    public static int total(Player player) {
        return Knowledge.get(player).getTotalWarp();
    }

    public static void add(ServerPlayer player, int amount, WarpType type) {
        if (amount == 0) {
            return;
        }
        PlayerKnowledge knowledge = Knowledge.get(player);
        if (amount < 0 && (type == WarpType.PERMANENT || knowledge.getWarp(type) <= 0)) {
            return;
        }
        knowledge.setWarp(type, knowledge.getWarp(type) + amount);
        knowledge.setWarpCounter(knowledge.getTotalWarp());
        Knowledge.sync(player);
        ModNetwork.sendToPlayer(player, new WarpMessagePayload(type.ordinal(), amount));
    }

    public static void set(ServerPlayer player, int amount, WarpType type) {
        Knowledge.get(player).setWarp(type, amount);
        Knowledge.sync(player);
    }

    public static int getFinalWarp(ItemStack stack, Player player) {
        return !stack.isEmpty() && stack.getItem() instanceof WarpingGear gear ? gear.getWarp(stack, player) : 0;
    }

    public static int getItemWarp(ItemStack stack) {
        return stack.isEmpty() ? 0 : WARP_ITEMS.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), 0);
    }

    public static void onItemCrafted(ServerPlayer player, ItemStack result) {
        int amount = getItemWarp(result);
        if (!ResearchConfig.WUSS_MODE && amount > 0) {
            add(player, amount, WarpType.STICKY);
        }
    }

    public static int getWarpFromGear(Player player) {
        int warp = getFinalWarp(player.getMainHandItem(), player);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            warp += getFinalWarp(player.getItemBySlot(slot), player);
        }
        for (ItemStack stack : Baubles.stacks(player)) {
            warp += getFinalWarp(stack, player);
        }
        return warp;
    }
}
