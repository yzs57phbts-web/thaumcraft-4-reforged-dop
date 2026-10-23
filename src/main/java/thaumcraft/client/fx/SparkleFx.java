package thaumcraft.client.fx;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;

public class SparkleFx extends ParticleFx {
    private final int multiplier;
    private int blend = 1;
    private boolean shrink = true;

    public SparkleFx(RandomSource random, double x, double y, double z, float size, int type, int multiplier) {
        super(random, x, y, z, 3 * multiplier);
        this.multiplier = multiplier;
        scale *= size;
        float[] color = WispFx.typeColor(random, type);
        color(color[0], color[1], color[2]);
        if (type == 5) {
            blend = 771;
        }
    }

    public SparkleFx noShrink() {
        shrink = false;
        return this;
    }

    @Override
    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        if (age++ >= maxAge) {
            dead = true;
            return;
        }
        motionY -= 0.04 * gravity;
        x += motionX;
        y += motionY;
        z += motionZ;
        motionX *= 0.908;
        motionY *= 0.908;
        motionZ *= 0.908;
    }

    @Override
    public int blendMode() {
        return blend;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        int part = 16 + age / multiplier;
        float u0 = part % 4 / 16.0F;
        float v0 = 0.25F;
        float size = 0.1F * scale;
        if (shrink) {
            size *= (float) (maxAge - age + 1) / maxAge;
        }
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, size, u0, v0, u0 + CELL, v0 + CELL, 1.0F);
    }
}
