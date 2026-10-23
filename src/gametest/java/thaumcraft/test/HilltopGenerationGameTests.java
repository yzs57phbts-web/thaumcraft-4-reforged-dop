package thaumcraft.test;

import java.lang.reflect.Proxy;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.world.WorldGenConfig;
import thaumcraft.world.gen.HilltopRuinPlacement;
import thaumcraft.world.gen.ThaumcraftWorldGenFeature;

@GameTestHolder("bbtt_ruin_tests")
@PrefixGameTestTemplate(false)
public final class HilltopGenerationGameTests {
    private static final long WORLD_SEED = 8616931143624757498L;

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void rejectedChunkCannotUseOldRandomRoll(GameTestHelper helper) {
        generate(helper, -224, -53, false);
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void selectedRuinRetainsChestAndWispSpawner(GameTestHelper helper) {
        generate(helper, -223, -41, true);
    }

    private static void generate(GameTestHelper helper, int chunkX, int chunkZ, boolean expected) {
        helper.assertTrue(HilltopRuinPlacement.isCandidate(WORLD_SEED, chunkX, chunkZ) == expected, "Invalid placement fixture");
        boolean[] previous = {WorldGenConfig.genTrees, WorldGenConfig.genAura, WorldGenConfig.genCinnibar,
            WorldGenConfig.genAmber, WorldGenConfig.genInfusedStone, WorldGenConfig.genStructure};
        try {
            WorldGenConfig.genTrees = WorldGenConfig.genAura = WorldGenConfig.genCinnibar = false;
            WorldGenConfig.genAmber = WorldGenConfig.genInfusedStone = WorldGenConfig.genStructure = false;
            long seed = oldHilltopRoll();
            Random positions = new Random(seed);
            int x = chunkX * 16 + positions.nextInt(16) - 8;
            int z = chunkZ * 16 + positions.nextInt(16) - 8;
            BlockPos center = new BlockPos(x, 100, z);
            var level = helper.getLevel();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-5, -1, -5), center.offset(5, 10, 5))) {
                level.setBlock(pos, pos.getY() == 99 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
            }
            WorldGenLevel world = (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(),
                new Class<?>[]{WorldGenLevel.class}, (proxy, method, args) -> method.getName().equals("getSeed")
                    ? WORLD_SEED : method.invoke(level, args));
            WorldGenConfig.genStructure = true;
            var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(level.getBiome(center)),
                level.registryAccess().lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD));
            new ThaumcraftWorldGenFeature().place(NoneFeatureConfiguration.INSTANCE, world,
                generator, new LegacyRandomSource(1) {
                    @Override public long nextLong() { return seed; }
                }, new BlockPos(chunkX * 16, 0, chunkZ * 16));
            if (expected) {
                helper.assertTrue(level.getBlockEntity(center.above(2)) instanceof ChestBlockEntity, "Selected ruin lost its chest");
                helper.assertTrue(level.getBlockEntity(center) instanceof SpawnerBlockEntity, "Selected ruin lost its spawner");
                CompoundTag chest = level.getBlockEntity(center.above(2)).saveWithoutMetadata(level.registryAccess());
                CompoundTag spawner = level.getBlockEntity(center).saveWithoutMetadata(level.registryAccess());
                helper.assertTrue(chest.getString("LootTable").equals("minecraft:chests/simple_dungeon"), "Ruin loot changed");
                helper.assertTrue(spawner.getCompound("SpawnData").getCompound("entity").getString("id").equals("thaumcraft:wisp"), "Ruin no longer spawns wisps");
            } else {
                helper.assertTrue(!level.getBlockState(center).is(Blocks.SPAWNER)
                    && !level.getBlockState(center.above(2)).is(Blocks.CHEST), "Old 1-in-40 roll bypassed spacing");
            }
            helper.succeed();
        } finally {
            WorldGenConfig.genTrees = previous[0];
            WorldGenConfig.genAura = previous[1];
            WorldGenConfig.genCinnibar = previous[2];
            WorldGenConfig.genAmber = previous[3];
            WorldGenConfig.genInfusedStone = previous[4];
            WorldGenConfig.genStructure = previous[5];
        }
    }

    private static long oldHilltopRoll() {
        for (long seed = 0; seed < 100000; seed++) {
            Random random = new Random(seed);
            random.nextInt(16);
            random.nextInt(16);
            if (random.nextInt(150) == 0 || random.nextInt(66) == 0) continue;
            Random current = new Random(seed);
            current.nextInt(16);
            current.nextInt(16);
            current.nextInt(150);
            current.nextInt(66);
            if (random.nextInt(40) == 0 && current.nextInt(WorldGenConfig.nodeRarity * 10) != 0) return seed;
        }
        throw new AssertionError("No legacy placement fixture found");
    }
}
