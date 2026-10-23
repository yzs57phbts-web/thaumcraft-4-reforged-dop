package thaumcraft.client.fx;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.WorldFx;
import thaumcraft.client.render.model.ObjModel;

public class ShieldRunesFx extends WorldFx {
    private static final ResourceLocation MODEL = Thaumcraft.id("models/obj/hemis.obj");

    private final Entity target;
    private final float yaw;
    private final float pitch;
    private final boolean mob;

    public ShieldRunesFx(RandomSource random, Entity target, int age, float yaw, float pitch) {
        super(target.getX(), centerY(target), target.getZ(), age + random.nextInt(age / 2));
        this.target = target;
        this.yaw = yaw;
        this.pitch = pitch;
        this.mob = target instanceof Monster && !net.minecraft.world.entity.EntityType.getKey(target.getType()).getPath().contains("cultist");
    }

    private static double centerY(Entity entity) {
        return (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
    }

    @Override
    public void tick() {
        super.tick();
        x = target.getX();
        y = centerY(target);
        z = target.getZ();
    }

    private float fade(float partialTick) {
        return (age + partialTick) / maxAge;
    }

    @Override
    public ResourceLocation texture() {
        int frame = Math.min(15, (int) (14.0F * (float) age / maxAge) + 1);
        return Thaumcraft.id("textures/model/" + (mob ? "ripple" : "hemis") + frame + ".png");
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera) {
        float fade = fade(partialTick);
        float brightness = mob ? 0.5F : 1.0F;
        float alpha = Math.min(1.0F, (1.0F - fade) * 3.0F);
        int color = ((int) (alpha * 255.0F) << 24) | ((int) (brightness * 255.0F) << 16) | ((int) (brightness * 255.0F) << 8) | (int) (brightness * 255.0F);
        float scale = 0.4F * target.getBbHeight();
        Matrix4f transform = new Matrix4f()
            .translate(renderX(partialTick, camera), renderY(partialTick, camera), renderZ(partialTick, camera))
            .rotateY((float) Math.toRadians(180.0F - yaw))
            .rotateX((float) Math.toRadians(-pitch))
            .scale(scale);
        ObjModel.get(MODEL).renderAll(transform, sink, color, 220);
    }
}
