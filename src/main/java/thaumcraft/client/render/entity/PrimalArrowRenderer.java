package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.projectile.PrimalArrow;

public class PrimalArrowRenderer extends ArrowRenderer<PrimalArrow> {
    private static final ResourceLocation ARROW = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private static final ResourceLocation WISP = Thaumcraft.id("textures/misc/wisp.png");
    private static final int[] COLORS = {16777215, 16777086, 16727041, 37119, 40960, 15650047, 5592439};

    public PrimalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimalArrow entity) {
        return ARROW;
    }

    @Override
    public void render(PrimalArrow entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int lightCoords) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, lightCoords);
        int frame = (int) (entity.tickCount + partialTick) % 16;
        float u0 = frame % 4 / 4.0F;
        float v0 = frame / 4 / 4.0F;
        int color = FastColor.ARGB32.opaque(COLORS[Math.min(entity.getArrowType(), 5) + 1]);
        var renderType = entity.getArrowType() < 5 ? TcRenderTypes.additive(WISP) : TcRenderTypes.translucent(WISP);
        Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(), renderType, u0 + 0.249F, u0, v0 + 0.249F, v0, 1.0F, color, 0xF000F0);
    }
}
