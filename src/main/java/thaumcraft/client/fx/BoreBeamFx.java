package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.WorldFxRenderer;

public class BoreBeamFx extends BeamFx {
    private final Impact impact;

    public BoreBeamFx(Level level, double x, double y, double z, double tx, double ty, double tz, float red, float green, float blue, int age) {
        super(level, x, y, z, tx, ty, tz, red, green, blue, age);
        impact = new Impact(this, red, green, blue);
        WorldFxRenderer.add(impact);
    }

    public void setImpact(int ticks) {
        impact.ticks = ticks;
    }

    private static final class Impact extends ParticleFx {
        private final BoreBeamFx beam;
        private int ticks;

        private Impact(BoreBeamFx beam, float red, float green, float blue) {
            super(Minecraft.getInstance().level.getRandom(), beam.targetX(), beam.targetY(), beam.targetZ(), Integer.MAX_VALUE);
            this.beam = beam;
            color(red, green, blue);
        }

        @Override
        public void tick() {
            prevX = x;
            prevY = y;
            prevZ = z;
            x = beam.targetX();
            y = beam.targetY();
            z = beam.targetZ();
            if (ticks > 0) {
                ticks--;
            }
            age++;
        }

        @Override
        public boolean isDead() {
            return beam.isDead();
        }

        @Override
        public int blendMode() {
            return 1;
        }

        @Override
        public void render(QuadSink sink, float partialTick, Vec3 camera, float cameraYaw, float cameraPitch) {
            if (ticks <= 0) {
                return;
            }
            float u0 = age % 16 / 16.0F;
            float size = beam.endMod() / 2.0F / (6 - ticks);
            billboard(sink, partialTick, camera, cameraYaw, cameraPitch, size, u0, 0.3125F, u0 + CELL, 0.3125F + CELL, 0.66F);
        }
    }
}
