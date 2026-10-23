package thaumcraft.test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.entity.monster.Pech;
import thaumcraft.entity.monster.taint.Taintacle;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.world.biome.TaintDecorationFeature;
import thaumcraft.world.gen.ThaumcraftWorldGenFeature;
import thaumcraft.world.tree.SilverwoodTreeGenerator;

@GameTestHolder("bbtt_tc_tests")
@PrefixGameTestTemplate(false)
public final class BiomePackFixGameTests {
    @GameTest(template = "test/platform")
    public static void nurseryDoesNotReleaseUnlimitedSwarms(GameTestHelper helper) {
        var level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        var pos = helper.absolutePos(new BlockPos(3, 3, 3));
        var nursery = ModEntities.TAINT_SPORE_SWARMER.get().create(level);
        nursery.setPos(pos.getX(), pos.getY(), pos.getZ());
        nursery.setNoAi(true);
        nursery.setNoGravity(true);
        var player = TestSupport.mockServerPlayer(helper);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(pos.getX() + 2, pos.getY(), pos.getZ());
        level.addNewPlayer(player);
        var added = new java.util.ArrayList<net.minecraft.world.entity.Entity>();
        try {
            for (int i = 0; i < 3; i++) {
                var swarm = ModEntities.TAINT_SWARM.get().create(level);
                swarm.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                level.addFreshEntity(swarm);
                added.add(swarm);
            }
            var area = nursery.getBoundingBox().inflate(24, 12, 24);
            int before = level.getEntitiesOfClass(thaumcraft.entity.monster.taint.TaintSwarm.class, area).size();
            for (int tick = 0; tick < 501; tick++) nursery.tick();
            helper.assertTrue(level.getEntitiesOfClass(thaumcraft.entity.monster.taint.TaintSwarm.class, area).size() == before,
                "Nursery exceeded the nearby swarm limit");
        } finally {
            added.forEach(net.minecraft.world.entity.Entity::discard);
            nursery.discard();
            level.removePlayerImmediately(player, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void taintedSurfaceHabitatAndDensityCaps(GameTestHelper helper) {
        var biome = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(TaintBiomes.TAINTED_LAND);
        var nearby = new java.util.ArrayList<net.minecraft.world.entity.Entity>();
        ServerLevelAccessor world = (ServerLevelAccessor) Proxy.newProxyInstance(ServerLevelAccessor.class.getClassLoader(),
            new Class<?>[]{ServerLevelAccessor.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getDifficulty" -> Difficulty.NORMAL;
                case "getNoiseBiome", "getBiome" -> biome;
                case "getBlockState" -> ((BlockPos) args[0]).getY() < 64 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState();
                case "getFluidState" -> net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState();
                case "getHeight" -> 64;
                case "getEntitiesOfClass" -> nearby.stream().filter(((Class<?>) args[0])::isInstance).toList();
                default -> throw new UnsupportedOperationException(method.toString());
            });
        var pos = new BlockPos(8, 64, 8);
        var random = RandomSource.create(1);
        helper.assertTrue(thaumcraft.entity.monster.taint.TaintSpawnRules.surface(world, pos), "Bare natural soil inside taint biome rejected");
        for (var mob : java.util.List.of(ModEntities.TAINTACLE.get().create(helper.getLevel()), ModEntities.TAINT_SWARM.get().create(helper.getLevel()),
            ModEntities.TAINT_SPORE_SWARMER.get().create(helper.getLevel()), ModEntities.THAUMIC_SLIME.get().create(helper.getLevel()))) {
            mob.setPos(8, 64, 8);
            helper.assertTrue(mob.checkSpawnRules(world, MobSpawnType.NATURAL), "Instance spawn rules reintroduced the generic monster light restriction: " + mob.getType());
        }
        helper.assertFalse(thaumcraft.entity.monster.taint.TaintSpawnRules.surface(world, pos.above()), "Floating spawn above the surface accepted");
        helper.assertTrue(thaumcraft.entity.monster.taint.TaintSpawnRules.nursery(ModEntities.TAINT_SPORE_SWARMER.get(), world, MobSpawnType.NATURAL, pos, random), "Empty nursery habitat rejected");
        nearby.add(ModEntities.TAINT_SPORE_SWARMER.get().create(helper.getLevel()));
        helper.assertFalse(thaumcraft.entity.monster.taint.TaintSpawnRules.nursery(ModEntities.TAINT_SPORE_SWARMER.get(), world, MobSpawnType.NATURAL, pos, random), "Nursery spacing cap ignored");
        nearby.clear();
        for (int i = 0; i < 2; i++) nearby.add(ModEntities.TAINT_SWARM.get().create(helper.getLevel()));
        helper.assertFalse(thaumcraft.entity.monster.taint.TaintSpawnRules.swarm(ModEntities.TAINT_SWARM.get(), world, MobSpawnType.NATURAL, pos, random), "Swarm density cap ignored");
        nearby.clear();
        for (int i = 0; i < 4; i++) nearby.add(ModEntities.THAUMIC_SLIME.get().create(helper.getLevel()));
        helper.assertFalse(thaumcraft.entity.monster.taint.TaintSpawnRules.slime(ModEntities.THAUMIC_SLIME.get(), world, MobSpawnType.NATURAL, pos, random), "Slime density cap ignored");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void exposedCrustProducesSwarmNursery(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(8, 3, 8));
        TaintBiomes.set(level, pos, TaintBiomes.TAINTED_LAND);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        var crust = ModBlocks.CRUSTED_TAINT.get().defaultBlockState();
        level.setBlockAndUpdate(pos, crust);
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(crust.isRandomlyTicking(), "Crust never receives the random ticks which produce nurseries");
        crust.randomTick(level, pos, new LegacyRandomSource(1) {
            @Override public int nextInt(int bound) {
                return bound == 200 ? 0 : bound == 3 ? 1 : bound == 5 ? 3 : super.nextInt(bound);
            }
        });
        helper.assertTrue(!level.getEntitiesOfClass(thaumcraft.entity.monster.taint.TaintSwarmer.class,
            new net.minecraft.world.phys.AABB(pos).inflate(3)).isEmpty(), "Exposed crust did not produce a swarm nursery");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void taintedSurfaceAboveCaveBiome(GameTestHelper helper) {
        int crust = 0, plants = 0;
        for (int seed = 0; seed < 16; seed++) {
            var world = new SurfaceWorld(helper, Blocks.GRASS_BLOCK.defaultBlockState(), TaintBiomes.TAINTED_LAND, false);
            new TaintDecorationFeature().place(NoneFeatureConfiguration.INSTANCE, world.level,
                helper.getLevel().getChunkSource().getGenerator(), RandomSource.create(seed), BlockPos.ZERO);
            crust += world.blocks.entrySet().stream().filter(e -> e.getKey().getY() >= 64 && e.getValue().is(ModBlocks.CRUSTED_TAINT.get())).count();
            plants += world.blocks.values().stream().filter(s -> s.is(ModBlocks.TAINTED_GRASS.get())
                || s.is(ModBlocks.TAINTED_PLANT.get()) || s.is(ModBlocks.SPORE_STALK.get())).count();
        }
        helper.assertTrue(crust > 0, "Crust growth remained buried beneath the surface");
        helper.assertTrue(plants > 30, "Cave biome at feature origin suppressed surface taint plants");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void cinderpearlsFollowDesertSurface(GameTestHelper helper) {
        var world = new SurfaceWorld(helper, Blocks.SAND.defaultBlockState(), Biomes.DESERT, true);
        helper.assertTrue(ThaumcraftWorldGenFeature.generateFlowers(world.level, new Random(12), 8, 64, 8,
            ModBlocks.CINDERPEARL.get().defaultBlockState()), "No cinderpearls placed on a sloping desert");
        helper.assertTrue(world.blocks.size() >= 6, "Flower attempts missed the terrain");
        for (var entry : world.blocks.entrySet()) {
            helper.assertTrue(entry.getKey().getY() == world.surface(entry.getKey().getX(), entry.getKey().getZ()), "Floating or buried flower");
            helper.assertTrue(entry.getValue().canSurvive(world.level, entry.getKey()), "Flower cannot survive on its chosen soil");
        }
        var grass = new SurfaceWorld(helper, Blocks.GRASS_BLOCK.defaultBlockState(), Biomes.PLAINS, false);
        helper.assertFalse(ThaumcraftWorldGenFeature.generateFlowers(grass.level, new Random(12), 8, 64, 8,
            ModBlocks.CINDERPEARL.get().defaultBlockState()), "Cinderpearl accepted grass instead of sand");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void silverwoodGeneratesShimmerleaf(GameTestHelper helper) {
        for (boolean natural : new boolean[]{true, false}) {
            var world = new SurfaceWorld(helper, Blocks.GRASS_BLOCK.defaultBlockState(), TaintBiomes.MAGICAL_FOREST, false);
            // Avoid aura knot placement: this fixture is for vegetation, not nodes.
            Random random = new Random(12) {
                @Override public int nextInt(int bound) { return bound >= 10 ? 1 : super.nextInt(bound); }
            };
            helper.assertTrue(new SilverwoodTreeGenerator(!natural, 8, 5).generate(world.level, random, new BlockPos(8, 64, 8)), "Silverwood failed to generate");
            long leaves = world.blocks.values().stream().filter(s -> s.is(ModBlocks.SHIMMERLEAF.get())).count();
            helper.assertTrue(natural ? leaves > 0 : leaves == 0, "Shimmerleaf missing around natural tree or incorrectly farmed from saplings");
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void taintacleSpawnsAboveInfectedGround(GameTestHelper helper) {
        var level = helper.getLevel();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        var column = helper.absolutePos(new BlockPos(3, 3, 3));
        var pos = new BlockPos(column.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ()) + 1, column.getZ());
        TaintBiomes.set(level, pos, TaintBiomes.TAINTED_LAND);
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        for (var ground : new net.minecraft.world.level.block.Block[]{ModBlocks.TAINTED_SOIL.get(), ModBlocks.CRUSTED_TAINT.get()}) {
            level.setBlockAndUpdate(pos.below(), ground.defaultBlockState());
            helper.assertTrue(Taintacle.canSpawnNaturally(ModEntities.TAINTACLE.get(), level, MobSpawnType.NATURAL, pos, level.random), "Air above infected ground rejected at Y=" + pos.getY() + " surface=" + level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()));
        }
        level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        helper.assertFalse(Taintacle.canSpawnNaturally(ModEntities.TAINTACLE.get(), level, MobSpawnType.NATURAL, pos, level.random), "Underwater tentacle accepted");
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        var existing = ModEntities.TAINTACLE.get().create(level);
        existing.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(existing);
        helper.assertFalse(Taintacle.canSpawnNaturally(ModEntities.TAINTACLE.get(), level, MobSpawnType.NATURAL, pos, level.random), "Tentacle spacing limit lost");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void taintPlantsSurviveNeighborUpdates(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 3, 4));
        for (var plant : new net.minecraft.world.level.block.Block[]{ModBlocks.TAINTED_GRASS.get(), ModBlocks.TAINTED_PLANT.get(), ModBlocks.SPORE_STALK.get()}) {
            level.setBlockAndUpdate(pos.below(), ModBlocks.TAINTED_SOIL.get().defaultBlockState());
            level.setBlockAndUpdate(pos, plant.defaultBlockState());
            level.setBlockAndUpdate(pos.east(), ModBlocks.CRUSTED_TAINT.get().defaultBlockState());
            level.setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());
            helper.assertTrue(level.getBlockState(pos).is(plant), "Plant disappeared while rooted in infected soil");
            level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
            helper.assertTrue(level.isEmptyBlock(pos), "Unsupported plant remained floating");
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void pechLegacyLightAndBiomeRules(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        for (int brightness : new int[]{6, 8}) {
            ServerLevelAccessor level = (ServerLevelAccessor) Proxy.newProxyInstance(ServerLevelAccessor.class.getClassLoader(),
                new Class<?>[]{ServerLevelAccessor.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getBiome" -> registry.getOrThrow(TaintBiomes.MAGICAL_FOREST);
                    case "getLevel" -> helper.getLevel();
                    case "getDifficulty" -> Difficulty.NORMAL;
                    case "getBlockState" -> Blocks.GRASS_BLOCK.defaultBlockState();
                    case "getBrightness" -> 0;
                    case "getMaxLocalRawBrightness" -> brightness;
                    default -> throw new UnsupportedOperationException(method.toString());
                });
            RandomSource random = new LegacyRandomSource(1) {
                @Override public int nextInt(int bound) { return bound - 1; }
            };
            boolean allowed = Pech.checkPechSpawnRules(ModEntities.PECH.get(), level, MobSpawnType.NATURAL,
                helper.absolutePos(new BlockPos(4, 3, 4)), random);
            helper.assertTrue(allowed == (brightness == 6), "Pech no longer follows the original 0..7 light limit");
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 600)
    public static void swarmSpawnCycle(GameTestHelper helper) {
        TaintGameTests.swarmerSpawnsForVulnerablePlayersWithoutStarving(helper);
    }

    @GameTest(template = "test/platform")
    public static void crustInteriorBecomesFluxGoo(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(8, 4, 8));
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -2, -1), pos.offset(1, 1, 1))) {
            TaintBiomes.set(level, p, TaintBiomes.TAINTED_LAND);
            level.setBlock(p, p.getY() == pos.getY() - 2 ? Blocks.STONE.defaultBlockState()
                : ModBlocks.CRUSTED_TAINT.get().defaultBlockState(), 2);
        }
        level.getBlockState(pos).randomTick(level, pos, new LegacyRandomSource(1) {
            @Override public int nextInt(int bound) { return bound - 1; }
        });
        helper.assertTrue(level.getBlockState(pos).is(ModBlocks.FLUX_GOO.get()), "Enclosed crust did not form the expected liquid taint core");
        helper.succeed();
    }

    private static final class SurfaceWorld {
        final Map<BlockPos, BlockState> blocks = new HashMap<>();
        final WorldGenLevel level;
        final boolean slope;
        final BlockState ground;

        int surface(int x, int z) { return 64 + (slope ? Math.floorMod(x + z, 5) - 2 : 0); }
        BlockState state(BlockPos p) { return blocks.getOrDefault(p, p.getY() < surface(p.getX(), p.getZ()) ? ground : Blocks.AIR.defaultBlockState()); }

        SurfaceWorld(GameTestHelper helper, BlockState ground, net.minecraft.resources.ResourceKey<Biome> biome, boolean slope) {
            this.ground = ground;
            this.slope = slope;
            Holder<Biome> above = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome);
            Holder<Biome> cave = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.LUSH_CAVES);
            level = (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(), new Class<?>[]{WorldGenLevel.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getMinBuildHeight" -> -64;
                case "getMaxBuildHeight" -> 320;
                case "getHeight" -> {
                    if (args == null || args.length == 0) yield 384;
                    int x = (int) args[1], z = (int) args[2], height = surface(x, z);
                    for (var e : blocks.entrySet()) {
                        var p = e.getKey(); var s = e.getValue();
                        if (p.getX() == x && p.getZ() == z && s.blocksMotion()
                            && !(args[0] == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES && s.is(net.minecraft.tags.BlockTags.LEAVES))) height = Math.max(height, p.getY() + 1);
                    }
                    yield height;
                }
                case "getBiome" -> ((BlockPos) args[0]).getY() < 32 ? cave : above;
                case "getBlockState" -> state((BlockPos) args[0]);
                case "getFluidState" -> state((BlockPos) args[0]).getFluidState();
                case "isEmptyBlock" -> state((BlockPos) args[0]).isAir();
                case "isOutsideBuildHeight" -> ((BlockPos) args[0]).getY() < -64 || ((BlockPos) args[0]).getY() >= 320;
                case "setBlock" -> { blocks.put(((BlockPos) args[0]).immutable(), (BlockState) args[1]); yield true; }
                case "scheduleTick" -> null;
                case "getRawBrightness", "getMaxLocalRawBrightness" -> 15;
                case "canSeeSky", "ensureCanWrite" -> true;
                case "getLevel" -> helper.getLevel();
                default -> throw new UnsupportedOperationException(method.toString());
            });
        }
    }
}
