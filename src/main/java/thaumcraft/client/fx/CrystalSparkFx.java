package thaumcraft.client.fx;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.QuadSink;

public class CrystalSparkFx extends ParticleFx {
    private static final ResourceLocation SPARKS = Thaumcraft.id("textures/misc/particles2.png");

    private final int particle;
    private final boolean flip;

    public CrystalSparkFx(RandomSource random, double x, double y, double z, float size) {
        super(random, x, y, z, 5 + random.nextInt(5));
        scale = size;
        particle = random.nextInt(3) * 8;
        flip = random.nextBoolean();
    }

    @Override
    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        if (age++ >= maxAge) {
            dead = true;
        }
    }

    @Override
    public ResourceLocation texture() {
        return SPARKS;
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        int part = particle + (int) ((float) age / maxAge * 7.0F);
        float u0 = part % 8 / 8.0F;
        float u1 = u0 + 0.125F;
        float v0 = part / 8 / 8.0F;
        float v1 = v0 + 0.125F;
        if (flip) {
            float swap = u0;
            u0 = u1;
            u1 = swap;
        }
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, scale, u0, v0, u1, v1, alpha);
    }
}
