package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.FireBat;

public class FireBatRenderer extends MobRenderer<FireBat, FireBatModel> {
    private static final ResourceLocation FIRE_BAT = Thaumcraft.id("textures/model/firebat.png");
    private static final ResourceLocation VAMPIRE_BAT = Thaumcraft.id("textures/model/vampirebat.png");

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel(context.bakeLayer(FireBatModel.LAYER)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(FireBat entity) {
        return entity.isVampire() ? VAMPIRE_BAT : FIRE_BAT;
    }

    @Override
    protected void scale(FireBat entity, PoseStack poseStack, float partialTick) {
        float scale = (entity.isDevil() || entity.isVampire()) ? 0.6F : 0.35F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    protected void setupRotations(FireBat entity, PoseStack poseStack, float ageInTicks, float bodyRot, float partialTick, float entityScale) {
        if (entity.isHanging()) {
            poseStack.translate(0.0F, -0.1F, 0.0F);
        } else {
            poseStack.translate(0.0F, Mth.cos(ageInTicks * 0.3F) * 0.1F, 0.0F);
        }
        super.setupRotations(entity, poseStack, ageInTicks, bodyRot, partialTick, entityScale);
    }
}
