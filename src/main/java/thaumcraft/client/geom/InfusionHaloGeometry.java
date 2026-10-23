package thaumcraft.client.geom;

import java.util.Random;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class InfusionHaloGeometry {
    private static final int CENTER_COLOR = 0xFFFFFFFF;
    private static final int EDGE_COLOR = 0x00CC00FF;

    private InfusionHaloGeometry() {
    }

    public static void render(QuadSink sink, int count, boolean fancy, float u, float v, int light) {
        int rays = fancy ? 20 : 10;
        float progress = count / 500.0F;
        Random random = new Random(245L);
        Matrix4f matrix = new Matrix4f();
        float growth = Math.min(count, 50) / 50.0F / 20.0F;
        for (int ray = 0; ray < rays; ray++) {
            matrix.rotate((float) Math.toRadians(random.nextFloat() * 360.0F), 1.0F, 0.0F, 0.0F);
            matrix.rotate((float) Math.toRadians(random.nextFloat() * 360.0F), 0.0F, 1.0F, 0.0F);
            matrix.rotate((float) Math.toRadians(random.nextFloat() * 360.0F), 0.0F, 0.0F, 1.0F);
            matrix.rotate((float) Math.toRadians(random.nextFloat() * 360.0F), 1.0F, 0.0F, 0.0F);
            matrix.rotate((float) Math.toRadians(random.nextFloat() * 360.0F), 0.0F, 1.0F, 0.0F);
            matrix.rotate((float) Math.toRadians(random.nextFloat() * 360.0F + progress * 360.0F), 0.0F, 0.0F, 1.0F);
            float height = (random.nextFloat() * 20.0F + 5.0F) * growth;
            float width = (random.nextFloat() * 2.0F + 1.0F) * growth;
            Vector3f center = matrix.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
            Vector3f first = matrix.transformPosition(new Vector3f(-0.866F * width, height, -0.5F * width));
            Vector3f second = matrix.transformPosition(new Vector3f(0.866F * width, height, -0.5F * width));
            Vector3f third = matrix.transformPosition(new Vector3f(0.0F, height, width));
            vertex(sink, center, u, v, CENTER_COLOR, light);
            vertex(sink, first, u, v, EDGE_COLOR, light);
            vertex(sink, second, u, v, EDGE_COLOR, light);
            vertex(sink, third, u, v, EDGE_COLOR, light);
            vertex(sink, center, u, v, CENTER_COLOR, light);
            vertex(sink, third, u, v, EDGE_COLOR, light);
            vertex(sink, first, u, v, EDGE_COLOR, light);
            vertex(sink, first, u, v, EDGE_COLOR, light);
        }
    }

    private static void vertex(QuadSink sink, Vector3f position, float u, float v, int argb, int light) {
        sink.vertex(position.x, position.y, position.z, u, v, argb, light);
    }
}
