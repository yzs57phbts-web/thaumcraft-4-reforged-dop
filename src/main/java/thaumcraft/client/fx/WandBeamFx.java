package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.WorldFxRenderer;

public class WandBeamFx extends BeamFx {
    private final Entity source;
    private final Impact impact;

    public WandBeamFx(Level level, Entity source, double tx, double ty, double tz, float red, float green, float blue, int age) {
        this(level, source, origin(source), tx, ty, tz, red, green, blue, age);
    }

    private WandBeamFx(Level level, Entity source, Vec3 origin, double tx, double ty, double tz, float red, float green, float blue, int age) {
        super(level, origin.x, origin.y, origin.z, tx, ty, tz, red, green, blue, age);
        this.source = source;
        impact = new Impact(this, red, green, blue);
        WorldFxRenderer.add(impact);
    }

    private static Vec3 origin(Entity source) {
        Minecraft minecraft = Minecraft.getInstance();
        double y = source == minecraft.getCameraEntity() ? source.getEyeY() : source.getY() + source.getBbHeight() / 2.0F + 0.25;
        float yaw = source.getYRot() / 180.0F * (float) Math.PI;
        Vec3 look = source.getViewVector(1.0F);
        return new Vec3(
            source.getX() - Mth.cos(yaw) * 0.066F + look.x * 0.3,
            y - 0.06 + look.y * 0.3,
            source.getZ() - Mth.sin(yaw) * 0.04F + look.z * 0.3
        );
    }

    public void setImpact(int ticks) {
        impact.ticks = ticks;
    }

    public boolean isFor(Entity entity) {
        return source == entity && !isDead();
    }

    @Override
    public void tick() {
        Vec3 origin = origin(source);
        follow(origin.x, origin.y, origin.z);
        super.tick();
        if (source.isRemoved()) {
            maxAge = 0;
        }
    }

    private static final class Impact extends ParticleFx {
        private final WandBeamFx beam;
        private int ticks;

        private Impact(WandBeamFx beam, float red, float green, float blue) {
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
