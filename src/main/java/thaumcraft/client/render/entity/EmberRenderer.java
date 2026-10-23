package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.projectile.Ember;

public class EmberRenderer extends EntityRenderer<Ember> {
    public EmberRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(Ember entity) {
        return thaumcraft.client.fx.ParticleFx.PARTICLES;
    }

    @Override
    public void render(Ember entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int lightCoords) {
        int frame = (int) (8.0F * ((float) entity.tickCount / Math.max(1, entity.getDuration())));
        float u0 = (7 + frame) / 16.0F;
        float v0 = 0.5625F;
        Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(), TcRenderTypes.additive(ParticleFx.PARTICLES), u0, u0 + 0.0625F, v0, v0 + 0.0625F, 0.25F + ((float) entity.tickCount / Math.max(1, entity.getDuration())), 0xE6FFFFFF, 220);
        super.render(entity, entityYaw, partialTick, poseStack, buffers, lightCoords);
    }
}
