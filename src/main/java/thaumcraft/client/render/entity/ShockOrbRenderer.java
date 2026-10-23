package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;

public class ShockOrbRenderer<T extends Entity> extends EntityRenderer<T> {
    public ShockOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(T entity) {
        return thaumcraft.client.fx.ParticleFx.PARTICLES;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int lightCoords) {
        int age = (int) (entity.tickCount + partialTick);
        float u0 = (1 + age % 6) / 8.0F;
        float v0 = 0.875F;
        float bob = Mth.sin(age / 5.0F) * 0.2F + 0.2F;
        Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(), TcRenderTypes.additive(ParticleFx.PARTICLES), u0, u0 + 0.125F, v0, v0 + 0.125F, 1.0F + bob, 0xCCFFFFFF, 220);
        super.render(entity, entityYaw, partialTick, poseStack, buffers, lightCoords);
    }
}
