package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.monster.Wisp;

public final class WispRenderer extends EntityRenderer<Wisp> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/misc/wisp.png");
    private static final ResourceLocation CORE_TEXTURE = Thaumcraft.id("textures/misc/particles.png");

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(Wisp entity) {
        return TEXTURE;
    }

    @Override
    public void render(Wisp entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        if (entity.getHealth() > 0.0F) {
            Aspect aspect = entity.getAspect();
            int color = aspect == null ? 0xFFFFFF : aspect.getColor();
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            if (entity.hurtTime > 0) {
                red = 255;
                green = green * 255 / 300;
                blue = blue * 255 / 300;
            }
            int frame = entity.tickCount & 15;
            float age = entity.tickCount + partialTick;
            float u0 = (frame % 4) / 4.0F;
            float u1 = (frame % 4 + 0.99F) / 4.0F;
            float v0 = (frame / 4) / 4.0F;
            float v1 = (frame / 4 + 0.99F) / 4.0F;
            int tint = 0xFF000000 | red << 16 | green << 8 | blue;
            poseStack.pushPose();
            Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(),
                TcRenderTypes.additive(TEXTURE), u0, u1, v0, v1, 1.0F, tint, 0xF000F0);
            float size = 0.4F + (float) Math.sin(age / 10.0F) * 0.1F;
            Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(),
                TcRenderTypes.additive(CORE_TEXTURE), (frame + 8) / 16.0F, (frame + 9) / 16.0F, 5.0F / 16.0F, 6.0F / 16.0F,
                size, 0xFFFFFFFF, 0xF000F0);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }
}
