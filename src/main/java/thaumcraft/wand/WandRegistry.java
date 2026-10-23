package thaumcraft.wand;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;

public final class WandRegistry {
    private static final Map<String, WandCap> CAPS = new LinkedHashMap<>();
    private static final Map<String, WandRod> RODS = new LinkedHashMap<>();

    public static final WandCap CAP_IRON = cap("iron", 1.1F, List.of(), 0.0F, "iron_wand_cap", 1, null);
    public static final WandCap CAP_GOLD = cap("gold", 1.0F, List.of(), 0.0F, "gold_wand_cap", 3, null);
    public static final WandCap CAP_THAUMIUM = cap("thaumium", 0.9F, List.of(), 0.0F, "thaumium_wand_cap", 6, null);
    public static final WandCap CAP_VOID = cap("void", 0.8F, List.of(), 0.0F, "void_wand_cap", 9, null);
    public static final WandCap CAP_COPPER = cap("copper", 1.1F, List.of(Aspect.ORDER, Aspect.ENTROPY), 1.0F, "copper_wand_cap", 2, "copper");
    public static final WandCap CAP_SILVER = cap(
        "silver", 1.0F, List.of(Aspect.AIR, Aspect.EARTH, Aspect.FIRE, Aspect.WATER), 0.95F, "silver_wand_cap", 4, "silver"
    );

    public static final WandRod ROD_WOOD = rod("wood", 25, ResourceLocation.withDefaultNamespace("stick"), 1, null, false, false, false);
    public static final WandRod ROD_GREATWOOD = rod("greatwood", 50, Thaumcraft.id("greatwood_rod"), 3, null, false, false, false);
    public static final WandRod ROD_OBSIDIAN = rod("obsidian", 75, Thaumcraft.id("obsidian_rod"), 6, RodUpdates.single(Aspect.EARTH), false, false, false);
    public static final WandRod ROD_BLAZE = rod("blaze", 75, Thaumcraft.id("blazing_rod"), 6, RodUpdates.single(Aspect.FIRE), true, false, false);
    public static final WandRod ROD_ICE = rod("ice", 75, Thaumcraft.id("icy_rod"), 6, RodUpdates.single(Aspect.WATER), false, false, false);
    public static final WandRod ROD_QUARTZ = rod("quartz", 75, Thaumcraft.id("quartz_rod"), 6, RodUpdates.single(Aspect.ORDER), false, false, false);
    public static final WandRod ROD_BONE = rod("bone", 75, Thaumcraft.id("bone_rod"), 6, RodUpdates.single(Aspect.ENTROPY), false, false, false);
    public static final WandRod ROD_REED = rod("reed", 75, Thaumcraft.id("reed_rod"), 6, RodUpdates.single(Aspect.AIR), false, false, false);
    public static final WandRod ROD_SILVERWOOD = rod("silverwood", 100, Thaumcraft.id("silverwood_rod"), 9, null, false, false, false);

    public static final WandRod STAFF_GREATWOOD = rod("greatwood", 125, Thaumcraft.id("greatwood_staff_core"), 8, null, false, true, false);
    public static final WandRod STAFF_OBSIDIAN = rod("obsidian", 175, Thaumcraft.id("obsidian_staff_core"), 14, RodUpdates.single(Aspect.EARTH), false, true, false);
    public static final WandRod STAFF_BLAZE = rod("blaze", 175, Thaumcraft.id("blazing_staff_core"), 14, RodUpdates.single(Aspect.FIRE), true, true, false);
    public static final WandRod STAFF_ICE = rod("ice", 175, Thaumcraft.id("icy_staff_core"), 14, RodUpdates.single(Aspect.WATER), false, true, false);
    public static final WandRod STAFF_QUARTZ = rod("quartz", 175, Thaumcraft.id("quartz_staff_core"), 14, RodUpdates.single(Aspect.ORDER), false, true, false);
    public static final WandRod STAFF_BONE = rod("bone", 175, Thaumcraft.id("bone_staff_core"), 14, RodUpdates.single(Aspect.ENTROPY), false, true, false);
    public static final WandRod STAFF_REED = rod("reed", 175, Thaumcraft.id("reed_staff_core"), 14, RodUpdates.single(Aspect.AIR), false, true, false);
    public static final WandRod STAFF_SILVERWOOD = rod("silverwood", 250, Thaumcraft.id("silverwood_staff_core"), 24, null, false, true, false);
    public static final WandRod STAFF_PRIMAL = rod("primal", 250, Thaumcraft.id("primal_staff_core"), 32, RodUpdates.primal(), false, true, true);

    private WandRegistry() {
    }

    private static WandCap cap(
        String tag,
        float discount,
        List<Aspect> special,
        float specialDiscount,
        String item,
        int craftCost,
        @Nullable String metal
    ) {
        return registerCap(
            tag,
            discount,
            special,
            specialDiscount,
            Thaumcraft.id(item),
            craftCost,
            metal,
            Thaumcraft.id("textures/model/wand_cap_" + tag + ".png")
        );
    }

    public static synchronized WandCap registerCap(
        String tag,
        float discount,
        List<Aspect> special,
        float specialDiscount,
        ResourceLocation item,
        int craftCost,
        @Nullable String metal,
        ResourceLocation texture
    ) {
        if (CAPS.containsKey(tag)) {
            throw new IllegalStateException("Wand cap already registered: " + tag);
        }
        WandCap cap = new WandCap(tag, discount, special, specialDiscount, item, craftCost, metal, texture);
        CAPS.put(tag, cap);
        return cap;
    }

    private static WandRod rod(
        String base,
        int capacity,
        ResourceLocation item,
        int craftCost,
        @Nullable RodUpdate update,
        boolean glowing,
        boolean staff,
        boolean runes
    ) {
        return registerRod(
            base,
            capacity,
            item,
            craftCost,
            update,
            glowing,
            staff,
            runes,
            Thaumcraft.id("textures/model/wand_rod_" + base + ".png")
        );
    }

    public static synchronized WandRod registerRod(
        String base,
        int capacity,
        ResourceLocation item,
        int craftCost,
        @Nullable RodUpdate update,
        boolean glowing,
        boolean staff,
        boolean runes,
        ResourceLocation texture
    ) {
        String tag = staff ? base + "_staff" : base;
        if (RODS.containsKey(tag)) {
            throw new IllegalStateException("Wand rod already registered: " + tag);
        }
        WandRod rod = new WandRod(tag, base, capacity, item, craftCost, update, glowing, staff, runes, texture);
        RODS.put(tag, rod);
        return rod;
    }

    public static synchronized WandCap cap(String tag) {
        return CAPS.getOrDefault(tag, CAP_IRON);
    }

    public static synchronized WandRod rod(String tag) {
        return RODS.getOrDefault(tag, ROD_WOOD);
    }

    public static synchronized @Nullable WandCap findCap(String tag) {
        return CAPS.get(tag);
    }

    public static synchronized @Nullable WandRod findRod(String tag) {
        return RODS.get(tag);
    }

    public static synchronized Collection<WandCap> caps() {
        return List.copyOf(CAPS.values());
    }

    public static synchronized Collection<WandRod> rods() {
        return List.copyOf(RODS.values());
    }
}
