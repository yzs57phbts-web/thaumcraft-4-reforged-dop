package thaumcraft.client.platform;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityRenderState extends EntityRenderState {
    public float yRot;
    public float xRot;
    public float walkAnimationPos;
    public float walkAnimationSpeed;
    public boolean hurt;

    public static void extract(LivingEntity entity, LivingEntityRenderState state, float partialTicks) {
        state.ageInTicks = entity.tickCount + partialTicks;
        state.yRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot) - Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.walkAnimationPos = entity.walkAnimation.position(partialTicks);
        state.walkAnimationSpeed = entity.walkAnimation.speed(partialTicks);
        state.isInvisible = entity.isInvisible();
        state.hurt = entity.hurtTime > 0 || entity.deathTime > 0;
    }
}
