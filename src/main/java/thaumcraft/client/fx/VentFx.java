package thaumcraft.client.fx;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;

public class VentFx extends ParticleFx {
    private final float maxScale;

    public VentFx(RandomSource random, double x, double y, double z, double directionX, double directionY, double directionZ, int color, float factor) {
        super(random, x, y, z, Integer.MAX_VALUE);
        scale = (random.nextFloat() * 0.1F + 0.05F) * factor;
        maxScale = factor;
        red = (color >> 16 & 255) / 255.0F;
        green = (color >> 8 & 255) / 255.0F;
        blue = (color & 255) / 255.0F;
        alpha = 0.4F;
        setHeading(directionX, directionY, directionZ, 0.125F, 5.0F);
        limitDistance(50);
    }

    private void setHeading(double headingX, double headingY, double headingZ, float speed, float spread) {
        double length = Math.sqrt(headingX * headingX + headingY * headingY + headingZ * headingZ);
        if (length > 1.0E-6) {
            headingX /= length;
            headingY /= length;
            headingZ /= length;
        }
        headingX += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075F * spread;
        headingY += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075F * spread;
        headingZ += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075F * spread;
        motionX = headingX * speed;
        motionY = headingY * speed;
        motionZ = headingZ * speed;
    }

    @Override
    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        age++;
        if (scale > maxScale) {
            dead = true;
        }
        motionY += 0.0025;
        x += motionX;
        y += motionY;
        z += motionZ;
        motionX *= 0.85F;
        motionY *= 0.85F;
        motionZ *= 0.85F;
        if (scale < maxScale) {
            scale = (float) (scale * 1.15);
        }
    }

    @Override
    public int blendMode() {
        return 771;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        int part = (int) (1.0F + scale / maxScale * 4.0F);
        float fade = alpha * ((maxScale - scale) / maxScale);
        cell(sink, partialTick, camera, cameraYaw, cameraPitch, 0.3F * scale, part, fade);
    }
}
