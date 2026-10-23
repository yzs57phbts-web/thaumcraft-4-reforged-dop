package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.FastColor;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.AspectOrb;

public final class AspectOrbRenderer extends EntityRenderer<AspectOrb> {
    public AspectOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(AspectOrb entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        int frame = (int) (System.nanoTime() / 25_000_000L % 16L);
        float u0 = frame / 16.0F;
        float u1 = (frame + 1.0F) / 16.0F;
        float scale = 0.1F + 0.3F * Math.max(0.0F, (150.0F - entity.getAge()) / 150.0F);
        int tint = FastColor.ARGB32.color(128, entity.getAspect().getColor());
        Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(),
            TcRenderTypes.byBlend(ParticleFx.PARTICLES, entity.getAspect().getBlend()), u0, u1, 0.5F, 0.5625F, scale, tint, 0xF000F0);
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(AspectOrb entity) {
        return ParticleFx.PARTICLES;
    }
}
