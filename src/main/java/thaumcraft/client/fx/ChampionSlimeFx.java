package thaumcraft.client.fx;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.ParticleSprites;

public final class ChampionSlimeFx extends ParticleFx {
    private final TextureAtlasSprite sprite;

    public ChampionSlimeFx(RandomSource random, double x, double y, double z, TextureAtlasSprite sprite) {
        super(random, x, y, z, (int) (66.0F / (random.nextFloat() * 0.9F + 0.1F)));
        this.sprite = sprite;
        scale = 1.0F;
        alpha = 0.4F;
        red = 0.7F;
        green = 0.0F;
        blue = 1.0F;
        gravity = 0.1F;
    }

    @Override public ResourceLocation texture() { return ParticleSprites.atlas(sprite); }

    @Override public void tick() {
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
        motionX *= 0.98;
        motionY *= 0.98;
        motionZ *= 0.98;
    }

    @Override public int blendMode() { return 1; }

    @Override public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        float fade = 1.0F - (float) age / maxAge;
        billboard(sink, partialTick, camera, cameraYaw, cameraPitch, 0.1F * scale * fade,
            ParticleSprites.u(sprite, 0.0F), ParticleSprites.v(sprite, 0.0F),
            ParticleSprites.u(sprite, 1.0F), ParticleSprites.v(sprite, 1.0F), alpha * fade);
    }
}
