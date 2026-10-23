package thaumcraft.client.geom;

public final class BoxGeometry {
    private BoxGeometry() {
    }

    public record UvRect(float u0, float v0, float u1, float v1) {
        public float u(float fraction) {
            return u0 + (u1 - u0) * fraction;
        }

        public float v(float fraction) {
            return v0 + (v1 - v0) * fraction;
        }
    }

    public static void box(
        QuadSink sink,
        float x1,
        float y1,
        float z1,
        float x2,
        float y2,
        float z2,
        UvRect bottom,
        UvRect top,
        UvRect side,
        int argb,
        int light
    ) {
        sink.vertex(x1, y1, z1, bottom.u(x1), bottom.v(z1), argb, light);
        sink.vertex(x2, y1, z1, bottom.u(x2), bottom.v(z1), argb, light);
        sink.vertex(x2, y1, z2, bottom.u(x2), bottom.v(z2), argb, light);
        sink.vertex(x1, y1, z2, bottom.u(x1), bottom.v(z2), argb, light);
        sink.vertex(x1, y2, z1, top.u(x1), top.v(z1), argb, light);
        sink.vertex(x1, y2, z2, top.u(x1), top.v(z2), argb, light);
        sink.vertex(x2, y2, z2, top.u(x2), top.v(z2), argb, light);
        sink.vertex(x2, y2, z1, top.u(x2), top.v(z1), argb, light);
        sink.vertex(x1, y1, z1, side.u(x1), side.v(1.0F - y1), argb, light);
        sink.vertex(x1, y2, z1, side.u(x1), side.v(1.0F - y2), argb, light);
        sink.vertex(x2, y2, z1, side.u(x2), side.v(1.0F - y2), argb, light);
        sink.vertex(x2, y1, z1, side.u(x2), side.v(1.0F - y1), argb, light);
        sink.vertex(x2, y1, z2, side.u(x2), side.v(1.0F - y1), argb, light);
        sink.vertex(x2, y2, z2, side.u(x2), side.v(1.0F - y2), argb, light);
        sink.vertex(x1, y2, z2, side.u(x1), side.v(1.0F - y2), argb, light);
        sink.vertex(x1, y1, z2, side.u(x1), side.v(1.0F - y1), argb, light);
        sink.vertex(x1, y1, z2, side.u(z2), side.v(1.0F - y1), argb, light);
        sink.vertex(x1, y2, z2, side.u(z2), side.v(1.0F - y2), argb, light);
        sink.vertex(x1, y2, z1, side.u(z1), side.v(1.0F - y2), argb, light);
        sink.vertex(x1, y1, z1, side.u(z1), side.v(1.0F - y1), argb, light);
        sink.vertex(x2, y1, z1, side.u(z1), side.v(1.0F - y1), argb, light);
        sink.vertex(x2, y2, z1, side.u(z1), side.v(1.0F - y2), argb, light);
        sink.vertex(x2, y2, z2, side.u(z2), side.v(1.0F - y2), argb, light);
        sink.vertex(x2, y1, z2, side.u(z2), side.v(1.0F - y1), argb, light);
    }
}
