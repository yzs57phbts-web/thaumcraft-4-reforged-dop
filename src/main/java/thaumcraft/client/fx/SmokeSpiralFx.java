package thaumcraft.client.fx;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.FacingQuads;
import thaumcraft.client.geom.QuadSink;

public class SmokeSpiralFx extends ParticleFx {
    private final float radius;
    private final int start;
    private final int minY;

    public SmokeSpiralFx(RandomSource random, double x, double y, double z, float radius, int start, int minY, int color) {
        super(random, x, y, z, 20 + random.nextInt(10));
        this.radius = radius;
        this.start = start;
        this.minY = minY;
        color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }

    @Override
    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        alpha = (float) (maxAge - age) / maxAge;
        if (age++ >= maxAge) {
            dead = true;
        }
    }

    @Override
    public int blendMode() {
        return 771;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        float progress = (age + partialTick) / maxAge;
        int particle = (int) (1.0F + (float) age / maxAge * 4.0F);
        float r1 = start + 720.0F * progress;
        float r2 = 90.0F - 180.0F * progress;
        float mx = -Mth.sin(r1 / 180.0F * (float) Math.PI) * Mth.cos(r2 / 180.0F * (float) Math.PI) * radius;
        float mz = Mth.cos(r1 / 180.0F * (float) Math.PI) * Mth.cos(r2 / 180.0F * (float) Math.PI) * radius;
        float my = -Mth.sin(r2 / 180.0F * (float) Math.PI) * radius;
        float u0 = particle % 16 / 16.0F;
        float v0 = particle / 16 / 16.0F;
        FacingQuads.particle(
            sink,
            new FacingQuads.Camera(cameraYaw, cameraPitch, 0.0F, 0.0F, 0.0F),
            (float) (x + mx - camera.x),
            (float) (Math.max(y + my, minY + 0.1F) - camera.y),
            (float) (z + mz - camera.z),
            0.15F * scale,
            u0,
            v0,
            u0 + CELL,
            v0 + CELL,
            argb(red, green, blue, 0.66F * alpha),
            240
        );
    }
}
