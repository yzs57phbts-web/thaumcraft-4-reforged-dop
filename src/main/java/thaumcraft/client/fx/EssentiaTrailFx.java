package thaumcraft.client.fx;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;

public class EssentiaTrailFx extends ParticleFx {
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int count;

    public EssentiaTrailFx(RandomSource random, double x, double y, double z, double targetX, double targetY, double targetZ, int count, int color, float scaleFactor) {
        super(random, x, y, z, 0);
        this.count = count;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        scale = (Mth.sin(count / 2.0F) * 0.1F + 1.0F) * scaleFactor;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        int base = (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 30.0F);
        if (base < 1) {
            base = 1;
        }
        maxAge = base / 2 + random.nextInt(base);
        motionX = Mth.sin(count / 4.0F) * 0.015F + random.nextGaussian() * 0.002F;
        motionY = 0.1F + Mth.sin(count / 3.0F) * 0.01F;
        motionZ = Mth.sin(count / 2.0F) * 0.015F + random.nextGaussian() * 0.002F;
        float mr = (color >> 16 & 255) / 255.0F * 0.2F;
        float mg = (color >> 8 & 255) / 255.0F * 0.2F;
        float mb = (color & 255) / 255.0F * 0.2F;
        red = (color >> 16 & 255) / 255.0F - mr + random.nextFloat() * mr;
        green = (color >> 8 & 255) / 255.0F - mg + random.nextFloat() * mg;
        blue = (color & 255) / 255.0F - mb + random.nextFloat() * mb;
        gravity = 0.2F;
        limitDistance(64);
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
        motionY += 0.01 * gravity;
        x += motionX;
        y += motionY;
        z += motionZ;
        motionX *= 0.985;
        motionY *= 0.985;
        motionZ *= 0.985;
        motionX = Mth.clamp((float) motionX, -0.05F, 0.05F);
        motionY = Mth.clamp((float) motionY, -0.05F, 0.05F);
        motionZ = Mth.clamp((float) motionZ, -0.05F, 0.05F);
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double pull = 0.01;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 2.0) {
            scale *= 0.98F;
        }
        if (scale < 0.2F) {
            dead = true;
        } else if (distance > 1.0E-6) {
            dx /= distance;
            dy /= distance;
            dz /= distance;
            motionX += dx * (pull / Math.min(1.0, distance));
            motionY += dy * (pull / Math.min(1.0, distance));
            motionZ += dz * (pull / Math.min(1.0, distance));
        }
    }

    @Override
    public int blendMode() {
        return 771;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        float pulse = Mth.sin((age - count) / 5.0F) * 0.25F + 1.0F;
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, 0.1F * scale * pulse, 0.5625F, 0.0625F, 0.625F, 0.125F, 0.5F);
    }
}
