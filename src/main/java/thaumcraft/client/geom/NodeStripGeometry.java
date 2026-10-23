package thaumcraft.client.geom;

public final class NodeStripGeometry {
    private NodeStripGeometry() {
    }

    public static void strip(QuadSink sink, float scale, int frames, int strip, int frame, int argb, int light) {
        float u0 = (float) frame / frames;
        float u1 = (float) (frame + 1) / frames;
        float v0 = (float) strip / frames;
        float v1 = (float) (strip + 1) / frames;
        float half = 0.5F * scale;
        sink.vertex(-half, half, 0.0F, u0, v1, argb, light);
        sink.vertex(half, half, 0.0F, u1, v1, argb, light);
        sink.vertex(half, -half, 0.0F, u1, v0, argb, light);
        sink.vertex(-half, -half, 0.0F, u0, v0, argb, light);
    }
}
