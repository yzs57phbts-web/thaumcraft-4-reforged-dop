package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class Billboards {
    public static void submit(PoseStack poses, ImmediateGeometry geometry, CameraRenderState camera, RenderType type, float u0, float u1, float v0, float v1, float scale, int argb, int light) {
        submit(poses, geometry.buffers, net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera(), type, u0, u1, v0, v1, scale, argb, light);
    }

    private Billboards() {
    }

    public static void submit(PoseStack poseStack, MultiBufferSource buffers, Camera camera, RenderType renderType, float u0, float u1, float v0, float v1, float scale, int argb, int light) {
        poseStack.pushPose();
        poseStack.mulPose(camera.rotation());
        poseStack.scale(scale, scale, scale);
        EntityQuadSink sink = new EntityQuadSink(poseStack.last(), buffers.getBuffer(renderType));
        sink.vertex(-0.5F, -0.5F, 0.0F, u0, v1, argb, light);
        sink.vertex(0.5F, -0.5F, 0.0F, u1, v1, argb, light);
        sink.vertex(0.5F, 0.5F, 0.0F, u1, v0, argb, light);
        sink.vertex(-0.5F, 0.5F, 0.0F, u0, v0, argb, light);
        poseStack.popPose();
    }
}
