package thaumcraft.device;

import net.minecraft.world.phys.Vec3;

public final class SinisterCone {
    public static final float APERTURE = 0.66F;
    public static final float RANGE = 256.0F;
    public static final long EXPIRY_MILLIS = 10000L;

    private SinisterCone() {
    }

    public static boolean isVisibleTo(float aperture, Vec3 eye, Vec3 look, double x, double y, double z, float range) {
        Vec3 axisEnd = eye.add(look.x * range, look.y * range, look.z * range);
        double[] apex = {eye.x - x, eye.y - y, eye.z - z};
        double[] axis = {eye.x - axisEnd.x, eye.y - axisEnd.y, eye.z - axisEnd.z};
        double halfAperture = aperture / 2.0F;
        double dot = apex[0] * axis[0] + apex[1] * axis[1] + apex[2] * axis[2];
        double axisLength = Math.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
        double apexLength = Math.sqrt(apex[0] * apex[0] + apex[1] * apex[1] + apex[2] * apex[2]);
        boolean inInfiniteCone = dot / apexLength / axisLength > Math.cos(halfAperture);
        return inInfiniteCone && dot / axisLength < axisLength;
    }
}
