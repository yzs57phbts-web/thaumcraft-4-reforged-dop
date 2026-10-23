package thaumcraft.client.geom;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class WandGeometry {
    private static final float TEXTURE_WIDTH = 32.0F;
    private static final float TEXTURE_HEIGHT = 32.0F;

    private WandGeometry() {
    }

    public static void rod(QuadSink sink, boolean staff, int light, int argb) {
        Matrix4f matrix = new Matrix4f();
        if (staff) {
            matrix.translate(0.0F, 0.2F, 0.0F).translate(0.0F, -0.1F, 0.0F).scale(1.2F, 2.0F, 1.2F);
        }
        box(sink, matrix, 2.0F, 2, 18, 2, 0, 8, light, argb);
    }

    public static void caps(QuadSink sink, boolean staff, boolean sceptre, int light) {
        int argb = 0xFFFFFFFF;
        Matrix4f matrix = new Matrix4f();
        if (staff) {
            matrix.translate(0.0F, 0.2F, 0.0F);
            matrix.scale(1.3F, 1.1F, 1.3F);
        } else {
            matrix.scale(1.2F, 1.0F, 1.2F);
        }
        if (sceptre) {
            box(sink, new Matrix4f(matrix).scale(1.3F, 1.3F, 1.3F), 0.0F, 2, 2, 2, 0, 0, light, argb);
            box(sink, new Matrix4f(matrix).translate(0.0F, 0.3F, 0.0F).scale(1.0F, 0.66F, 1.0F), 0.0F, 2, 2, 2, 0, 0, light, argb);
        } else {
            box(sink, matrix, 0.0F, 2, 2, 2, 0, 0, light, argb);
        }
        if (staff) {
            matrix.translate(0.0F, 0.225F, 0.0F);
            box(sink, new Matrix4f(matrix).scale(1.0F, 0.66F, 1.0F), 0.0F, 2, 2, 2, 0, 0, light, argb);
            matrix.translate(0.0F, 0.65F, 0.0F);
        }
        box(sink, matrix, 20.0F, 2, 2, 2, 0, 0, light, argb);
    }

    public static void focus(QuadSink sink, boolean staff, int argb, int light) {
        Matrix4f matrix = new Matrix4f();
        if (staff) {
            matrix.translate(0.0F, 0.2F, 0.0F).translate(0.0F, -0.0475F, 0.0F).scale(0.525F, 0.5525F, 0.525F);
        } else {
            matrix.scale(0.5F, 0.5F, 0.5F);
        }
        box(sink, matrix, -5.0F, 6, 6, 6, 0, 0, light, argb);
    }

    public static void focusDepth(QuadSink sink, boolean staff, float u0, float v0, float u1, float v1, int light) {
        Matrix4f matrix = new Matrix4f();
        if (staff) {
            matrix.translate(0.0F, 0.2F, 0.0F).translate(0.0F, -0.15F, 0.0F).scale(0.165F, 0.1765F, 0.165F);
        } else {
            matrix.translate(0.0F, -0.09F, 0.0F).scale(0.16F, 0.16F, 0.16F);
        }
        Vector3f point = new Vector3f();
        for (int face = 0; face < 6; face++) {
            int axis = face / 2;
            float sign = face % 2 == 0 ? -0.5F : 0.5F;
            Vector3f normal = new Vector3f(axis == 0 ? sign : 0.0F, axis == 1 ? sign : 0.0F, axis == 2 ? sign : 0.0F);
            Vector3f u = axis == 1 ? new Vector3f(0.5F, 0.0F, 0.0F) : new Vector3f(0.0F, 0.5F, 0.0F);
            Vector3f v = new Vector3f(normal).normalize().cross(u);
            float[][] corners = {{-1.0F, -1.0F, u0, v1}, {1.0F, -1.0F, u1, v1}, {1.0F, 1.0F, u1, v0}, {-1.0F, 1.0F, u0, v0}};
            for (float[] corner : corners) {
                matrix.transformPosition(
                    normal.x + u.x * corner[0] + v.x * corner[1],
                    normal.y + u.y * corner[0] + v.y * corner[1],
                    normal.z + u.z * corner[0] + v.z * corner[1],
                    point
                );
                sink.vertex(point.x, point.y, point.z, corner[2], corner[3], 0xFFFFFFFF, light);
            }
        }
    }

    public static void focusOrnament(QuadSink sink, boolean staff, float u0, float v0, float u1, float v1, int light) {
        Matrix4f base = new Matrix4f();
        if (staff) {
            base.translate(0.0F, 0.2F, 0.0F);
        }
        base.rotateX((float) Math.PI);
        ornament(sink, new Matrix4f(base).translate(-0.25F, -0.1F, 0.0275F).scale(0.5F, 0.5F, 0.5F), u0, v0, u1, v1, light);
        ornament(sink, new Matrix4f(base).rotateY((float) (Math.PI / 2.0)).translate(-0.25F, -0.1F, 0.0275F).scale(0.5F, 0.5F, 0.5F), u0, v0, u1, v1, light);
    }

    private static void ornament(QuadSink sink, Matrix4f matrix, float u0, float v0, float u1, float v1, int light) {
        float depth = -0.1F;
        Vector3f point = new Vector3f();
        float[][] front = {{0.0F, 0.0F, 0.0F, u1, v1}, {1.0F, 0.0F, 0.0F, u0, v1}, {1.0F, 1.0F, 0.0F, u0, v0}, {0.0F, 1.0F, 0.0F, u1, v0}};
        float[][] back = {{0.0F, 1.0F, depth, u1, v0}, {1.0F, 1.0F, depth, u0, v0}, {1.0F, 0.0F, depth, u0, v1}, {0.0F, 0.0F, depth, u1, v1}};
        for (float[][] face : new float[][][] {front, back}) {
            for (float[] corner : face) {
                matrix.transformPosition(corner[0], corner[1], corner[2], point);
                sink.vertex(point.x, point.y, point.z, corner[3], corner[4], 0xFFFFFFFF, light);
            }
        }
    }

    public static void runes(QuadSink sink, boolean staff, boolean sceptre, boolean primalRunes, float ticks, int light) {
        Matrix4f matrix = new Matrix4f();
        if (staff) {
            matrix.translate(0.0F, 0.2F, 0.0F);
        }
        if (sceptre) {
            for (int rotation = 0; rotation < 10; rotation++) {
                Matrix4f ring = new Matrix4f(matrix).rotateY((float) Math.toRadians(36 * rotation + ticks));
                rune(sink, ring, 0.16F, -0.01F, -0.125F, rotation, ticks, light);
            }
        }
        if (primalRunes) {
            Matrix4f ring = new Matrix4f(matrix);
            for (int rotation = 0; rotation < 4; rotation++) {
                ring.rotateY((float) (Math.PI / 2.0));
                for (int step = 0; step < 14; step++) {
                    int rune = (step + rotation * 3) % 16;
                    rune(sink, ring, 0.36F + step * 0.14F, -0.01F, -0.08F, rune, ticks, light);
                }
            }
        }
    }

    private static void rune(QuadSink sink, Matrix4f parent, float x, float y, float z, int rune, float ticks, int light) {
        float red = (float) Math.sin((ticks + rune * 5) / 5.0F) * 0.1F + 0.88F;
        float green = (float) Math.sin((ticks + rune * 5) / 7.0F) * 0.1F + 0.63F;
        float alpha = (float) Math.sin((ticks + rune * 5) / 10.0F) * 0.2F;
        int argb = (clamp((alpha + 0.6F) * 255.0F) << 24) | (clamp(red * 255.0F) << 16) | (clamp(green * 255.0F) << 8) | clamp(0.2F * 255.0F);
        Matrix4f matrix = new Matrix4f(parent).rotateZ((float) (Math.PI / 2.0)).translate(x, y, z);
        float u0 = 0.0625F * rune;
        float u1 = u0 + 0.0625F;
        float size = 0.06F + alpha / 40.0F;
        Vector3f point = new Vector3f();
        matrix.transformPosition(-size, size, 0.0F, point);
        sink.vertex(point.x, point.y, point.z, u1, 1.0F, argb, light);
        matrix.transformPosition(size, size, 0.0F, point);
        sink.vertex(point.x, point.y, point.z, u1, 0.0F, argb, light);
        matrix.transformPosition(size, -size, 0.0F, point);
        sink.vertex(point.x, point.y, point.z, u0, 0.0F, argb, light);
        matrix.transformPosition(-size, -size, 0.0F, point);
        sink.vertex(point.x, point.y, point.z, u0, 1.0F, argb, light);
    }

    private static int clamp(float value) {
        return Math.max(0, Math.min(255, (int) value));
    }

    private static void box(
        QuadSink sink,
        Matrix4f matrix,
        float pointY,
        int width,
        int height,
        int depth,
        int textureX,
        int textureY,
        int light,
        int argb
    ) {
        float x1 = -width / 32.0F;
        float x2 = width / 32.0F;
        float y1 = (pointY - 1.0F) / 16.0F;
        float y2 = (pointY - 1.0F + height) / 16.0F;
        float z1 = -depth / 32.0F;
        float z2 = depth / 32.0F;
        float frontU = textureX + depth;
        float rightU = textureX + depth + width;
        float backU = textureX + 2 * depth + width;
        float sideV = textureY + depth;
        quad(sink, matrix, light, argb, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, rightU, sideV, rightU + depth, sideV + height);
        quad(sink, matrix, light, argb, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, textureX, sideV, textureX + depth, sideV + height);
        quad(sink, matrix, light, argb, x2, y1, z2, x1, y1, z2, x1, y1, z1, x2, y1, z1, frontU, textureY, frontU + width, textureY + depth);
        quad(sink, matrix, light, argb, x2, y2, z1, x1, y2, z1, x1, y2, z2, x2, y2, z2, rightU, sideV, rightU + width, textureY);
        quad(sink, matrix, light, argb, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, frontU, sideV, frontU + width, sideV + height);
        quad(sink, matrix, light, argb, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, backU, sideV, backU + width, sideV + height);
    }

    private static void quad(
        QuadSink sink,
        Matrix4f matrix,
        int light,
        int argb,
        float ax,
        float ay,
        float az,
        float bx,
        float by,
        float bz,
        float cx,
        float cy,
        float cz,
        float dx,
        float dy,
        float dz,
        float u0,
        float v0,
        float u1,
        float v1
    ) {
        Vector3f point = new Vector3f();
        matrix.transformPosition(ax, ay, az, point);
        sink.vertex(point.x, point.y, point.z, u1 / TEXTURE_WIDTH, v0 / TEXTURE_HEIGHT, argb, light);
        matrix.transformPosition(bx, by, bz, point);
        sink.vertex(point.x, point.y, point.z, u0 / TEXTURE_WIDTH, v0 / TEXTURE_HEIGHT, argb, light);
        matrix.transformPosition(cx, cy, cz, point);
        sink.vertex(point.x, point.y, point.z, u0 / TEXTURE_WIDTH, v1 / TEXTURE_HEIGHT, argb, light);
        matrix.transformPosition(dx, dy, dz, point);
        sink.vertex(point.x, point.y, point.z, u1 / TEXTURE_WIDTH, v1 / TEXTURE_HEIGHT, argb, light);
    }
}
