package thaumcraft.test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.world.WorldGenConfig;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlocks;
import thaumcraft.world.gen.OreGeneration;

@net.neoforged.neoforge.gametest.GameTestHolder(Thaumcraft.MODID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class OreGenerationGameTests {
    private OreGenerationGameTests() {
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 200, batch = "ore_generation")
    public static void deepHosts(GameTestHelper helper) {
        for (var host : List.of(Blocks.DEEPSLATE, Blocks.TUFF, Blocks.STONE)) {
            FlatWorld world = new FlatWorld(helper, -64, 384, 64, host.defaultBlockState());
            CountingRandom random = new CountingRandom(37);
            OreGeneration.generate(world.level, random, -3, 7);
            helper.assertValueEqual(random.veins, 17, "crystal attempts at surface 64");
            helper.assertValueEqual(random.cinnabar, 27, "cinnabar attempts in a 384-block world");
            helper.assertValueEqual(random.amber, 20, "amber attempts");
            helper.assertValueEqual(world.undergroundBiomeQueries, 0, "crystal aspect queried a cave biome instead of the surface");
            helper.assertTrue(world.count(2, true) > 0, "no deep crystals in " + host);
            helper.assertTrue(world.count(0, true) > 0, "no deep cinnabar in " + host);
            helper.assertTrue(world.count(1, false) > 0, "no surface amber in " + host);
            for (var entry : world.blocks.entrySet()) {
                int y = entry.getKey().getY();
                int family = family(entry.getValue());
                boolean deep = family == 1 ? host != Blocks.STONE : y < 0 || host == Blocks.DEEPSLATE;
                helper.assertTrue(entry.getValue().getValue(thaumcraft.block.HostRockOreBlock.DEEPSLATE) == deep,
                    "Generated ore did not keep its actual host: " + entry);
                helper.assertTrue(y >= -64 && y < 64, "ore outside terrain");
                if (family == 0) helper.assertTrue(y < 12, "cinnabar outside the bottom fifth");
                if (family == 1) helper.assertTrue(y >= 40, "amber outside its original surface band");
            }
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 200, batch = "ore_generation")
    public static void protectedBlocks(GameTestHelper helper) {
        for (var host : List.of(Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.BEDROCK, Blocks.DIAMOND_ORE, Blocks.DIRT, ModBlocks.AIR_INFUSED_STONE.get())) {
            FlatWorld world = new FlatWorld(helper, -64, 384, 64, host.defaultBlockState());
            OreGeneration.generate(world.level, new Random(41), 0, 0);
            helper.assertTrue(world.blocks.isEmpty(), "generation replaced protected host " + host);
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 200, batch = "ore_generation")
    public static void toggles(GameTestHelper helper) {
        boolean cinnabar = WorldGenConfig.genCinnibar;
        boolean amber = WorldGenConfig.genAmber;
        boolean infused = WorldGenConfig.genInfusedStone;
        try {
            for (int enabled = -1; enabled < 3; enabled++) {
                WorldGenConfig.genCinnibar = enabled == 0;
                WorldGenConfig.genAmber = enabled == 1;
                WorldGenConfig.genInfusedStone = enabled == 2;
                FlatWorld world = new FlatWorld(helper, -64, 384, 64, Blocks.DEEPSLATE.defaultBlockState());
                Random random = new Random(43);
                OreGeneration.generate(world.level, random, 0, 0);
                if (enabled < 0) {
                    helper.assertTrue(world.blocks.isEmpty(), "disabled generation placed ores");
                    helper.assertValueEqual(random.nextLong(), new Random(43).nextLong(), "disabled ores consumed randomness");
                } else {
                    helper.assertTrue(!world.blocks.isEmpty(), "enabled ore did not generate");
                    for (var state : world.blocks.values()) helper.assertValueEqual(family(state), enabled, "disabled ore generated");
                }
            }
        } finally {
            WorldGenConfig.genCinnibar = cinnabar;
            WorldGenConfig.genAmber = amber;
            WorldGenConfig.genInfusedStone = infused;
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 200, batch = "ore_generation")
    public static void legacyParity(GameTestHelper helper) {
        for (int seed = 0; seed < 4096; seed++) {
            FlatWorld original = new FlatWorld(helper, 0, 256, 64, null);
            FlatWorld actual = new FlatWorld(helper, 0, 256, 64, null);
            Random referenceRandom = new Random(seed);
            CountingRandom actualRandom = new CountingRandom(seed);
            Tc4OreReference.generate(original.level, referenceRandom, -3, 7);
            OreGeneration.generate(actual.level, actualRandom, -3, 7);
            helper.assertValueEqual(actualRandom.veins, 8, "legacy crystal attempts");
            helper.assertValueEqual(actualRandom.cinnabar, 18, "legacy cinnabar attempts");
            helper.assertValueEqual(actualRandom.amber, 20, "legacy amber attempts");
            helper.assertTrue(original.blocks.equals(actual.blocks), "TC4 placement/vein mismatch for seed " + seed);
            helper.assertValueEqual(actualRandom.nextLong(), referenceRandom.nextLong(), "TC4 random stream mismatch");
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 200, batch = "ore_generation")
    public static void distribution(GameTestHelper helper) {
        for (int surface : new int[]{64, 96, 192}) {
            long[][] totals = new long[3][6];
            for (int seed = 0; seed < 4096; seed++) {
                for (int mode = 0; mode < 3; mode++) {
                    FlatWorld world = new FlatWorld(helper, mode == 0 ? 0 : -64, mode == 0 ? 256 : 384, surface, null);
                    if (mode < 2) Tc4OreReference.generate(world.level, new Random(seed), 0, 0);
                    else OreGeneration.generate(world.level, new Random(seed), 0, 0);
                    for (var entry : world.blocks.entrySet()) {
                        int family = family(entry.getValue());
                        totals[mode][family]++;
                        if (entry.getKey().getY() < 0) totals[mode][family + 3]++;
                    }
                }
            }
            helper.assertTrue(java.util.Arrays.equals(totals[0], totals[1]), "flat-world output before the fix differs from TC4");
            helper.assertTrue(totals[2][5] > 0 && totals[2][3] > 0, "modern ores never reached negative Y");
            double originalDensity = (double) totals[0][2] / (surface - 5);
            double fixedDensity = (double) totals[2][2] / (surface - 5 + 64);
            helper.assertTrue(Math.abs(fixedDensity / originalDensity - 1) < 0.08, "crystal density drift exceeds rounding/edge tolerance");
            double originalCinnabarDensity = totals[0][0] / 51.0;
            double fixedCinnabarDensity = totals[2][0] / 76.0;
            helper.assertTrue(Math.abs(fixedCinnabarDensity / originalCinnabarDensity - 1) < 0.04, "cinnabar density drift");
            for (int mode = 0; mode < 3; mode++) {
                Thaumcraft.LOGGER.info(String.format(Locale.ROOT,
                    "ORE_AUDIT surface=%d mode=%s seeds=4096 cinnabar=%.4f amber=%.4f crystals=%.4f deep_cinnabar=%.4f deep_crystals=%.4f",
                    surface, new String[]{"TC4", "before", "after"}[mode], totals[mode][0] / 4096.0, totals[mode][1] / 4096.0,
                    totals[mode][2] / 4096.0, totals[mode][3] / 4096.0, totals[mode][5] / 4096.0));
            }
        }
        helper.succeed();
    }

    private static int family(BlockState state) {
        if (state.is(ModBlocks.CINNABAR_ORE.get()) || state.is(ModBlocks.DEEPSLATE_CINNABAR_ORE.get())) return 0;
        if (state.is(ModBlocks.AMBER_ORE.get())) return 1;
        return 2;
    }

    private static final class CountingRandom extends Random {
        int veins;
        int cinnabar;
        int amber;

        CountingRandom(long seed) {
            super(seed);
        }

        @Override
        public float nextFloat() {
            veins++;
            return super.nextFloat();
        }

        @Override
        public int nextInt(int bound) {
            if (bound == 51 || bound == 76) cinnabar++;
            if (bound == 25) amber++;
            return super.nextInt(bound);
        }
    }

    private static final class FlatWorld {
        final Map<BlockPos, BlockState> blocks = new HashMap<>();
        final WorldGenLevel level;
        int undergroundBiomeQueries;

        FlatWorld(GameTestHelper helper, int minY, int height, int surface, BlockState host) {
            Holder<Biome> surfaceBiome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
            Holder<Biome> caveBiome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.LUSH_CAVES);
            level = (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(), new Class<?>[]{WorldGenLevel.class}, (proxy, method, args) -> {
                return switch (method.getName()) {
                    case "getMinBuildHeight" -> minY;
                    case "getHeight" -> args == null || args.length == 0 ? height : surface;
                    case "getBiome" -> {
                        if (((BlockPos) args[0]).getY() < surface) undergroundBiomeQueries++;
                        yield host == null || ((BlockPos) args[0]).getY() >= surface ? surfaceBiome : caveBiome;
                    }
                    case "isOutsideBuildHeight" -> {
                        int y = args[0] instanceof BlockPos pos ? pos.getY() : (int) args[0];
                        yield y < minY || y >= minY + height;
                    }
                    case "getBlockState" -> {
                        BlockPos pos = (BlockPos) args[0];
                        if (pos.getY() < minY || pos.getY() >= surface) yield Blocks.AIR.defaultBlockState();
                        yield blocks.getOrDefault(pos, host != null ? host : (pos.getY() < 0 ? Blocks.DEEPSLATE : Blocks.STONE).defaultBlockState());
                    }
                    case "setBlock" -> {
                        BlockPos pos = ((BlockPos) args[0]).immutable();
                        if (pos.getY() < minY || pos.getY() >= minY + height) throw new AssertionError("out-of-world write " + pos);
                        blocks.put(pos, (BlockState) args[1]);
                        yield true;
                    }
                    default -> throw new UnsupportedOperationException(method.toString());
                };
            });
        }

        long count(int type, boolean deep) {
            return blocks.entrySet().stream().filter(entry -> family(entry.getValue()) == type && (entry.getKey().getY() < 0) == deep).count();
        }
    }
}
