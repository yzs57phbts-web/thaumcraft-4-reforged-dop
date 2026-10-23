package thaumcraft.world.village;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModVillagers;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class VillageTrades {
    private static final int MAX_USES = 7;
    private static final int XP = 5;
    private static final float REPUTATION_DISCOUNT = 0.05F;

    private static final List<Trade> WIZARD = List.of(
        new Trade("thaumcraft:gold_coin", 20, 22, "minecraft:emerald", 1, 1),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:knowledge_fragment", 1, 1),
        new Trade("thaumcraft:quicksilver", 4, 6, "minecraft:emerald", 1, 1),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:alumentum", 1, 1),
        new Trade("thaumcraft:amber", 4, 6, "minecraft:emerald", 1, 1),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:nitor", 1, 1),
        new Trade("thaumcraft:chicken_nugget", 24, 31, "minecraft:emerald", 1, 1),
        new Trade("minecraft:book", 4, 6, "thaumcraft:knowledge_fragment", 1, 1),
        new Trade("thaumcraft:beef_nugget", 24, 31, "minecraft:emerald", 1, 1),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:air_shard", 2, 3),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:fire_shard", 2, 3),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:water_shard", 2, 3),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:earth_shard", 2, 3),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:order_shard", 2, 3),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:entropy_shard", 2, 3),
        new Trade("minecraft:emerald", 1, 1, "thaumcraft:mana_bean", 1, 2)
    );

    private static final List<Trade> BANKER = List.of(
        new Trade("thaumcraft:gold_coin", 20, 22, "minecraft:emerald", 1, 1),
        new Trade("thaumcraft:gold_coin", 2, 3, "minecraft:arrow", 1, 1),
        new Trade("thaumcraft:gold_coin", 6, 8, "minecraft:white_wool", 1, 1),
        new Trade("thaumcraft:gold_coin", 3, 4, "minecraft:paper", 1, 1),
        new Trade("thaumcraft:gold_coin", 7, 9, "minecraft:book", 1, 1),
        new Trade("thaumcraft:gold_coin", 16, 20, "minecraft:experience_bottle", 1, 1),
        new Trade("thaumcraft:gold_coin", 9, 12, "minecraft:glowstone", 1, 1),
        new Trade("thaumcraft:gold_coin", 2, 3, "minecraft:coal", 1, 1),
        new Trade("thaumcraft:gold_coin", 22, 24, "minecraft:diamond", 1, 1),
        new Trade("thaumcraft:gold_coin", 6, 8, "minecraft:iron_ingot", 1, 1),
        new Trade("thaumcraft:gold_coin", 10, 12, "thaumcraft:thaumium_ingot", 1, 1),
        new Trade("thaumcraft:gold_coin", 25, 32, "minecraft:saddle", 1, 1)
    );

    private VillageTrades() {
    }

    @SubscribeEvent
    static void onVillagerTrades(VillagerTradesEvent event) {
        List<Trade> trades;
        if (event.getType() == ModVillagers.WIZARD.get()) {
            trades = WIZARD;
        } else if (event.getType() == ModVillagers.BANKER.get()) {
            trades = BANKER;
        } else {
            return;
        }
        for (int level = 1; level <= 5; level++) {
            List<ItemListing> listings = event.getTrades().computeIfAbsent(level, (int key) -> new ArrayList<>());
            listings.addAll(trades);
        }
    }

    private record Trade(String wantsId, int wantsMin, int wantsMax, String givesId, int givesMin, int givesMax) implements ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            Item wants = BuiltInRegistries.ITEM.get(ResourceLocation.parse(wantsId));
            Item gives = BuiltInRegistries.ITEM.get(ResourceLocation.parse(givesId));
            int wantsCount = wantsMin == wantsMax ? wantsMin : random.nextIntBetweenInclusive(wantsMin, wantsMax);
            int givesCount = givesMin == givesMax ? givesMin : random.nextIntBetweenInclusive(givesMin, givesMax);
            return new MerchantOffer(new ItemCost(wants, wantsCount), new ItemStack(gives, givesCount), MAX_USES, XP, REPUTATION_DISCOUNT);
        }
    }
}
