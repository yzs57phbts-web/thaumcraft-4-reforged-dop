package thaumcraft.client.fx;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.client.geom.QuadSink;

public class WispFx extends ParticleFx {
    private final float baseScale;
    private final int halfLife;
    private final @Nullable Entity target;
    private int blend = 1;
    private boolean shrink;

    public WispFx(RandomSource random, double x, double y, double z, float size, int type, @Nullable Entity target) {
        super(random, x, y, z, (int) (36.0 / (Math.random() * 0.3 + 0.7)));
        float[] color = typeColor(random, type);
        color(color[0], color[1], color[2]);
        if (type == 5) {
            blend = 771;
        }
        scale *= size;
        baseScale = scale;
        halfLife = Math.max(1, maxAge / 2);
        this.target = target;
        limitDistance(50);
    }

    public static float[] typeColor(RandomSource random, int type) {
        return switch (type) {
            case 0 -> new float[] {0.75F + random.nextFloat() * 0.25F, 0.25F + random.nextFloat() * 0.25F, 0.75F + random.nextFloat() * 0.25F};
            case 1 -> new float[] {0.5F + random.nextFloat() * 0.3F, 0.5F + random.nextFloat() * 0.3F, 0.2F};
            case 2 -> new float[] {0.2F, 0.2F, 0.7F + random.nextFloat() * 0.3F};
            case 3 -> new float[] {0.2F, 0.7F + random.nextFloat() * 0.3F, 0.2F};
            case 4 -> new float[] {0.7F + random.nextFloat() * 0.3F, 0.2F, 0.2F};
            case 5 -> new float[] {random.nextFloat() * 0.1F, random.nextFloat() * 0.1F, random.nextFloat() * 0.1F};
            case 6 -> new float[] {0.8F + random.nextFloat() * 0.2F, 0.8F + random.nextFloat() * 0.2F, 0.8F + random.nextFloat() * 0.2F};
            case 7 -> new float[] {0.7F + random.nextFloat() * 0.3F, 0.5F + random.nextFloat() * 0.2F, 0.3F + random.nextFloat() * 0.1F};
            default -> new float[] {1.0F, 0.0F, 0.0F};
        };
    }

    public WispFx moveTo(double targetX, double targetY, double targetZ) {
        if (maxAge > 0) {
            motionX = (targetX - x) / maxAge;
            motionY = (targetY - y) / maxAge;
            motionZ = (targetZ - z) / maxAge;
        }
        return this;
    }

    public WispFx shrink(boolean shrink) {
        this.shrink = shrink;
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
        if (target != null) {
            motionX *= 0.985;
            motionY *= 0.985;
            motionZ *= 0.985;
            double dx = target.getX() - x;
            double dy = target.getY() + target.getBbHeight() / 2.0F - y;
            double dz = target.getZ() - z;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length > 0.0) {
                motionX = Mth.clamp(motionX + dx / length * 0.2, -0.2, 0.2);
                motionY = Mth.clamp(motionY + dy / length * 0.2, -0.2, 0.2);
                motionZ = Mth.clamp(motionZ + dz / length * 0.2, -0.2, 0.2);
            }
        } else {
            motionX *= 0.98F;
            motionY *= 0.98F;
            motionZ *= 0.98F;
        }
    }

    @Override
    public int blendMode() {
        return blend;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        float ageScale;
        if (shrink) {
            ageScale = ((float) maxAge - age) / maxAge;
        } else {
            ageScale = (float) age / halfLife;
            if (ageScale > 1.0F) {
                ageScale = 2.0F - ageScale;
            }
        }
        float size = 0.5F * baseScale * ageScale;
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, size, 0.0F, 0.875F, 0.125F, 1.0F, 0.5F);
    }
}
