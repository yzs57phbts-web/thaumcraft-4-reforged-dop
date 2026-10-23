package thaumcraft.client.geom;

import org.joml.Vector3f;

public final class FishingLineGeometry {
    @FunctionalInterface
    public interface Sink {
        void vertex(float x, float y, float z, float nx, float ny, float nz);
    }

    private FishingLineGeometry() { }

    public static void render(float dx, float dy, float dz, Sink sink) {
        for (int segment = 0; segment < 16; segment++) {
            float from = segment / 16.0F;
            float to = (segment + 1) / 16.0F;
            float x0 = dx * from;
            float y0 = y(dy, from);
            float z0 = dz * from;
            float x1 = dx * to;
            float y1 = y(dy, to);
            float z1 = dz * to;
            Vector3f normal = new Vector3f(x1 - x0, y1 - y0, z1 - z0);
            if (normal.lengthSquared() == 0) continue;
            normal.normalize();
            sink.vertex(x0, y0, z0, normal.x, normal.y, normal.z);
            sink.vertex(x1, y1, z1, -normal.x, -normal.y, -normal.z);
        }
    }

    private static float y(float delta, float fraction) { return delta * (fraction * fraction + fraction) * 0.5F + 0.25F; }
}
