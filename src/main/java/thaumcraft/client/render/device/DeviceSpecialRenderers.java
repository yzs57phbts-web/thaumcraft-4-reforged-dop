package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import thaumcraft.device.CrystalClusterBlock;

public final class DeviceSpecialRenderers {
    private DeviceSpecialRenderers() {
    }

    public static void crystalCluster(MultiBufferSource buffers, PoseStack poseStack, int light, int kind) {
        int ticks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
        poseStack.pushPose();
        if (kind == CrystalClusterBlock.STRANGE) {
            DeviceRendering.strangeCrystal(buffers, poseStack, light, Direction.UP, 0, ticks);
        } else {
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.translate(-0.5F, 0.0F, -0.5F);
            DeviceRendering.crystalCluster(buffers, poseStack, kind, Direction.UP, kind, ticks);
        }
        poseStack.popPose();
    }
}
