package thaumcraft.client.render.entity.eldritch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.AlphaMobRenderer;
import thaumcraft.client.platform.ClientLayers;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.monster.eldritch.MindSpider;

public final class MindSpiderRenderer extends AlphaMobRenderer<MindSpider, SpiderModel<MindSpider>> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/taint_spider.png");

    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.0F);
        addLayer(ClientLayers.fullbrightEyes(this, Thaumcraft.id("textures/model/taint_spider_eyes.png")));
    }

    @Override
    public boolean shouldRender(MindSpider entity, Frustum frustum, double camX, double camY, double camZ) {
        if (!super.shouldRender(entity, frustum, camX, camY, camZ)) {
            return false;
        }
        String viewer = entity.getViewer();
        return viewer.isEmpty() || Minecraft.getInstance().player != null
            && viewer.equals(Minecraft.getInstance().player.getUUID().toString());
    }

    @Override
    public ResourceLocation getTextureLocation(MindSpider entity) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(MindSpider entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return TcRenderTypes.entityTranslucentNoDepth(TEXTURE);
    }

    @Override
    protected float getEntityAlpha(MindSpider entity) {
        return Math.min(0.1F, entity.tickCount / 100.0F);
    }

    @Override
    protected void scale(MindSpider entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.3F, 0.3F, 0.3F);
    }

    @Override
    protected float getFlipDegrees(MindSpider entity) {
        return 180.0F;
    }
}
