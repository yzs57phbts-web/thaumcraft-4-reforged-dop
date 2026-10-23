package thaumcraft.client.render.entity.eldritch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.boss.EldritchGolem;

public final class EldritchGolemRenderer extends MobRenderer<EldritchGolem, EldritchGolemModel> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/eldritch_golem.png");

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGolemModel(context.bakeLayer(EldritchGolemModel.LAYER)), 0.5F);
    }

    @Override
    public void render(EldritchGolem entity, float yaw, float partialTicks, PoseStack poses, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        var state = createRenderState();
        extractRenderState(entity, state, partialTicks);
        getModel().state = state;
        super.render(entity, yaw, partialTicks, poses, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchGolem entity) {
        return getTextureLocation(getModel().state);
    }

    public EldritchGolemRenderState createRenderState() {
        return new EldritchGolemRenderState();
    }

    public void extractRenderState(EldritchGolem entity, EldritchGolemRenderState state, float partialTicks) {
        thaumcraft.client.platform.LivingEntityRenderState.extract(entity, state, partialTicks);
        state.headless = entity.isHeadless();
        state.attackTimer = entity.getAttackTimer();
        state.spawnTimer = entity.getSpawnTimer();
        state.partialTick = partialTicks;
    }

    public ResourceLocation getTextureLocation(EldritchGolemRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(EldritchGolem entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(2.15F, 2.15F, 2.15F);
    }
}
