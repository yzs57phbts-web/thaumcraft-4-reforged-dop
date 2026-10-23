package thaumcraft.client.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;

public abstract class WorldFx {
    protected double x;
    protected double y;
    protected double z;
    protected double prevX;
    protected double prevY;
    protected double prevZ;
    protected int age;
    protected int maxAge;

    protected WorldFx(double x, double y, double z, int maxAge) {
        this.x = x;
        this.y = y;
        this.z = z;
        prevX = x;
        prevY = y;
        prevZ = z;
        this.maxAge = maxAge;
    }

    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        age++;
    }

    public boolean isDead() {
        return age > maxAge;
    }

    protected float renderX(float partialTick, Vec3 camera) {
        return (float) (Mth.lerp(partialTick, prevX, x) - camera.x);
    }

    protected float renderY(float partialTick, Vec3 camera) {
        return (float) (Mth.lerp(partialTick, prevY, y) - camera.y);
    }

    protected float renderZ(float partialTick, Vec3 camera) {
        return (float) (Mth.lerp(partialTick, prevZ, z) - camera.z);
    }

    public abstract ResourceLocation texture();

    public abstract int blendMode();

    public boolean depthTest() {
        return true;
    }

    public void render(QuadSink sink, float partialTick, Vec3 camera) {
    }

    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        render(sink, partialTick, camera);
    }
}
