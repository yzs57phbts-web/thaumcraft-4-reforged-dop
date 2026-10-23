package thaumcraft.client.geom;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class PowerBeamGeometry {
    private static final int LIGHT = 200;
    private static final float SIZE = 0.7F;

    private PowerBeamGeometry() {
    }

    public static void render(float x, float y, float z, float targetX, float targetY, float targetZ, float slide, float opacity, int argb, QuadSink sink) {
        float dx = x - targetX;
        float dy = y - targetY;
        float dz = z - targetZ;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-4F) {
            return;
        }
        float horizontal = (float) Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);
        float pitch = (float) (Math.atan2(dy, horizontal) * 180.0 / Math.PI);
        float offset = -slide * 0.2F - (float) Math.floor(-slide * 0.1F);
        Matrix4f matrix = new Matrix4f()
            .translate(x, y, z)
            .rotateX((float) Math.toRadians(90.0F))
            .rotateZ((float) Math.toRadians(-(180.0F + yaw)))
            .rotateX((float) Math.toRadians(pitch));
        float near = -0.15F * SIZE;
        float far = 0.15F * SIZE;
        int color = (Math.clamp(Math.round(opacity * 255.0F), 0, 255) << 24) | (argb & 0xFFFFFF);
        Vector3f position = new Vector3f();
        for (int strip = 0; strip < 2; strip++) {
            float v0 = -1.0F + offset + strip / 3.0F;
            float v1 = length + v0;
            matrix.rotateY((float) Math.toRadians(90.0F));
            emit(matrix, position, sink, near, length, 1.0F, v1, color);
            emit(matrix, position, sink, near, 0.0F, 1.0F, v0, color);
            emit(matrix, position, sink, far, 0.0F, 0.0F, v0, color);
            emit(matrix, position, sink, far, length, 0.0F, v1, color);
        }
    }

    private static void emit(Matrix4f matrix, Vector3f position, QuadSink sink, float x, float y, float u, float v, int color) {
        matrix.transformPosition(x, y, 0.0F, position);
        sink.vertex(position.x, position.y, position.z, u, v, color, LIGHT);
    }
}
