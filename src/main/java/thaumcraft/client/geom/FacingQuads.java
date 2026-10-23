package thaumcraft.client.geom;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class FacingQuads {
    private FacingQuads() {
    }

    public record Camera(float yaw, float pitch, float x, float y, float z) {
    }

    public static void strip(QuadSink sink, Camera camera, float px, float py, float pz, float angle, float scale, int frames, int strip, int frame, int argb, int light) {
        float u0 = (float) frame / frames;
        float u1 = (float) (frame + 1) / frames;
        float v0 = (float) strip / frames;
        float v1 = (strip + 1.0F) / frames;
        emit(sink, camera, px, py, pz, angle, scale, new float[] {u1, v1, u1, v0, u0, v0, u0, v1}, argb, light);
    }

    public static void frame(QuadSink sink, Camera camera, float px, float py, float pz, float angle, float scale, int frames, int frame, int argb, int light) {
        float u0 = (float) frame / frames;
        float u1 = (float) (frame + 1) / frames;
        emit(sink, camera, px, py, pz, angle, scale, new float[] {u0, 1.0F, u1, 1.0F, u1, 0.0F, u0, 0.0F}, argb, light);
    }

    public static void particle(QuadSink sink, Camera camera, float px, float py, float pz, float scale, float u0, float v0, float u1, float v1, int argb, int light) {
        emit(sink, camera, px, py, pz, 0.0F, scale, new float[] {u1, v1, u1, v0, u0, v0, u0, v1}, argb, light);
    }

    private static void emit(QuadSink sink, Camera camera, float px, float py, float pz, float angle, float scale, float[] uv, int argb, int light) {
        float yaw = (float) Math.toRadians(camera.yaw());
        float pitch = (float) Math.toRadians(camera.pitch());
        float rotationX = (float) Math.cos(yaw);
        float rotationZ = (float) Math.sin(yaw);
        float rotationYZ = -rotationZ * (float) Math.sin(pitch);
        float rotationXY = rotationX * (float) Math.sin(pitch);
        float rotationXZ = (float) Math.cos(pitch);
        Vector3f[] corners = {
            new Vector3f(-rotationX * scale - rotationYZ * scale, -rotationXZ * scale, -rotationZ * scale - rotationXY * scale),
            new Vector3f(-rotationX * scale + rotationYZ * scale, rotationXZ * scale, -rotationZ * scale + rotationXY * scale),
            new Vector3f(rotationX * scale + rotationYZ * scale, rotationXZ * scale, rotationZ * scale + rotationXY * scale),
            new Vector3f(rotationX * scale - rotationYZ * scale, -rotationXZ * scale, rotationZ * scale - rotationXY * scale)
        };
        if (angle != 0.0F) {
            Vector3f axis = new Vector3f(camera.x() - px, camera.y() - py, camera.z() - pz);
            if (axis.lengthSquared() > 1.0E-8F) {
                axis.normalize();
                Quaternionf rotation = new Quaternionf().rotateAxis(angle, axis);
                for (Vector3f corner : corners) {
                    rotation.transform(corner);
                }
            }
        }
        for (int index = 0; index < 4; index++) {
            sink.vertex(px + corners[index].x, py + corners[index].y, pz + corners[index].z, uv[index * 2], uv[index * 2 + 1], argb, light);
        }
    }
}
