package thaumcraft.client.color;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlocks;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientColorRegistration {
    private static final int WHITE = 0xFFFFFFFF;

    private ClientColorRegistration() {
    }

    private static ItemColor constant(int color) {
        int argb = FastColor.ARGB32.opaque(color);
        return (stack, tintIndex) -> tintIndex == 0 ? argb : WHITE;
    }

    private static ItemColor layer(int layer, ItemColor source) {
        return (stack, tintIndex) -> tintIndex == layer ? source.getColor(stack, tintIndex) : WHITE;
    }

    private static Map<String, ItemColor> tints() {
        Map<String, ItemColor> tints = new LinkedHashMap<>();
        tints.put("air_infused_stone", constant(0xFFFF7E));
        tints.put("fire_infused_stone", constant(0xFF3C01));
        tints.put("water_infused_stone", constant(0x0090FF));
        tints.put("earth_infused_stone", constant(0x00A000));
        tints.put("order_infused_stone", constant(0xEECCFF));
        tints.put("entropy_infused_stone", constant(0x555577));
        for (String element : List.of("air","fire","water","earth","order","entropy")) {
            tints.put("deepslate_" + element + "_infused_stone", tints.get(element + "_infused_stone"));
        }
        tints.put("air_shard", constant(0xFFFF7E));
        tints.put("fire_shard", constant(0xFF3C01));
        tints.put("water_shard", constant(0x0090FF));
        tints.put("earth_shard", constant(0x00A000));
        tints.put("order_shard", constant(0xEECCFF));
        tints.put("entropy_shard", constant(0x555577));
        tints.put("greatwood_leaves", constant(0x48B518));
        tints.put("silverwood_leaves", constant(0x8899AA));
        tints.put("tainted_soil", constant(0x6D4189));
        tints.put("fibrous_taint", constant(0x6D4189));
        for (int index = 0; index < ModBlocks.CANDLE_COLORS.length; index++) {
            tints.put(ModBlocks.CANDLE_COLORS[index] + "_tallow_candle", constant(net.minecraft.world.item.DyeColor.byName(ModBlocks.CANDLE_COLORS[index], net.minecraft.world.item.DyeColor.WHITE).getTextureDiffuseColor()));
        }
        tints.put("essentia_phial", layer(1, AspectTint.INSTANCE));
        tints.put("wisp_essence", layer(0, AspectTint.INSTANCE));
        tints.put("crystallized_essence", layer(0, AspectTint.INSTANCE));
        tints.put("mana_bean", layer(0, AspectTint.INSTANCE));
        tints.put("research_notes", layer(1, ResearchNoteTint.INSTANCE));
        tints.put("discovery", layer(1, ResearchNoteTint.INSTANCE));
        for (String path : List.of("robe_chestplate", "robe_leggings", "robe_boots", "void_robe_helmet", "void_robe_chestplate", "void_robe_leggings")) {
            tints.put(path, (stack, tintIndex) -> tintIndex == 0 ? net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack, 6961280) | 0xFF000000 : WHITE);
        }
        tints.put("apprentice_ring_air", constant(0xFFFF7E));
        tints.put("apprentice_ring_earth", constant(0x56C000));
        tints.put("apprentice_ring_fire", constant(0xFF5A01));
        tints.put("apprentice_ring_water", constant(0x3CD4FC));
        tints.put("apprentice_ring_order", constant(0xD5D4EC));
        tints.put("apprentice_ring_entropy", constant(0x404040));
        tints.put("arcane_levitator", (stack, layer) -> layer == 0 ? 0xFF00A000 : 0xFFEECCFF);
        return tints;
    }

    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (Map.Entry<String, ItemColor> entry : tints().entrySet()) {
            Item item = BuiltInRegistries.ITEM.get(Thaumcraft.id(entry.getKey()));
            if (item != Items.AIR) {
                event.register(entry.getValue(), item);
            }
        }
    }
}
