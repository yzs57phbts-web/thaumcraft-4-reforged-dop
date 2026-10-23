package thaumcraft.client.color;

import java.util.List;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.Thaumcraft;
import thaumcraft.block.InfusedStoneBlock;
import thaumcraft.registry.ModBlocks;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BlockTints {
    private BlockTints() {
    }

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.Block event) {
        List<DeferredBlock<InfusedStoneBlock>> infusedStones = List.of(
            ModBlocks.AIR_INFUSED_STONE,
            ModBlocks.FIRE_INFUSED_STONE,
            ModBlocks.WATER_INFUSED_STONE,
            ModBlocks.EARTH_INFUSED_STONE,
            ModBlocks.ORDER_INFUSED_STONE,
            ModBlocks.ENTROPY_INFUSED_STONE,
            ModBlocks.DEEPSLATE_AIR_INFUSED_STONE, ModBlocks.DEEPSLATE_FIRE_INFUSED_STONE,
            ModBlocks.DEEPSLATE_WATER_INFUSED_STONE, ModBlocks.DEEPSLATE_EARTH_INFUSED_STONE,
            ModBlocks.DEEPSLATE_ORDER_INFUSED_STONE, ModBlocks.DEEPSLATE_ENTROPY_INFUSED_STONE
        );
        for (DeferredBlock<InfusedStoneBlock> stone : infusedStones) {
            event.register(constant(stone.get().getColor()), stone.get());
        }
        event.register(
            (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor(),
            ModBlocks.GREATWOOD_LEAVES.get()
        );
        event.register(constant(0x8899AA), ModBlocks.SILVERWOOD_LEAVES.get());
        // These legacy textures are grayscale masks, including outside the taint biome.
        event.register(constant(0x6D4189), ModBlocks.TAINTED_SOIL.get(), ModBlocks.FIBROUS_TAINT.get());
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && level.getBlockEntity(pos) instanceof thaumcraft.blockentity.WardedBlockEntity warded) {
                var disguise = warded.getDisguise();
                if (disguise != null && !(disguise.getBlock() instanceof thaumcraft.block.WardedBlock)) {
                    return net.minecraft.client.Minecraft.getInstance().getBlockColors().getColor(disguise, level, pos, tintIndex);
                }
            }
            return 0xFFFFFFFF;
        }, ModBlocks.WARDED_BLOCK.get());
        for (int index = 0; index < ModBlocks.CANDLE_COLORS.length; index++) {
            Block candle = ModBlocks.CANDLES.get(ModBlocks.CANDLE_COLORS[index]).get();
            event.register(constant(ModBlocks.CANDLE_TINTS[index]), candle);
        }
    }

    private static BlockColor constant(int color) {
        int argb = FastColor.ARGB32.opaque(color);
        return (state, level, pos, tintIndex) -> argb;
    }
}
