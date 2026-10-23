package thaumcraft.client.fx;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.ParticleSprites;

public class BoreParticleFx extends BoreSparkleFx {
    private final TextureAtlasSprite sprite;
    private final float jitterX;
    private final float jitterY;

    public BoreParticleFx(RandomSource random, double x, double y, double z, double targetX, double targetY, double targetZ, TextureAtlasSprite sprite) {
        super(random, x, y, z, targetX, targetY, targetZ);
        this.sprite = sprite;
        jitterX = random.nextFloat() * 3.0F;
        jitterY = random.nextFloat() * 3.0F;
        scale = random.nextFloat() * 0.3F + 0.4F;
        red = 0.6F;
        green = 0.6F;
        blue = 0.6F;
        gravity = 0.2F;
    }

    @Override
    public ResourceLocation texture() {
        return ParticleSprites.atlas(sprite);
    }

    @Override
    public int blendMode() {
        return 771;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        float u0 = ParticleSprites.u(sprite, jitterX / 4.0F);
        float u1 = ParticleSprites.u(sprite, (jitterX + 1.0F) / 4.0F);
        float v0 = ParticleSprites.v(sprite, jitterY / 4.0F);
        float v1 = ParticleSprites.v(sprite, (jitterY + 1.0F) / 4.0F);
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, 0.1F * scale, u0, v0, u1, v1, 1.0F);
    }
}
