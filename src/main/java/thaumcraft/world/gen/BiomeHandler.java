package thaumcraft.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.aspect.Aspect;

public final class BiomeHandler {
    private record BiomeInfo(TagKey<Biome> tag, int aura, Aspect aspect, boolean greatwood, float greatwoodChance) {
    }

    private static final List<BiomeInfo> INFO = List.of(
        new BiomeInfo(Tags.Biomes.IS_FOREST, 120, Aspect.EARTH, true, 1.0F),
        new BiomeInfo(Tags.Biomes.IS_PLAINS, 80, Aspect.AIR, true, 0.2F),
        new BiomeInfo(Tags.Biomes.IS_MOUNTAIN, 100, Aspect.AIR, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_HILL, 120, Aspect.AIR, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_SWAMP, 120, Aspect.ENTROPY, true, 0.2F),
        new BiomeInfo(Tags.Biomes.IS_AQUATIC, 100, Aspect.WATER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_DESERT, 100, Aspect.FIRE, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_ICY, 100, Aspect.ORDER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_JUNGLE, 100, Aspect.PLANT, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_WASTELAND, 80, Aspect.ENTROPY, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_NETHER, 120, Aspect.FIRE, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_END, 80, Aspect.VOID, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_MUSHROOM, 140, Aspect.ORDER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_MAGICAL, 100, null, true, 1.0F),
        new BiomeInfo(Tags.Biomes.IS_BADLANDS, 80, Aspect.FIRE, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_OCEAN, 120, Aspect.WATER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_RIVER, 100, Aspect.WATER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_WET, 80, Aspect.WATER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_HOT, 100, Aspect.FIRE, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_DENSE_VEGETATION, 100, Aspect.ORDER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_SNOWY, 80, Aspect.ORDER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_COLD, 80, Aspect.ORDER, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_CONIFEROUS_TREE, 100, Aspect.EARTH, true, 0.2F),
        new BiomeInfo(Tags.Biomes.IS_SANDY, 80, Aspect.EARTH, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_BEACH, 80, Aspect.EARTH, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_SAVANNA, 80, Aspect.AIR, true, 0.2F),
        new BiomeInfo(Tags.Biomes.IS_DRY, 80, Aspect.ENTROPY, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_SPARSE_VEGETATION, 80, Aspect.ENTROPY, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_LUSH, 100, Aspect.PLANT, true, 0.5F),
        new BiomeInfo(Tags.Biomes.IS_SPOOKY, 80, Aspect.SOUL, false, 0.0F),
        new BiomeInfo(Tags.Biomes.IS_DEAD, 50, Aspect.DEATH, false, 0.0F)
    );

    private BiomeHandler() {
    }

    private static List<BiomeInfo> typesFor(Holder<Biome> biome) {
        List<BiomeInfo> types = new ArrayList<>();
        for (BiomeInfo info : INFO) {
            if (biome.is(info.tag())) {
                types.add(info);
            }
        }
        return types;
    }

    public static int getBiomeAura(Holder<Biome> biome) {
        List<BiomeInfo> types = typesFor(biome);
        if (types.isEmpty()) {
            return 100;
        }
        int total = 0;
        for (BiomeInfo info : types) {
            total += info.aura();
        }
        return total / types.size();
    }

    public static Aspect getRandomBiomeTag(Holder<Biome> biome, Random random) {
        List<BiomeInfo> types = typesFor(biome);
        if (types.isEmpty()) {
            return null;
        }
        return types.get(random.nextInt(types.size())).aspect();
    }

    public static float getBiomeSupportsGreatwood(Holder<Biome> biome) {
        for (BiomeInfo info : typesFor(biome)) {
            if (info.greatwood()) {
                return info.greatwoodChance();
            }
        }
        return 0.0F;
    }
}
