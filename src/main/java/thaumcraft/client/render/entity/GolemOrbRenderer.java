package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.projectile.GolemOrb;

public final class GolemOrbRenderer extends EntityRenderer<GolemOrb> {
    public GolemOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(GolemOrb orb, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        int age = orb.tickCount;
        float u0 = (1 + age % 6) / 8.0F;
        float v0 = orb.isRed() ? 0.75F : 0.875F;
        float size = 1.0F + Mth.sin(age / 5.0F) * 0.2F + 0.2F;
        Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(),
            TcRenderTypes.additive(ParticleFx.PARTICLES), u0, u0 + 0.125F, v0, v0 + 0.125F, size, 0xCCFFFFFF, 220);
        super.render(orb, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(GolemOrb orb) {
        return ParticleFx.PARTICLES;
    }
}
