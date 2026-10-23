package thaumcraft.device;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class InfernalTiles {
    public static final int CORE = 0;
    public static final int BARS = 1;
    public static final int BOTTOM = 0;
    public static final int MIDDLE = 1;
    public static final int TOP = 2;
    public static final int COUNT;

    private static final int[] LEVEL = new int[32];
    private static final int[] CX = new int[32];
    private static final int[] CZ = new int[32];
    private static final int[][][] INDEX = new int[3][3][3];

    static {
        int count = 2;
        for (int level = BOTTOM; level <= TOP; level++) {
            for (int cz = 0; cz < 3; cz++) {
                for (int cx = 0; cx < 3; cx++) {
                    if (level == MIDDLE && cx == 1 && (cz == 1 || cz == 0) || level == TOP && cx == 1 && cz == 1) {
                        INDEX[level][cz][cx] = -1;
                        continue;
                    }
                    LEVEL[count] = level;
                    CX[count] = cx;
                    CZ[count] = cz;
                    INDEX[level][cz][cx] = count++;
                }
            }
        }
        COUNT = count;
    }

    private InfernalTiles() {
    }

    public static int level(int tile) {
        return LEVEL[tile];
    }

    public static int cx(int tile) {
        return CX[tile];
    }

    public static int cz(int tile) {
        return CZ[tile];
    }

    public static int wall(int level, int cx, int cz) {
        return INDEX[level][cz][cx];
    }

    public static String name(int tile) {
        if (tile == CORE) {
            return "core";
        }
        if (tile == BARS) {
            return "bars";
        }
        return new String[] {"bottom", "middle", "top"}[LEVEL[tile]] + "_" + CX[tile] + "_" + CZ[tile];
    }

    public static boolean isCorner(int tile) {
        return tile >= 2 && CX[tile] != 1 && CZ[tile] != 1;
    }

    public static boolean isNozzle(int tile) {
        if (tile < 2) {
            return false;
        }
        int level = LEVEL[tile];
        int cx = CX[tile];
        int cz = CZ[tile];
        return level == MIDDLE && (cx == 0 && cz == 1 || cx == 2 && cz == 1 || cx == 1 && cz == 2) || level == BOTTOM && cx == 1 && cz == 1;
    }

    public static Block revertBlock(int tile) {
        if (tile == CORE) {
            return Blocks.AIR;
        }
        if (tile == BARS) {
            return Blocks.IRON_BARS;
        }
        return isCorner(tile) ? Blocks.NETHER_BRICKS : Blocks.OBSIDIAN;
    }

    public static int turns(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    public static int[] toCanonical(int dx, int dz, Direction facing) {
        int x = dx;
        int z = dz;
        for (int step = 0; step < turns(facing); step++) {
            int nextX = z;
            int nextZ = -x;
            x = nextX;
            z = nextZ;
        }
        return new int[] {x, z};
    }
}
