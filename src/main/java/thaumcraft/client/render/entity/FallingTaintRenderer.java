package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.RenderShape;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.entity.monster.taint.FallingTaint;

public final class FallingTaintRenderer extends EntityRenderer<FallingTaint> {
    private final BlockRenderDispatcher blockRenderer;

    public FallingTaintRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public boolean shouldRender(FallingTaint entity, Frustum frustum, double camX, double camY, double camZ) {
        return super.shouldRender(entity, frustum, camX, camY, camZ)
            && entity.getBlockState() != entity.level().getBlockState(entity.blockPosition());
    }

    @Override
    public void render(FallingTaint entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        if (entity.getBlockState().getRenderShape() == RenderShape.MODEL) {
            poseStack.pushPose();
            poseStack.translate(-0.5, 0.0, -0.5);
            blockRenderer.renderSingleBlock(entity.getBlockState(), poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingTaint entity) {
        return ParticleFx.PARTICLES;
    }
}
