package thaumcraft.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModItems;
import thaumcraft.wand.VisStorage;

public final class TcLoot {
    private interface Entry {
        int weight();

        ItemStack stack();
    }

    private record PlainEntry(Item item, int count, int weight) implements Entry {
        @Override
        public ItemStack stack() {
            return new ItemStack(item, count);
        }
    }

    private record PotionEntry(Item item, Holder<Potion> potion, int weight) implements Entry {
        @Override
        public ItemStack stack() {
            return PotionContents.createItemStack(item, potion);
        }
    }

    private record VisAmuletEntry(int weight) implements Entry {
        @Override
        public ItemStack stack() {
            return lootAmulet().copy();
        }
    }

    private static @Nullable ItemStack amulet;

    private static ItemStack lootAmulet() {
        if (amulet == null) {
            RandomSource random = RandomSource.create();
            amulet = new ItemStack(ModItems.VIS_AMULET.get());
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                VisStorage.addVis(amulet, aspect, random.nextInt(5), true);
            }
        }
        return amulet;
    }

    private static final String[] APPRENTICE_RINGS = {"air", "earth", "fire", "water", "order", "entropy"};

    private static final String[][] GEAR = {
        {"iron_axe", "iron_sword", "golden_axe", "golden_sword", "thaumium_sword", "diamond_sword", "void_sword"},
        {"leather_boots", "golden_boots", "chainmail_boots", "iron_boots", "thaumium_boots", "diamond_boots", "void_boots"},
        {"leather_leggings", "golden_leggings", "chainmail_leggings", "iron_leggings", "thaumium_leggings", "diamond_leggings", "void_leggings"},
        {"leather_chestplate", "golden_chestplate", "chainmail_chestplate", "iron_chestplate", "thaumium_chestplate", "diamond_chestplate", "void_chestplate"},
        {"leather_helmet", "golden_helmet", "chainmail_helmet", "iron_helmet", "thaumium_helmet", "diamond_helmet", "void_helmet"}
    };

    private static List<List<Entry>> tables;

    private TcLoot() {
    }

    public static ItemStack generateLoot(RegistryAccess registryAccess, int rarity, RandomSource random) {
        int tier = Math.max(0, Math.min(2, rarity));
        ItemStack stack = ItemStack.EMPTY;
        if (tier > 0 && random.nextFloat() < 0.025F * tier) {
            stack = generateGear(registryAccess, tier, random);
        }
        if (stack.isEmpty()) {
            stack = pick(tier, random);
        }
        if (stack.is(Items.BOOK)) {
            stack = enchant(registryAccess, random, stack, tier);
        }
        return stack;
    }

    private static ItemStack enchant(RegistryAccess registryAccess, RandomSource random, ItemStack stack, int rarity) {
        int level = (int) (5.0F + rarity * 0.75F * random.nextInt(18));
        return EnchantmentHelper.enchantItem(random, stack, level, registryAccess, Optional.empty());
    }

    private static ItemStack generateGear(RegistryAccess registryAccess, int rarity, RandomSource random) {
        int quality = random.nextInt(2);
        if (random.nextFloat() < 0.2F) {
            quality++;
        }
        if (random.nextFloat() < 0.15F) {
            quality++;
        }
        if (random.nextFloat() < 0.1F) {
            quality++;
        }
        if (random.nextFloat() < 0.095F) {
            quality++;
        }
        if (random.nextFloat() < 0.095F) {
            quality++;
        }
        String[] ladder = GEAR[random.nextInt(5)];
        for (int index = Math.min(quality, ladder.length - 1); index >= 0; index--) {
            Optional<Item> item = lookup(ladder[index]);
            if (item.isPresent()) {
                ItemStack stack = new ItemStack(item.get());
                if (stack.isDamageableItem()) {
                    stack.setDamageValue(random.nextInt(1 + stack.getMaxDamage() / 6));
                }
                if (random.nextInt(4) < rarity) {
                    stack = enchant(registryAccess, random, stack, rarity);
                }
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack pick(int tier, RandomSource random) {
        List<Entry> entries = table(tier);
        int total = 0;
        for (Entry entry : entries) {
            total += entry.weight();
        }
        if (total <= 0) {
            return ItemStack.EMPTY;
        }
        int roll = random.nextInt(total);
        for (Entry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.stack();
            }
        }
        return ItemStack.EMPTY;
    }

    private static Optional<Item> lookup(String path) {
        ResourceLocation modded = Thaumcraft.id(path);
        if (BuiltInRegistries.ITEM.containsKey(modded)) {
            return BuiltInRegistries.ITEM.getOptional(modded);
        }
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.withDefaultNamespace(path));
    }

    private static synchronized List<Entry> table(int tier) {
        if (tables == null) {
            tables = build();
        }
        return tables.get(tier);
    }

    private static List<List<Entry>> build() {
        List<List<Entry>> result = new ArrayList<>();
        for (int tier = 0; tier < 3; tier++) {
            result.add(new ArrayList<>());
        }
        add(result, "gold_coin", 1, 2500, 0);
        add(result, "gold_coin", 2, 2250, 1);
        add(result, "gold_coin", 3, 2000, 2);
        add(result, "primordial_pearl", 1, 1, 2);
        add(result, "nether_star", 1, 1, 2);
        add(result, "diamond", 1, 10, 0);
        add(result, "diamond", 1, 50, 1, 2);
        add(result, "emerald", 1, 15, 0);
        add(result, "emerald", 1, 75, 1, 2);
        add(result, "gold_ingot", 1, 100, 0, 1, 2);
        add(result, "ender_pearl", 1, 100, 0, 1, 2);
        add(result, "knowledge_fragment", 1, 25, 0, 1, 2);
        add(result, "mundane_amulet", 1, 10, 0);
        add(result, "mundane_ring", 1, 10, 0);
        add(result, "mundane_belt", 1, 10, 0);
        for (String element : APPRENTICE_RINGS) {
            add(result, "apprentice_ring_" + element, 1, 5, 1);
            add(result, "apprentice_ring_" + element, 1, 7, 2);
        }
        result.get(1).add(new VisAmuletEntry(6));
        result.get(2).add(new VisAmuletEntry(6));
        add(result, "protection_ring", 1, 5, 1, 2);
        add(result, "experience_bottle", 1, 5, 0);
        add(result, "experience_bottle", 1, 10, 1);
        add(result, "experience_bottle", 1, 20, 2);
        add(result, "enchanted_golden_apple", 1, 1, 0);
        add(result, "enchanted_golden_apple", 1, 2, 1);
        add(result, "enchanted_golden_apple", 1, 3, 2);
        add(result, "golden_apple", 1, 3, 0);
        add(result, "golden_apple", 1, 6, 1);
        add(result, "golden_apple", 1, 9, 2);
        add(result, "book", 1, 10, 0, 1, 2);
        addPotions(result);
        return result;
    }

    private static void add(List<List<Entry>> result, String path, int count, int weight, int... tiers) {
        Optional<Item> item = lookup(path);
        if (item.isEmpty()) {
            return;
        }
        for (int tier : tiers) {
            result.get(tier).add(new PlainEntry(item.get(), count, weight));
        }
    }

    private static void addPotions(List<List<Entry>> result) {
        for (Item container : new Item[] {Items.POTION, Items.SPLASH_POTION}) {
            BuiltInRegistries.POTION.holders().forEach(holder -> {
                if (holder.value().getEffects().isEmpty()) {
                    return;
                }
                String path = holder.unwrapKey().map(key -> key.location().getPath()).orElse("");
                int weight = path.startsWith("long_") ? 3 : path.startsWith("strong_") ? 2 : 1;
                for (List<Entry> table : result) {
                    table.add(new PotionEntry(container, holder, weight));
                }
            });
        }
    }
}
