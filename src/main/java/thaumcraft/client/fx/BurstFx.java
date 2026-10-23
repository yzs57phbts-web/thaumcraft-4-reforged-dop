package thaumcraft.client.fx;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.QuadSink;

public class BurstFx extends ParticleFx {
    private static final ResourceLocation NODES = Thaumcraft.id("textures/misc/nodes.png");

    public BurstFx(RandomSource random, double x, double y, double z, float size) {
        super(random, x, y, z, 31);
        scale *= size;
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
        return NODES;
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        float u0 = age % 32 / 32.0F;
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, scale, u0, 0.96875F, u0 + 0.03125F, 1.0F, 1.0F);
    }
}
