package thaumcraft.client.fx;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.BeamGeometry;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.WorldFx;

public class BeamFx extends WorldFx {
    private static final ResourceLocation[] TEXTURES = {
        Thaumcraft.id("textures/misc/beam.png"),
        Thaumcraft.id("textures/misc/beam1.png"),
        Thaumcraft.id("textures/misc/beam2.png"),
        Thaumcraft.id("textures/misc/beam3.png")
    };

    private final Level level;
    private final @Nullable Entity targetEntity;
    private final float red;
    private final float green;
    private final float blue;
    private boolean updated;
    private double moveX;
    private double moveY;
    private double moveZ;
    private double targetX;
    private double targetY;
    private double targetZ;
    private float length;
    private float yaw;
    private float pitch;
    private float prevYaw;
    private float prevPitch;
    private float prevSize;
    private int type;
    private float endMod = 1.0F;
    private boolean reverse;
    private boolean pulse = true;
    private int rotationSpeed = 5;
    private int blendMode = 1;
    private float width = 1.0F;

    public BeamFx(Level level, double x, double y, double z, double targetX, double targetY, double targetZ, float red, float green, float blue, int age) {
        super(x, y, z, age);
        this.level = level;
        this.targetEntity = null;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        init();
    }

    public BeamFx(Level level, double x, double y, double z, Entity target, float red, float green, float blue, int age) {
        super(x, y, z, age);
        this.level = level;
        this.targetEntity = target;
        this.red = red;
        this.green = green;
        this.blue = blue;
        trackTarget();
        init();
    }

    private void init() {
        updateAngles();
        prevYaw = yaw;
        prevPitch = pitch;
        Minecraft minecraft = Minecraft.getInstance();
        int visibleDistance = minecraft.options.graphicsMode().get() == GraphicsStatus.FAST ? 25 : 50;
        Entity viewer = minecraft.getCameraEntity();
        if (viewer != null && viewer.position().distanceTo(new Vec3(x, y, z)) > visibleDistance) {
            maxAge = 0;
        }
    }

    private void trackTarget() {
        if (targetEntity != null) {
            targetX = targetEntity.getX();
            targetY = targetEntity.getY() + targetEntity.getEyeHeight() - targetEntity.getBbHeight() / 2.0F;
            targetZ = targetEntity.getZ();
        }
    }

    private void updateAngles() {
        float dx = (float) (x - targetX);
        float dy = (float) (y - targetY);
        float dz = (float) (z - targetZ);
        length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        double horizontal = Mth.sqrt(dx * dx + dz * dz);
        yaw = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);
        pitch = (float) (Math.atan2(dy, horizontal) * 180.0 / Math.PI);
    }

    public void updateBeam(double sx, double sy, double sz, double tx, double ty, double tz) {
        moveX = sx;
        moveY = sy;
        moveZ = sz;
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        while (maxAge - age < 4) {
            maxAge++;
        }
        updated = true;
    }

    protected void follow(double sx, double sy, double sz) {
        moveX = sx;
        moveY = sy;
        moveZ = sz;
        updated = true;
    }

    public void retarget(double tx, double ty, double tz) {
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        while (maxAge - age < 4) {
            maxAge++;
        }
    }

    public double targetX() {
        return targetX;
    }

    public double targetY() {
        return targetY;
    }

    public double targetZ() {
        return targetZ;
    }

    public float endMod() {
        return endMod;
    }

    public void setType(int type) {
        this.type = Math.clamp(type, 0, TEXTURES.length - 1);
    }

    public void setEndMod(float endMod) {
        this.endMod = endMod;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public void setPulse(boolean pulse) {
        this.pulse = pulse;
    }

    public void setRotationSpeed(int rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setBlendMode(int blendMode) {
        this.blendMode = blendMode;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    @Override
    public void tick() {
        super.tick();
        if (updated) {
            x = moveX;
            y = moveY;
            z = moveZ;
            updated = false;
        }
        prevYaw = yaw;
        prevPitch = pitch;
        trackTarget();
        updateAngles();
    }

    @Override
    public ResourceLocation texture() {
        return TEXTURES[type];
    }

    @Override
    public int blendMode() {
        return blendMode;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera) {
        Minecraft minecraft = Minecraft.getInstance();
        float playerTicks = minecraft.player == null ? 0.0F : minecraft.player.tickCount;
        BeamGeometry.Params params = new BeamGeometry.Params(
            renderX(partialTick, camera),
            renderY(partialTick, camera),
            renderZ(partialTick, camera),
            Mth.lerp(partialTick, prevYaw, yaw),
            Mth.lerp(partialTick, prevPitch, pitch),
            length,
            width,
            endMod,
            pulse,
            reverse,
            rotationSpeed,
            age,
            maxAge,
            prevSize,
            level.getDayTime(),
            playerTicks,
            partialTick,
            red,
            green,
            blue
        );
        prevSize = BeamGeometry.render(params, sink);
    }
}
