package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public final class MachineSpecialRenderers {
    private MachineSpecialRenderers() {
    }

    public static void boreBase(MultiBufferSource buffers, PoseStack poseStack, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        BoreRendering.boreBase(buffers, poseStack, light, Direction.NORTH);
        poseStack.popPose();
    }

    public static void bore(MultiBufferSource buffers, PoseStack poseStack, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(-0.5F, -0.75F, -0.5F);
        Minecraft minecraft = Minecraft.getInstance();
        int ticks = minecraft.getCameraEntity() == null ? 0 : minecraft.getCameraEntity().tickCount;
        BoreRendering.bore(buffers, poseStack, light, new BoreRendering.BoreState(0.0F, 0.0F, false, 0, false, ticks % 45));
        poseStack.popPose();
    }
}
