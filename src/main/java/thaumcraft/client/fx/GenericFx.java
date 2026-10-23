package thaumcraft.client.fx;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;

public class GenericFx extends ParticleFx {
    private final boolean loop;
    private final int delay;
    private final int startParticle;
    private final int numParticles;
    private final int particleInc;
    private int particle;

    public GenericFx(
        RandomSource random,
        double x,
        double y,
        double z,
        double motionX,
        double motionY,
        double motionZ,
        boolean loop,
        int startParticle,
        int numParticles,
        int particleInc,
        int age,
        int delay,
        float scale
    ) {
        super(random, x, y, z, age + delay);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.loop = loop;
        this.delay = delay;
        this.startParticle = startParticle;
        this.numParticles = numParticles;
        this.particleInc = particleInc;
        this.scale = scale;
        particle = startParticle;
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
        motionX *= 0.98F;
        motionY *= 0.98F;
        motionZ *= 0.98F;
        if (loop) {
            particle = startParticle + age / particleInc % numParticles;
        } else {
            float fraction = (float) age / maxAge;
            particle = (int) (startParticle + Math.min(numParticles * fraction, numParticles - 1));
        }
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        if (age < delay) {
            return;
        }
        float shown = age <= 1 || age >= maxAge - 1 ? alpha / 2.0F : alpha;
        cell(sink, partialTick, camera, cameraYaw, cameraPitch, 0.1F * scale, particle, shown);
    }
}
