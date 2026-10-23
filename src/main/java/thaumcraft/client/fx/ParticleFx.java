package thaumcraft.client.fx;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.FacingQuads;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.WorldFx;

public abstract class ParticleFx extends WorldFx {
    public static final ResourceLocation PARTICLES = Thaumcraft.id("textures/misc/particles.png");
    public static final float CELL = 0.0624375F;

    protected final RandomSource random;
    protected double motionX;
    protected double motionY;
    protected double motionZ;
    protected float red = 1.0F;
    protected float green = 1.0F;
    protected float blue = 1.0F;
    protected float alpha = 1.0F;
    protected float scale;
    protected float gravity;
    protected boolean dead;

    protected ParticleFx(RandomSource random, double x, double y, double z, int maxAge) {
        super(x, y, z, maxAge);
        this.random = random;
        scale = (random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    public ParticleFx color(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    public ParticleFx alpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public ParticleFx motion(double motionX, double motionY, double motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        return this;
    }

    public ParticleFx gravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    protected void limitDistance(int fancyDistance) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() == null) {
            return;
        }
        int visibleDistance = minecraft.options.graphicsMode().get() == GraphicsStatus.FAST ? fancyDistance / 2 : fancyDistance;
        if (minecraft.getCameraEntity().distanceToSqr(x, y, z) > (double) visibleDistance * visibleDistance) {
            dead = true;
        }
    }

    @Override
    public boolean isDead() {
        return dead || super.isDead();
    }

    @Override
    public ResourceLocation texture() {
        return PARTICLES;
    }

    protected static int argb(float red, float green, float blue, float alpha) {
        return Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24
            | Mth.clamp((int) (red * 255.0F), 0, 255) << 16
            | Mth.clamp((int) (green * 255.0F), 0, 255) << 8
            | Mth.clamp((int) (blue * 255.0F), 0, 255);
    }

    protected void billboard(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch, float size, float u0, float v0, float u1, float v1, float alpha) {
        FacingQuads.particle(
            sink,
            new FacingQuads.Camera(cameraYaw, cameraPitch, 0.0F, 0.0F, 0.0F),
            renderX(partialTick, camera),
            renderY(partialTick, camera),
            renderZ(partialTick, camera),
            size,
            u0,
            v0,
            u1,
            v1,
            argb(red, green, blue, alpha),
            240
        );
    }

    protected void cell(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch, float size, int index, float alpha) {
        float u0 = index % 16 / 16.0F;
        float v0 = index / 16 / 16.0F;
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, size, u0, v0, u0 + CELL, v0 + CELL, alpha);
    }
}
