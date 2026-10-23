package thaumcraft.client.fx;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;

public class BoreSparkleFx extends ParticleFx {
    protected final double targetX;
    protected final double targetY;
    protected final double targetZ;

    public BoreSparkleFx(RandomSource random, double x, double y, double z, double targetX, double targetY, double targetZ) {
        super(random, x, y, z, 0);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        scale = random.nextFloat() * 0.5F + 0.5F;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        int base = (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0F);
        if (base < 1) {
            base = 1;
        }
        maxAge = base / 2 + random.nextInt(base);
        motionX = (float) random.nextGaussian() * 0.01F;
        motionY = (float) random.nextGaussian() * 0.01F;
        motionZ = (float) random.nextGaussian() * 0.01F;
        limitDistance(64);
    }

    protected final void boreTick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        if (age++ < maxAge && (Mth.floor(x) != Mth.floor(targetX) || Mth.floor(y) != Mth.floor(targetY) || Mth.floor(z) != Mth.floor(targetZ))) {
            x += motionX;
            y += motionY;
            z += motionZ;
            motionX *= 0.985;
            motionY *= 0.985;
            motionZ *= 0.985;
            double dx = targetX - x;
            double dy = targetY - y;
            double dz = targetZ - z;
            double pull = 0.3;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 4.0) {
                scale *= 0.9F;
                pull = 0.6;
            }
            if (distance > 1.0E-6) {
                dx /= distance;
                dy /= distance;
                dz /= distance;
                motionX += dx * pull;
                motionY += dy * pull;
                motionZ += dz * pull;
            }
            motionX = Mth.clamp((float) motionX, -0.35F, 0.35F);
            motionY = Mth.clamp((float) motionY, -0.35F, 0.35F);
            motionZ = Mth.clamp((float) motionZ, -0.35F, 0.35F);
        } else {
            dead = true;
        }
    }

    @Override
    public void tick() {
        boreTick();
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        float bob = Mth.sin(age / 3.0F) * 0.5F + 1.0F;
        float u0 = age % 4 / 16.0F;
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, 0.1F * scale * bob, u0, 0.25F, u0 + CELL, 0.25F + CELL, 1.0F);
    }
}
