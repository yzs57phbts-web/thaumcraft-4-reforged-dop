package thaumcraft.world.gen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.world.WorldGenConfig;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModBlocks;

public final class OreGeneration {
    private OreGeneration() {
    }

    public static void generate(WorldGenLevel level, Random random, int chunkX, int chunkZ) {
        int minY = level.getMinBuildHeight();
        if (WorldGenConfig.genCinnibar) {
            int depth = Math.max(1, level.getHeight() / 5);
            int attempts = (int) Math.round(18.0 * depth / 51);
            for (int i = 0; i < attempts; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int y = minY + random.nextInt(depth);
                int z = chunkZ * 16 + random.nextInt(16);
                replace(level, x, y, z, ModBlocks.CINNABAR_ORE.get().defaultBlockState());
            }
        }
        if (WorldGenConfig.genAmber) {
            for (int i = 0; i < 20; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);
                int y = height(level, x, z) - random.nextInt(25);
                replace(level, x, y, z, ModBlocks.AMBER_ORE.get().defaultBlockState());
            }
        }
        if (WorldGenConfig.genInfusedStone) {
            int surface = height(level, chunkX * 16 + 8, chunkZ * 16 + 8);
            int attempts = Math.max(1, (int) Math.round(8.0 * crystalDepth(level, surface) / Math.max(5, surface - 5)));
            for (int i = 0; i < attempts; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);
                int columnSurface = height(level, x, z);
                int y = minY + random.nextInt(crystalDepth(level, columnSurface));
                int type = random.nextInt(6) + 1;
                if (random.nextInt(3) == 0) {
                    Holder<Biome> biome = level.getBiome(new BlockPos(x, columnSurface, z));
                    Aspect tag = BiomeHandler.getRandomBiomeTag(biome, random);
                    if (tag == null) {
                        type = 1 + random.nextInt(6);
                    } else if (tag == Aspect.AIR) {
                        type = 1;
                    } else if (tag == Aspect.FIRE) {
                        type = 2;
                    } else if (tag == Aspect.WATER) {
                        type = 3;
                    } else if (tag == Aspect.EARTH) {
                        type = 4;
                    } else if (tag == Aspect.ORDER) {
                        type = 5;
                    } else if (tag == Aspect.ENTROPY) {
                        type = 6;
                    }
                }
                LegacyMinable.generate(level, random, x, y, z, 6, infusedStone(type));
            }
        }
    }

    private static BlockState infusedStone(int type) {
        return switch (type) {
            case 1 -> ModBlocks.AIR_INFUSED_STONE.get().defaultBlockState();
            case 2 -> ModBlocks.FIRE_INFUSED_STONE.get().defaultBlockState();
            case 3 -> ModBlocks.WATER_INFUSED_STONE.get().defaultBlockState();
            case 4 -> ModBlocks.EARTH_INFUSED_STONE.get().defaultBlockState();
            case 5 -> ModBlocks.ORDER_INFUSED_STONE.get().defaultBlockState();
            default -> ModBlocks.ENTROPY_INFUSED_STONE.get().defaultBlockState();
        };
    }

    private static int height(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    private static int crystalDepth(WorldGenLevel level, int surface) {
        return Math.min(level.getHeight(), Math.max(5, surface - 5 - level.getMinBuildHeight()));
    }

    public static BlockState withHostRock(BlockState ore, BlockState host) {
        return thaumcraft.registry.DeepOreVariants.select(ore, host.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES));
    }

    public static BlockState withHostRock(BlockState ore, BlockState host, int y) {
        if (!thaumcraft.registry.DeepOreVariants.supports(ore)) return withHostRock(ore, host);
        // Below zero use the deep family, including tuff pockets. In vanilla's
        // transition band follow the actual deepslate host.
        return thaumcraft.registry.DeepOreVariants.select(ore, y < 0 || host.is(Blocks.DEEPSLATE));
    }

    static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES)
            || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
            || state.is(Blocks.TUFF);
    }

    private static void replace(WorldGenLevel level, int x, int y, int z, BlockState state) {
        if (!level.isOutsideBuildHeight(y) && isReplaceable(LegacyGen.get(level, x, y, z))) {
            LegacyGen.set(level, x, y, z, withHostRock(state, LegacyGen.get(level, x, y, z), y));
        }
    }
}
