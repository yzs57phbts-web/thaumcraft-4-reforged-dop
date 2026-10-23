package thaumcraft.client.geom;

public final class ModelBoxGeometry {
    private static final int[][] QUADS = {
        {5, 1, 2, 6},
        {0, 4, 7, 3},
        {5, 4, 0, 1},
        {2, 3, 7, 6},
        {1, 0, 3, 2},
        {4, 5, 6, 7}
    };

    private ModelBoxGeometry() {
    }

    public static void box(
        QuadSink sink,
        float x,
        float y,
        float z,
        int width,
        int height,
        int depth,
        int textureU,
        int textureV,
        float textureWidth,
        float textureHeight,
        boolean mirror,
        int argb,
        int light
    ) {
        float x1 = x;
        float x2 = x + width;
        float y1 = y;
        float y2 = y + height;
        float z1 = z;
        float z2 = z + depth;
        if (mirror) {
            float swap = x2;
            x2 = x1;
            x1 = swap;
        }
        float[][] corners = {
            {x1, y1, z1},
            {x2, y1, z1},
            {x2, y2, z1},
            {x1, y2, z1},
            {x1, y1, z2},
            {x2, y1, z2},
            {x2, y2, z2},
            {x1, y2, z2}
        };
        float centerX = (x + width * 0.5F);
        float centerY = (y + height * 0.5F);
        float centerZ = (z + depth * 0.5F);
        int[][] regions = {
            {textureU + depth + width, textureV + depth, textureU + depth + width + depth, textureV + depth + height},
            {textureU, textureV + depth, textureU + depth, textureV + depth + height},
            {textureU + depth, textureV, textureU + depth + width, textureV + depth},
            {textureU + depth + width, textureV + depth, textureU + depth + width + width, textureV},
            {textureU + depth, textureV + depth, textureU + depth + width, textureV + depth + height},
            {textureU + depth + width + depth, textureV + depth, textureU + depth + width + depth + width, textureV + depth + height}
        };
        for (int quad = 0; quad < QUADS.length; quad++) {
            int[] indices = QUADS[quad];
            int[] region = regions[quad];
            float[][] uvs = {
                {region[2] / textureWidth, region[1] / textureHeight},
                {region[0] / textureWidth, region[1] / textureHeight},
                {region[0] / textureWidth, region[3] / textureHeight},
                {region[2] / textureWidth, region[3] / textureHeight}
            };
            float[] first = corners[indices[0]];
            float[] second = corners[indices[1]];
            float[] fourth = corners[indices[3]];
            float firstX = second[0] - first[0];
            float firstY = second[1] - first[1];
            float firstZ = second[2] - first[2];
            float secondX = fourth[0] - first[0];
            float secondY = fourth[1] - first[1];
            float secondZ = fourth[2] - first[2];
            float normalX = firstY * secondZ - firstZ * secondY;
            float normalY = firstZ * secondX - firstX * secondZ;
            float normalZ = firstX * secondY - firstY * secondX;
            float middleX = (first[0] + corners[indices[2]][0]) * 0.5F - centerX;
            float middleY = (first[1] + corners[indices[2]][1]) * 0.5F - centerY;
            float middleZ = (first[2] + corners[indices[2]][2]) * 0.5F - centerZ;
            boolean outward = normalX * middleX + normalY * middleY + normalZ * middleZ >= 0.0F;
            for (int step = 0; step < 4; step++) {
                int vertex = outward ? step : (4 - step) % 4;
                float[] corner = corners[indices[vertex]];
                sink.vertex(corner[0] / 16.0F, corner[1] / 16.0F, corner[2] / 16.0F, uvs[vertex][0], uvs[vertex][1], argb, light);
            }
        }
    }
}
