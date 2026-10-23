package thaumcraft.crafting;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import javax.annotation.Nullable;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.device.WardedKeys;
import thaumcraft.registry.ModBlocks;
import thaumcraft.wand.WandKeys;
import thaumcraft.wand.WandParts;

public record TcStack(ResourceLocation item, int count, @Nullable AspectList aspects, Map<String, String> data) {
    private static final Map<String, BiConsumer<ItemStack, String>> DATA_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<String, BiPredicate<ItemStack, String>> DATA_MATCHERS = new ConcurrentHashMap<>();

    public static final TcStack NONE = new TcStack(ResourceLocation.parse("minecraft:air"), 0, null, Map.of());

    public TcStack {
        data = Map.copyOf(data);
    }

    public TcStack(ResourceLocation item, int count, @Nullable AspectList aspects) {
        this(item, count, aspects, Map.of());
    }

    public static void registerDataHandler(String key, BiConsumer<ItemStack, String> handler) {
        DATA_HANDLERS.put(key, handler);
    }

    public static void registerDataMatcher(String key, BiPredicate<ItemStack, String> matcher) {
        DATA_MATCHERS.put(key, matcher);
    }

    public static TcStack of(String item) {
        return new TcStack(ResourceLocation.parse(item), 1, null);
    }

    public static TcStack of(String item, int count) {
        return new TcStack(ResourceLocation.parse(item), count, null);
    }

    public static TcStack withAspects(String item, int count, AspectList aspects) {
        return new TcStack(ResourceLocation.parse(item), count, aspects);
    }

    public static TcStack withData(String item, int count, Map<String, String> data) {
        return new TcStack(ResourceLocation.parse(item), count, null, data);
    }

    public @Nullable Item resolve() {
        Item resolved = BuiltInRegistries.ITEM.get(item);
        return resolved == Items.AIR ? null : resolved;
    }

    public boolean exists() {
        return resolve() != null;
    }

    public ItemStack create() {
        Item resolved = resolve();
        if (resolved == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(resolved, count);
        if (aspects != null) {
            AspectKeys.ASPECTS.set(stack, aspects.copy());
        }
        if (data.containsKey("cap") && data.containsKey("rod")) {
            WandKeys.PARTS.set(stack, new WandParts(data.get("cap"), data.get("rod"), false));
        }
        if (data.containsKey("color")) {
            int color = Arrays.asList(ModBlocks.CANDLE_COLORS).indexOf(data.get("color"));
            if (color >= 0) {
                WardedKeys.BANNER_COLOR.set(stack, color);
            }
        }
        applyDataHandlers(stack);
        return stack;
    }

    public boolean hasDataHandler() {
        return data.keySet().stream().anyMatch(DATA_HANDLERS::containsKey);
    }

    private void applyDataHandlers(ItemStack stack) {
        data.forEach((key, value) -> {
            BiConsumer<ItemStack, String> handler = DATA_HANDLERS.get(key);
            if (handler != null) {
                handler.accept(stack, value);
            }
        });
    }

    public boolean matches(ItemStack stack) {
        Item resolved = resolve();
        if (resolved == null || !stack.is(resolved)) {
            return false;
        }
        if (data.containsKey("cap") && data.containsKey("rod")) {
            WandParts parts = WandKeys.PARTS.getOrDefault(stack, WandParts.DEFAULT);
            if (!parts.cap().equals(data.get("cap")) || !parts.rod().equals(data.get("rod"))) {
                return false;
            }
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            BiPredicate<ItemStack, String> matcher = DATA_MATCHERS.get(entry.getKey());
            if (matcher != null && !matcher.test(stack, entry.getValue())) {
                return false;
            }
        }
        return aspects == null || aspects.equals(AspectKeys.ASPECTS.get(stack));
    }
}
