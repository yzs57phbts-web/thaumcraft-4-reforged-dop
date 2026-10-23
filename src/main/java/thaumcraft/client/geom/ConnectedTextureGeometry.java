package thaumcraft.client.geom;

public final class ConnectedTextureGeometry {
    public static final int[] CONNECTED = {
        0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
        1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
        0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
        1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
        4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26,
        16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38,
        4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12,
        16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32,
        0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
        1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
        0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
        1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
        4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26,
        7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33,
        4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12,
        7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11
    };

    private static final int[][] NEIGHBORS = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

    @FunctionalInterface
    public interface Neighbors {
        boolean contains(int x, int y, int z);
    }

    private ConnectedTextureGeometry() {
    }

    public static void face(QuadSink sink, int side, int color, int light) {
        EldritchSurfaceGeometry.cubeFace((x, y, z, u, v, argb, packedLight) -> {
            float textureU = switch (side) {
                case 0, 1, 3 -> x;
                case 2 -> 1.0F - x;
                case 4 -> z;
                default -> 1.0F - z;
            };
            float textureV = side < 2 ? z : 1.0F - y;
            sink.vertex(x, y, z, textureU, textureV, argb, packedLight);
        }, side, 1.0F, 0.0F, color, light);
    }

    public static int texture(int side, Neighbors neighbors) {
        if (side < 0 || side > 5) {
            throw new IllegalArgumentException("Invalid cube face " + side);
        }
        int mask = 0;
        for (int index = 0; index < NEIGHBORS.length; index++) {
            int u = NEIGHBORS[index][0];
            int v = NEIGHBORS[index][1];
            boolean connected = switch (side) {
                case 0, 1 -> neighbors.contains(u, 0, v);
                case 2, 3 -> neighbors.contains(side == 2 ? -u : u, -v, 0);
                default -> neighbors.contains(0, -v, side == 5 ? -u : u);
            };
            if (connected) {
                mask |= 1 << index;
            }
        }
        return CONNECTED[mask] + 1;
    }
}
