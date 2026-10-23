package thaumcraft.client.geom;

public final class ExtrudedGeometry {
    private ExtrudedGeometry() {
    }

    public static void render(QuadSink sink, float maxU, float minV, float minU, float maxV, int width, int height, float thickness, int argb, int light) {
        sink.vertex(0.0F, 0.0F, 0.0F, maxU, maxV, argb, light);
        sink.vertex(1.0F, 0.0F, 0.0F, minU, maxV, argb, light);
        sink.vertex(1.0F, 1.0F, 0.0F, minU, minV, argb, light);
        sink.vertex(0.0F, 1.0F, 0.0F, maxU, minV, argb, light);
        sink.vertex(0.0F, 1.0F, -thickness, maxU, minV, argb, light);
        sink.vertex(1.0F, 1.0F, -thickness, minU, minV, argb, light);
        sink.vertex(1.0F, 0.0F, -thickness, minU, maxV, argb, light);
        sink.vertex(0.0F, 0.0F, -thickness, maxU, maxV, argb, light);
        float halfU = 0.5F * (maxU - minU) / width;
        float halfV = 0.5F * (maxV - minV) / height;
        for (int column = 0; column < width; column++) {
            float fraction = (float) column / width;
            float u = maxU + (minU - maxU) * fraction - halfU;
            sink.vertex(fraction, 0.0F, -thickness, u, maxV, argb, light);
            sink.vertex(fraction, 0.0F, 0.0F, u, maxV, argb, light);
            sink.vertex(fraction, 1.0F, 0.0F, u, minV, argb, light);
            sink.vertex(fraction, 1.0F, -thickness, u, minV, argb, light);
        }
        for (int column = 0; column < width; column++) {
            float fraction = (float) column / width;
            float u = maxU + (minU - maxU) * fraction - halfU;
            float edge = fraction + 1.0F / width;
            sink.vertex(edge, 1.0F, -thickness, u, minV, argb, light);
            sink.vertex(edge, 1.0F, 0.0F, u, minV, argb, light);
            sink.vertex(edge, 0.0F, 0.0F, u, maxV, argb, light);
            sink.vertex(edge, 0.0F, -thickness, u, maxV, argb, light);
        }
        for (int row = 0; row < height; row++) {
            float fraction = (float) row / height;
            float v = maxV + (minV - maxV) * fraction - halfV;
            float edge = fraction + 1.0F / height;
            sink.vertex(0.0F, edge, 0.0F, maxU, v, argb, light);
            sink.vertex(1.0F, edge, 0.0F, minU, v, argb, light);
            sink.vertex(1.0F, edge, -thickness, minU, v, argb, light);
            sink.vertex(0.0F, edge, -thickness, maxU, v, argb, light);
        }
        for (int row = 0; row < height; row++) {
            float fraction = (float) row / height;
            float v = maxV + (minV - maxV) * fraction - halfV;
            sink.vertex(1.0F, fraction, 0.0F, minU, v, argb, light);
            sink.vertex(0.0F, fraction, 0.0F, maxU, v, argb, light);
            sink.vertex(0.0F, fraction, -thickness, maxU, v, argb, light);
            sink.vertex(1.0F, fraction, -thickness, minU, v, argb, light);
        }
    }
}
