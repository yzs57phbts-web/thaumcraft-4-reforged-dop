package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.projectile.ExplosiveOrb;

public class ExplosiveOrbRenderer extends EntityRenderer<ExplosiveOrb> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/misc/particles2.png");

    public ExplosiveOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(ExplosiveOrb entity) {
        return thaumcraft.client.fx.ParticleFx.PARTICLES;
    }

    @Override
    public void render(ExplosiveOrb entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int lightCoords) {
        float u0 = (int) (entity.tickCount + partialTick) % 4 / 16.0F;
        float v0 = 0.8125F;
        Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(), TcRenderTypes.translucent(TEXTURE), u0, u0 + 0.0625F, v0, v0 + 0.0625F, 0.7F, 0xCCFFFFFF, 220);
        super.render(entity, entityYaw, partialTick, poseStack, buffers, lightCoords);
    }
}
