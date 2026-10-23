package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.monster.Zombie;
import thaumcraft.entity.monster.GiantBrainyZombie;

public final class GiantBrainyZombieRenderer extends BrainyZombieRenderer {
    public GiantBrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(Zombie entity, PoseStack poseStack, float partialTickTime) {
        super.scale(entity, poseStack, partialTickTime);
        if (entity instanceof GiantBrainyZombie giant) {
            float scale = Math.max(1.0F, giant.getAnger());
            poseStack.scale(scale, scale, scale);
        }
    }
}
