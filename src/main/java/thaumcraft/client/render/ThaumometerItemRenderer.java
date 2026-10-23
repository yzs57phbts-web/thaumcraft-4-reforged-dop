package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.TcRenderTypes.Kind;
import thaumcraft.client.render.model.ObjModel;

public final class ThaumometerItemRenderer {
    private static final ResourceLocation MODEL = Thaumcraft.id("models/obj/scanner.obj");
    private static final ResourceLocation SCANNER = Thaumcraft.id("textures/model/scanner.png");
    private static final ResourceLocation SCREEN = Thaumcraft.id("textures/model/scanscreen.png");
    private static final int WHITE = 0xFFFFFFFF;

    private ThaumometerItemRenderer() {
    }

    private static void transform(PoseStack poseStack, LegacyItemView view) {
        if (view == LegacyItemView.EQUIPPED) {
            poseStack.translate(0.5F, 0.5F, 0.2F);
            poseStack.scale(0.25F, 0.25F, 0.25F);
            return;
        }
        view.apply(poseStack);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        switch (view) {
            case INVENTORY -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(60.0F));
                poseStack.mulPose(Axis.ZN.rotationDegrees(30.0F));
                poseStack.mulPose(Axis.YN.rotationDegrees(248.0F));
            }
            default -> {
            }
        }
    }

    public static int screenLight(int offset) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return WorldRendering.emissiveLight(200.0F);
        }
        int random = player.level().getRandom().nextInt(2);
        return WorldRendering.emissiveLight((int) (190.0F + Mth.sin(offset + player.tickCount - random) * 10.0F + 10.0F));
    }

    public static void submitModel(MultiBufferSource buffers, PoseStack poseStack, int light) {
        ObjModel model = ObjModel.get(MODEL);
        model.renderPart("scanner", poseStack.last(), buffers.getBuffer(TcRenderTypes.of(Kind.ENTITY_CUTOUT, SCANNER)), WHITE, light);
    }

    public static void beginScreen(MultiBufferSource buffers, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.11F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.scale(2.5F, 2.5F, 2.5F);
        int light = screenLight(0);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(TcRenderTypes.translucent(SCREEN));
        screenVertex(buffer, pose, -0.5F, 0.5F, 0.0F, 1.0F, light);
        screenVertex(buffer, pose, 0.5F, 0.5F, 1.0F, 1.0F, light);
        screenVertex(buffer, pose, 0.5F, -0.5F, 1.0F, 0.0F, light);
        screenVertex(buffer, pose, -0.5F, -0.5F, 0.0F, 0.0F, light);
    }

    private static void screenVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(WHITE).setUv(u, v).setLight(light);
    }

    public static void endScreen(PoseStack poseStack) {
        poseStack.popPose();
    }

    public static void render(LegacyItemView view, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (view == LegacyItemView.FIRST_PERSON) {
            return;
        }
        poseStack.pushPose();
        transform(poseStack, view);
        submitModel(buffers, poseStack, light);
        beginScreen(buffers, poseStack);
        endScreen(poseStack);
        poseStack.popPose();
    }
}
