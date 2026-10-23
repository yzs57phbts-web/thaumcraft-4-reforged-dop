package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.ThaumicSlime;

public final class ThaumicSlimeRenderer extends MobRenderer<ThaumicSlime, SlimeModel<ThaumicSlime>> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/tslime.png");

    public ThaumicSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
        addLayer(new ThaumicSlimeOuterLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(ThaumicSlime entity, PoseStack poseStack, float partialTick) {
        float size = Mth.sqrt(entity.getSlimeSize());
        float squish = Mth.lerp(partialTick, entity.prevSquishFactor, entity.squishFactor) / (size * 0.25F + 1.0F);
        float inverse = 1.0F / (squish + 1.0F);
        poseStack.scale(inverse * size + 0.1F, 1.0F / inverse * size + 0.1F, inverse * size + 0.1F);
    }

    @Override
    protected float getShadowRadius(ThaumicSlime entity) {
        return Mth.sqrt(entity.getSlimeSize()) * 0.25F;
    }

    @Override
    public ResourceLocation getTextureLocation(ThaumicSlime entity) {
        return TEXTURE;
    }
}
