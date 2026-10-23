package thaumcraft.test;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModBlocks;
import thaumcraft.world.gen.BiomeHandler;
import thaumcraft.world.gen.LegacyGen;

final class Tc4OreReference {
    private Tc4OreReference() {
    }

    static void generate(WorldGenLevel level, Random random, int chunkX, int chunkZ) {
        {
            for (int i = 0; i < 18; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int y = random.nextInt(256 / 5);
                int z = chunkZ * 16 + random.nextInt(16);
                if (LegacyGen.get(level, x, y, z).is(BlockTags.STONE_ORE_REPLACEABLES)) {
                    LegacyGen.set(level, x, y, z, ModBlocks.CINNABAR_ORE.get().defaultBlockState());
                }
            }
        }
        {
            for (int i = 0; i < 20; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);
                int y = height(level, x, z) - random.nextInt(25);
                if (LegacyGen.get(level, x, y, z).is(BlockTags.STONE_ORE_REPLACEABLES)) {
                    LegacyGen.set(level, x, y, z, ModBlocks.AMBER_ORE.get().defaultBlockState());
                }
            }
        }
        {
            for (int i = 0; i < 8; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);
                int y = random.nextInt(Math.max(5, height(level, x, z) - 5));
                int type = random.nextInt(6) + 1;
                if (random.nextInt(3) == 0) {
                    Holder<Biome> biome = level.getBiome(new BlockPos(x, y, z));
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
                vein(level, random, x, y, z, infusedStone(type));
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

    private static void vein(WorldGenLevel var1, Random var2, int var3, int var4, int var5, BlockState state) {
        float var6 = var2.nextFloat() * (float) Math.PI;
        double var7 = var3 + 8 + Mth.sin(var6) * 6 / 8.0F;
        double var9 = var3 + 8 - Mth.sin(var6) * 6 / 8.0F;
        double var11 = var5 + 8 + Mth.cos(var6) * 6 / 8.0F;
        double var13 = var5 + 8 - Mth.cos(var6) * 6 / 8.0F;
        double var15 = var4 + var2.nextInt(3) - 2;
        double var17 = var4 + var2.nextInt(3) - 2;

        for (int var19 = 0; var19 <= 6; var19++) {
            double var20 = var7 + (var9 - var7) * var19 / 6;
            double var22 = var15 + (var17 - var15) * var19 / 6;
            double var24 = var11 + (var13 - var11) * var19 / 6;
            double var26 = var2.nextDouble() * 6 / 16.0;
            double var28 = (Mth.sin(var19 * (float) Math.PI / 6) + 1.0F) * var26 + 1.0;
            double var30 = (Mth.sin(var19 * (float) Math.PI / 6) + 1.0F) * var26 + 1.0;
            int var32 = Mth.floor(var20 - var28 / 2.0);
            int var33 = Mth.floor(var22 - var30 / 2.0);
            int var34 = Mth.floor(var24 - var28 / 2.0);
            int var35 = Mth.floor(var20 + var28 / 2.0);
            int var36 = Mth.floor(var22 + var30 / 2.0);
            int var37 = Mth.floor(var24 + var28 / 2.0);

            for (int var38 = var32; var38 <= var35; var38++) {
                double var39 = (var38 + 0.5 - var20) / (var28 / 2.0);
                if (var39 * var39 < 1.0) {
                    for (int var41 = var33; var41 <= var36; var41++) {
                        double var42 = (var41 + 0.5 - var22) / (var30 / 2.0);
                        if (var39 * var39 + var42 * var42 < 1.0) {
                            for (int var44 = var34; var44 <= var37; var44++) {
                                double var45 = (var44 + 0.5 - var24) / (var28 / 2.0);
                                if (var39 * var39 + var42 * var42 + var45 * var45 < 1.0 && LegacyGen.get(var1, var38, var41, var44).is(BlockTags.STONE_ORE_REPLACEABLES)) {
                                    LegacyGen.set(var1, var38, var41, var44, state);
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
