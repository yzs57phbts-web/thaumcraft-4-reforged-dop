package thaumcraft.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import javax.annotation.Nullable;
import thaumcraft.registry.ModItems;

public final class SmeltingBonuses {
    private record ItemEntry(Supplier<Item> input, Supplier<Item> bonus) {
    }

    private record TagEntry(TagKey<Item> tag, Supplier<Item> bonus) {
    }

    private static final List<ItemEntry> ITEMS = new ArrayList<>();
    private static final List<TagEntry> TAGS = new ArrayList<>();
    private static final Map<Item, Item> RESOLVED = new HashMap<>();

    static {
        tag("gold", () -> Items.GOLD_NUGGET);
        tag("iron", () -> Items.IRON_NUGGET);
        tag("cinnabar", ModItems.QUICKSILVER_DROP::get);
        tag("copper", ModItems.COPPER_NUGGET::get);
        tag("tin", ModItems.TIN_NUGGET::get);
        tag("silver", ModItems.SILVER_NUGGET::get);
        tag("lead", ModItems.LEAD_NUGGET::get);
        item(ModItems.NATIVE_GOLD_CLUSTER::get, () -> Items.GOLD_NUGGET);
        item(ModItems.NATIVE_IRON_CLUSTER::get, () -> Items.IRON_NUGGET);
        item(ModItems.NATIVE_CINNABAR_CLUSTER::get, ModItems.QUICKSILVER_DROP::get);
        item(ModItems.NATIVE_COPPER_CLUSTER::get, ModItems.COPPER_NUGGET::get);
        item(ModItems.NATIVE_TIN_CLUSTER::get, ModItems.TIN_NUGGET::get);
        item(ModItems.NATIVE_SILVER_CLUSTER::get, ModItems.SILVER_NUGGET::get);
        item(ModItems.NATIVE_LEAD_CLUSTER::get, ModItems.LEAD_NUGGET::get);
        item(() -> Items.CHICKEN, ModItems.CHICKEN_NUGGET::get);
        item(() -> Items.BEEF, ModItems.BEEF_NUGGET::get);
        item(() -> Items.PORKCHOP, ModItems.PORK_NUGGET::get);
        item(() -> Items.COD, ModItems.FISH_NUGGET::get);
        item(() -> Items.SALMON, ModItems.FISH_NUGGET::get);
        item(() -> Items.TROPICAL_FISH, ModItems.FISH_NUGGET::get);
        item(() -> Items.PUFFERFISH, ModItems.FISH_NUGGET::get);
    }

    private SmeltingBonuses() {
    }

    private static void tag(String metal, Supplier<Item> bonus) {
        TAGS.add(new TagEntry(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ores/" + metal)), bonus));
    }

    private static void item(Supplier<Item> input, Supplier<Item> bonus) {
        ITEMS.add(new ItemEntry(input, bonus));
    }

    public static @Nullable Item get(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (RESOLVED.isEmpty()) {
            for (ItemEntry entry : ITEMS) {
                RESOLVED.put(entry.input().get(), entry.bonus().get());
            }
        }
        Item bonus = RESOLVED.get(stack.getItem());
        if (bonus != null) {
            return bonus;
        }
        for (TagEntry entry : TAGS) {
            if (stack.is(entry.tag())) {
                return entry.bonus().get();
            }
        }
        return null;
    }

    public static synchronized void register(Supplier<Item> input, Supplier<Item> bonus) {
        ITEMS.add(new ItemEntry(input, bonus));
        RESOLVED.clear();
    }
}
