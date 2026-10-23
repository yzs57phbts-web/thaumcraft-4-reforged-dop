package thaumcraft.world.dim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.ModBlocks;
import thaumcraft.taint.TaintBiomes;

public class GenPassage extends GenCommon {
    void generateDefaultPassage(ServerLevel world, RandomSource random, int cx, int cz, int y, Cell cell) {
        int x = cx * 16;
        int z = cz * 16;
        generateConnections(world, random, cx, cz, y, cell, 4, false);
        int mod = 0;
        if (cell.north && cell.south && cell.west && cell.east && random.nextBoolean()) {
            mod = 1;
        }

        for (int w = 1; w < 8; w++) {
            for (int h = 1; h < 8; h++) {
                if (w == 4 && h == 4 && mod == 1) {
                    placeBlock(world, x + 4 + w, y + 2, z + 4 + h, 7, cell);
                    placeBlock(world, x + 4 + w, y + 8, z + 4 + h, 7, cell);
                } else {
                    placeBlock(world, x + 4 + w, y + 2, z + 4 + h, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                    placeBlock(world, x + 4 + w, y + 8, z + 4 + h, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                }

                placeBlock(world, x + 4 + w, y, z + 4 + h, 1, cell);
                placeBlock(world, x + 4 + w, y + 10, z + 4 + h, 1, cell);
                placeBlock(world, x + 4 + w, y + 1, z + 4 + h, 8, cell);
                placeBlock(world, x + 4 + w, y + 9, z + 4 + h, 8, cell);
            }
        }

        if (cell.north) {
            for (int w = 2 + mod; w < 9 - mod; w++) {
                for (int h = 2 + mod; h < 9 - mod; h++) {
                    placeBlock(world, x + 3 + w, y + 10 - h, z + 5, PAT_CONNECT[h][w], Direction.NORTH, cell);
                }
            }

            if (mod == 0) {
                if (cell.west) {
                    placeBlock(world, x + 6, y + 3, z + 6, 3, Direction.EAST, cell);
                    placeBlock(world, x + 6, y + 7, z + 6, 5, Direction.EAST, cell);
                }

                if (cell.east) {
                    placeBlock(world, x + 10, y + 3, z + 6, 3, Direction.EAST, cell);
                    placeBlock(world, x + 10, y + 7, z + 6, 5, Direction.EAST, cell);
                }
            }
        } else {
            for (int w = 1; w < 8; w++) {
                for (int h = 1; h < 8; h++) {
                    placeBlock(world, x + 4 + w, y + 9 - h, z + 5, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                    placeBlock(world, x + 4 + w, y + 9 - h, z + 4, 8, cell);
                    placeBlock(world, x + 4 + w, y + 9 - h, z + 3, 1, cell);
                    if (h == 7) {
                        placeBlock(world, x + 4 + w, y + 1, z + 4, 1, cell);
                        placeBlock(world, x + 4 + w, y + 9, z + 4, 1, cell);
                    }

                    if (w == 7) {
                        placeBlock(world, x + 4, y + 9 - h, z + 4, 1, cell);
                        placeBlock(world, x + 12, y + 9 - h, z + 4, 1, cell);
                    }
                }
            }

            for (int w = 2; w < 7; w++) {
                placeBlock(world, x + 4 + w, y + 3, z + 6, 3, Direction.EAST, cell);
                placeBlock(world, x + 4 + w, y + 7, z + 6, 5, Direction.EAST, cell);
            }
        }

        if (cell.south) {
            for (int w = 2 + mod; w < 9 - mod; w++) {
                for (int h = 2 + mod; h < 9 - mod; h++) {
                    placeBlock(world, x + 3 + w, y + 10 - h, z + 11, PAT_CONNECT[h][w], Direction.SOUTH, cell);
                }
            }

            if (mod == 0) {
                if (cell.west) {
                    placeBlock(world, x + 6, y + 3, z + 10, 4, Direction.EAST, cell);
                    placeBlock(world, x + 6, y + 7, z + 10, 6, Direction.EAST, cell);
                }

                if (cell.east) {
                    placeBlock(world, x + 10, y + 3, z + 10, 4, Direction.EAST, cell);
                    placeBlock(world, x + 10, y + 7, z + 10, 6, Direction.EAST, cell);
                }
            }
        } else {
            for (int w = 1; w < 8; w++) {
                for (int h = 1; h < 8; h++) {
                    placeBlock(world, x + 4 + w, y + 9 - h, z + 11, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                    placeBlock(world, x + 4 + w, y + 9 - h, z + 12, 8, cell);
                    placeBlock(world, x + 4 + w, y + 9 - h, z + 13, 1, cell);
                    if (h == 7) {
                        placeBlock(world, x + 4 + w, y + 1, z + 12, 1, cell);
                        placeBlock(world, x + 4 + w, y + 9, z + 12, 1, cell);
                    }

                    if (w == 7) {
                        placeBlock(world, x + 4, y + 9 - h, z + 12, 1, cell);
                        placeBlock(world, x + 12, y + 9 - h, z + 12, 1, cell);
                    }
                }
            }

            for (int w = 2; w < 7; w++) {
                placeBlock(world, x + 4 + w, y + 3, z + 10, 4, Direction.EAST, cell);
                placeBlock(world, x + 4 + w, y + 7, z + 10, 6, Direction.EAST, cell);
            }
        }

        if (cell.east) {
            for (int w = 2 + mod; w < 9 - mod; w++) {
                for (int h = 2 + mod; h < 9 - mod; h++) {
                    placeBlock(world, x + 11, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.EAST, cell);
                }
            }

            if (mod == 0) {
                if (cell.north) {
                    placeBlock(world, x + 10, y + 3, z + 6, 4, Direction.NORTH, cell);
                    placeBlock(world, x + 10, y + 7, z + 6, 6, Direction.NORTH, cell);
                }

                if (cell.south) {
                    placeBlock(world, x + 10, y + 3, z + 10, 4, Direction.NORTH, cell);
                    placeBlock(world, x + 10, y + 7, z + 10, 6, Direction.NORTH, cell);
                }
            }
        } else {
            for (int w = 1; w < 8; w++) {
                for (int h = 1; h < 8; h++) {
                    placeBlock(world, x + 11, y + 9 - h, z + 4 + w, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                    placeBlock(world, x + 12, y + 9 - h, z + 4 + w, 8, cell);
                    placeBlock(world, x + 13, y + 9 - h, z + 4 + w, 1, cell);
                    if (h == 7) {
                        placeBlock(world, x + 12, y + 1, z + 4 + w, 1, cell);
                        placeBlock(world, x + 12, y + 9, z + 4 + w, 1, cell);
                    }

                    if (w == 7) {
                        placeBlock(world, x + 12, y + 9 - h, z + 4, 1, cell);
                        placeBlock(world, x + 12, y + 9 - h, z + 12, 1, cell);
                    }
                }
            }

            for (int w = 2; w < 7; w++) {
                placeBlock(world, x + 10, y + 3, z + 4 + w, 4, Direction.NORTH, cell);
                placeBlock(world, x + 10, y + 7, z + 4 + w, 6, Direction.NORTH, cell);
            }
        }

        if (cell.west) {
            for (int w = 2 + mod; w < 9 - mod; w++) {
                for (int h = 2 + mod; h < 9 - mod; h++) {
                    placeBlock(world, x + 5, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.WEST, cell);
                }
            }

            if (mod == 0) {
                if (cell.north) {
                    placeBlock(world, x + 6, y + 3, z + 6, 3, Direction.NORTH, cell);
                    placeBlock(world, x + 6, y + 7, z + 6, 5, Direction.NORTH, cell);
                }

                if (cell.south) {
                    placeBlock(world, x + 6, y + 3, z + 10, 3, Direction.NORTH, cell);
                    placeBlock(world, x + 6, y + 7, z + 10, 5, Direction.NORTH, cell);
                }
            }
        } else {
            for (int w = 1; w < 8; w++) {
                for (int h = 1; h < 8; h++) {
                    placeBlock(world, x + 5, y + 9 - h, z + 4 + w, cell.feature == 11 && random.nextInt(3) == 0 ? 20 : 2, cell);
                    placeBlock(world, x + 4, y + 9 - h, z + 4 + w, 8, cell);
                    placeBlock(world, x + 3, y + 9 - h, z + 4 + w, 1, cell);
                    if (h == 7) {
                        placeBlock(world, x + 4, y + 1, z + 4 + w, 1, cell);
                        placeBlock(world, x + 4, y + 9, z + 4 + w, 1, cell);
                    }

                    if (w == 7) {
                        placeBlock(world, x + 4, y + 9 - h, z + 4, 1, cell);
                        placeBlock(world, x + 4, y + 9 - h, z + 12, 1, cell);
                    }
                }
            }

            for (int w = 2; w < 7; w++) {
                placeBlock(world, x + 6, y + 3, z + 4 + w, 3, Direction.NORTH, cell);
                placeBlock(world, x + 6, y + 7, z + 4 + w, 5, Direction.NORTH, cell);
            }
        }

        if (mod == 1) {
            placeBlock(world, x + 5, y + 3, z + 5, 3, Direction.EAST, cell);
            placeBlock(world, x + 5, y + 7, z + 5, 5, Direction.EAST, cell);
            placeBlock(world, x + 5, y + 3, z + 6, 3, Direction.NORTH, cell);
            placeBlock(world, x + 5, y + 7, z + 6, 5, Direction.NORTH, cell);
            placeBlock(world, x + 11, y + 3, z + 5, 3, Direction.EAST, cell);
            placeBlock(world, x + 11, y + 7, z + 5, 5, Direction.EAST, cell);
            placeBlock(world, x + 11, y + 3, z + 6, 4, Direction.NORTH, cell);
            placeBlock(world, x + 11, y + 7, z + 6, 6, Direction.NORTH, cell);
            placeBlock(world, x + 5, y + 3, z + 11, 3, Direction.NORTH, cell);
            placeBlock(world, x + 5, y + 7, z + 11, 5, Direction.NORTH, cell);
            placeBlock(world, x + 6, y + 3, z + 11, 4, Direction.EAST, cell);
            placeBlock(world, x + 6, y + 7, z + 11, 6, Direction.EAST, cell);
            placeBlock(world, x + 11, y + 3, z + 11, 4, Direction.NORTH, cell);
            placeBlock(world, x + 11, y + 7, z + 11, 6, Direction.NORTH, cell);
            placeBlock(world, x + 10, y + 3, z + 11, 4, Direction.EAST, cell);
            placeBlock(world, x + 10, y + 7, z + 11, 6, Direction.EAST, cell);
        }

        if (cell.feature == 12) {
            for (int w = -4; w <= 4; w++) {
                for (int h = -4; h <= 4; h++) {
                    for (int j = -4; j <= 4; j++) {
                        BlockPos pos = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
                        BlockState state = world.getBlockState(pos);
                        if ((state.isAir() || state.is(ModBlocks.ANCIENT_STONE_PEDESTAL.get()) || state.is(ModBlocks.ANCIENT_STONE_STAIRS.get())) && random.nextBoolean()) {
                            placeBlock(world, pos.getX(), pos.getY(), pos.getZ(), 21, cell);
                        }
                    }
                }
            }
        }

        if (cell.feature == 13) {
            for (int w = -4; w <= 4; w++) {
                for (int h = -3; h <= 3; h++) {
                    for (int j = -4; j <= 4; j++) {
                        BlockPos pos = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
                        if (world.isEmptyBlock(pos) && adjacentToSolid(world, pos)) {
                            if (random.nextInt(3) != 0) world.setBlock(pos, ModBlocks.FIBROUS_TAINT.get().defaultBlockState(), 3);
                            TaintBiomes.set(world, pos, TaintBiomes.TAINTED_LAND);
                        }
                    }
                }
            }
        }

        if (cell.feature == 14) {
            BlockPos center = new BlockPos(x + 8, y + 4, z + 8);
            for (int w = -3; w <= 3; w++) {
                for (int h = -3; h <= 3; h++) {
                    for (int j = -3; j <= 3; j++) {
                        BlockPos pos = center.offset(w, h, j);
                        if (world.isEmptyBlock(pos) && random.nextFloat() < 0.35F) {
                            world.setBlock(pos, net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState(), 3);
                        }
                    }
                }
            }
            world.setBlock(center, net.minecraft.world.level.block.Blocks.SPAWNER.defaultBlockState(), 3);
            if (world.getBlockEntity(center) instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity spawner) spawner.setEntityId(thaumcraft.registry.ModEntities.MIND_SPIDER.get(), random);
        }
    }

    private boolean adjacentToSolid(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).canOcclude()) return true;
        }
        return false;
    }
}
