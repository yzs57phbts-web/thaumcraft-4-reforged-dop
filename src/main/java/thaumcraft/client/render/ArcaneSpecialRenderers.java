package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public final class ArcaneSpecialRenderers {
    private ArcaneSpecialRenderers() {
    }

    private static float viewerTicks() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? 0.0F : minecraft.player.tickCount;
    }

    public static void runicMatrix(PoseStack poseStack, MultiBufferSource buffers, int light) {
        RunicMatrixRendering.submit(buffers, poseStack, light, false, false, false, 0.0F, 0, 0, viewerTicks());
    }

    public static void chargeRelay(PoseStack poseStack, MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        poseStack.translate(-0.5F, -0.25F, -0.5F);
        NodeDeviceRendering.charger(buffers, poseStack, light, -1, false, viewerTicks());
        poseStack.popPose();
    }
}
