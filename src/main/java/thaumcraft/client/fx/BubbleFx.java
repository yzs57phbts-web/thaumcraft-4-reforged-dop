package thaumcraft.client.fx;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;

public class BubbleFx extends ParticleFx {
    private int particle = 16;
    private double bubbleSpeed = 0.002;
    private int life;

    public BubbleFx(RandomSource random, double x, double y, double z, int age) {
        super(random, x, y, z, Integer.MAX_VALUE);
        red = 1.0F;
        green = 0.0F;
        blue = 0.5F;
        scale *= random.nextFloat() * 0.3F + 0.2F;
        motionX = (random.nextFloat() * 2.0F - 1.0F) * 0.02F;
        motionY = random.nextFloat() * 0.02F;
        motionZ = (random.nextFloat() * 2.0F - 1.0F) * 0.02F;
        life = (int) (age + 2 + 8.0 / (random.nextDouble() * 0.8 + 0.2));
        limitDistance(50);
    }

    public BubbleFx bubbleSpeed(double bubbleSpeed) {
        this.bubbleSpeed = bubbleSpeed;
        return this;
    }

    public BubbleFx scaleTo(float particleSize) {
        scale = particleSize * 10.0F;
        return this;
    }

    public BubbleFx froth() {
        scale *= 0.75F;
        life = 4 + random.nextInt(3);
        bubbleSpeed = -0.001;
        motionX /= 5.0;
        motionY /= 10.0;
        motionZ /= 5.0;
        return this;
    }

    public BubbleFx froth2() {
        scale *= 0.75F;
        life = 12 + random.nextInt(12);
        bubbleSpeed = -0.005;
        motionX /= 5.0;
        motionY /= 10.0;
        motionZ /= 5.0;
        return this;
    }

    @Override
    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        motionY += bubbleSpeed;
        if (bubbleSpeed > 0.0) {
            motionX += (random.nextFloat() - random.nextFloat()) * 0.01F;
            motionZ += (random.nextFloat() - random.nextFloat()) * 0.01F;
        }
        x += motionX;
        y += motionY;
        z += motionZ;
        motionX *= 0.85F;
        motionY *= 0.85F;
        motionZ *= 0.85F;
        if (life-- <= 0) {
            dead = true;
        } else if (life <= 2) {
            particle++;
        }
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        cell(sink, partialTick, camera, cameraYaw, cameraPitch, 0.1F * scale, particle, alpha);
    }
}
