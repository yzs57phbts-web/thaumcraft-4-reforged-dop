package thaumcraft.client.geom;

/** Positions the four arms in the door plane, around its outward normal. */
public final class EldritchLockGeometry {
    private EldritchLockGeometry() {
    }

    public static int segmentCount(int side, int phase) {
        return Math.max(0, 4 - Math.max(0, phase + side * 5) / 20);
    }

    public static Segment segment(int side, int level, int normalX, int normalZ, float time) {
        float distance = 0.25F + 0.5F * level;
        int horizontal = side == 1 ? 1 : side == 3 ? -1 : 0;
        int vertical = side == 0 ? 1 : side == 2 ? -1 : 0;
        float wave = (float) Math.sin((time + level * 10.0F + side * 20.0F) / 20.0F) * 0.1F;
        if (level == 1 || level == 4) {
            wave = wave / 2.0F + 0.2F;
        }
        return new Segment(-normalZ * horizontal * distance, vertical * distance,
            normalX * horizontal * distance, 0.5F + wave);
    }

    public record Segment(float x, float y, float z, float width) {
    }
}
