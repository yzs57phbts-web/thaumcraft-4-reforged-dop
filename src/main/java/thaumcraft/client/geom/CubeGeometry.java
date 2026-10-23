package thaumcraft.client.geom;

public final class CubeGeometry {
    private static final int[][] QUAD_VERTICES = {{5, 1, 2, 6}, {0, 4, 7, 3}, {5, 4, 0, 1}, {2, 3, 7, 6}, {1, 0, 3, 2}, {4, 5, 6, 7}};
    private static final int[][] QUAD_UV = {{32, 16, 48, 32}, {0, 16, 16, 32}, {16, 0, 32, 16}, {32, 0, 48, 16}, {16, 16, 32, 32}, {48, 16, 64, 32}};
    private static final float TEXTURE_SIZE = 64.0F;
    private static final float SCALE = 0.0625F;

    private CubeGeometry() {
    }

    public static void render(QuadSink sink, int shift, int argb, int light) {
        float[][] vertices = {
            {8.0F, -8.0F, -8.0F},
            {-8.0F, -8.0F, -8.0F},
            {-8.0F, 8.0F, -8.0F},
            {8.0F, 8.0F, -8.0F},
            {8.0F, -8.0F, 8.0F},
            {-8.0F, -8.0F, 8.0F},
            {-8.0F, 8.0F, 8.0F},
            {8.0F, 8.0F, 8.0F}
        };
        for (int quad = 0; quad < QUAD_VERTICES.length; quad++) {
            int[] uv = QUAD_UV[quad];
            float[][] corners = {
                {uv[2], uv[1] + shift},
                {uv[0], uv[1] + shift},
                {uv[0], uv[3] + shift},
                {uv[2], uv[3] + shift}
            };
            for (int order = 3; order >= 0; order--) {
                float[] position = vertices[QUAD_VERTICES[quad][order]];
                sink.vertex(position[0] * SCALE, position[1] * SCALE, position[2] * SCALE, corners[order][0] / TEXTURE_SIZE, corners[order][1] / TEXTURE_SIZE, argb, light);
            }
        }
    }
}
