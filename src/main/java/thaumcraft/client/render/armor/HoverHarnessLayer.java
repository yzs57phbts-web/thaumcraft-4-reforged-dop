package thaumcraft.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.ItemGeometry;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.render.model.ObjModel;
import thaumcraft.item.armor.Hover;
import thaumcraft.item.armor.HoverHarnessItem;

public class HoverHarnessLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation MODEL = Thaumcraft.id("models/obj/hoverharness.obj");
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/hoverharness2.png");
    private static final ResourceLocation RING = Thaumcraft.id("item/lightningring");
    private static final int RING_LIGHT = 230;

    public HoverHarnessLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int lightCoords, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float yRot, float xRot) {
        if (!(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof HoverHarnessItem)) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(0.1F, 0.1F, 0.1F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        if (player.isCrouching()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(28.64789F));
        }
        poseStack.translate(0.0F, 0.33F, -3.7F);
        ObjModel.get(MODEL).renderAll(poseStack.last(), buffers.getBuffer(TcRenderTypes.entityCutout(TEXTURE)), 0xFFFFFFFF, lightCoords);
        poseStack.popPose();
        if (!Hover.isHovering(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST))) {
            return;
        }
        TextureAtlasSprite ring = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RING);
        poseStack.pushPose();
        if (player.isCrouching()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(28.64789F));
            poseStack.translate(0.0F, 0.075F, -0.05F);
        }
        poseStack.translate(0.0F, 0.2F, 0.55F);
        poseStack.pushPose();
        poseStack.scale(2.5F, 2.5F, 2.5F);
        ItemGeometry.blended(buffers, poseStack, ring.atlasLocation(), 1, sink -> quad(sink, ring, 0xFFFFFFFF));
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, 0.0F, 0.03F);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        ItemGeometry.blended(buffers, poseStack, ring.atlasLocation(), 1, sink -> quad(sink, ring, 0xFFFF80FF));
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void quad(QuadSink sink, TextureAtlasSprite sprite, int argb) {
        sink.vertex(-0.5F, 0.5F, 0.0F, sprite.getU1(), sprite.getV1(), argb, RING_LIGHT);
        sink.vertex(0.5F, 0.5F, 0.0F, sprite.getU0(), sprite.getV1(), argb, RING_LIGHT);
        sink.vertex(0.5F, -0.5F, 0.0F, sprite.getU0(), sprite.getV0(), argb, RING_LIGHT);
        sink.vertex(-0.5F, -0.5F, 0.0F, sprite.getU1(), sprite.getV0(), argb, RING_LIGHT);
    }
}
