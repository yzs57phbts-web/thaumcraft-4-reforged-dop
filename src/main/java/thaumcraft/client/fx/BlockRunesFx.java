package thaumcraft.client.fx;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import thaumcraft.client.geom.QuadSink;

public class BlockRunesFx extends ParticleFx {
    private final double offsetX;
    private final double offsetY;
    private final float rotation;
    private final int runeIndex;

    public BlockRunesFx(RandomSource random, double x, double y, double z, float red, float green, float blue, int duration, float gravity) {
        super(random, x, y, z, 3 * duration);
        this.red = red == 0.0F ? 1.0F : red;
        this.green = green;
        this.blue = blue;
        this.gravity = gravity;
        rotation = random.nextInt(4) * 90;
        runeIndex = (int) (random.nextDouble() * 16.0 + 224.0);
        offsetX = random.nextFloat() * 0.2;
        offsetY = -0.3 + random.nextFloat() * 0.6;
        scale = (float) (1.0 + random.nextGaussian() * 0.1F);
        alpha = 0.0F;
    }

    @Override
    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        float threshold = maxAge / 5.0F;
        if (age <= threshold) {
            alpha = age / threshold;
        } else {
            alpha = (float) (maxAge - age) / maxAge;
        }
        if (age++ >= maxAge) {
            dead = true;
        }
        motionY -= 0.04 * gravity;
        x += motionX;
        y += motionY;
        z += motionZ;
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
        Matrix4f matrix = new Matrix4f()
            .translate(renderX(partialTick, camera), renderY(partialTick, camera), renderZ(partialTick, camera))
            .rotateY((float) Math.toRadians(rotation))
            .rotateZ((float) Math.toRadians(90.0F))
            .translate((float) offsetX, (float) offsetY, -0.51F);
        float u0 = runeIndex % 16 / 16.0F;
        float u1 = u0 + CELL;
        float v0 = 0.375F;
        float v1 = v0 + CELL;
        float half = 0.5F * 0.3F * scale;
        int color = argb(red, green, blue, alpha / 2.0F);
        float[][] corners = {{-half, half, u1, v1}, {half, half, u1, v0}, {half, -half, u0, v0}, {-half, -half, u0, v1}};
        Vector3f point = new Vector3f();
        for (float[] corner : corners) {
            matrix.transformPosition(corner[0], corner[1], 0.0F, point);
            sink.vertex(point.x, point.y, point.z, corner[2], corner[3], color, 240);
        }
    }
}
