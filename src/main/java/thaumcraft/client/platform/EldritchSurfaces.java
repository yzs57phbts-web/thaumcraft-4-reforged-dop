package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import thaumcraft.client.platform.ImmediateGeometry;
import net.minecraft.client.renderer.RenderType;
import thaumcraft.client.geom.EldritchSurfaceGeometry;

public final class EldritchSurfaces {
    private EldritchSurfaces() {
    }

    public static void submitPortalFaces(ImmediateGeometry collector, PoseStack poseStack, int mask, boolean nearby, float time) {
        if (mask == 0) {
            return;
        }
        var tunnel = thaumcraft.Thaumcraft.id("textures/misc/tunnel.png");
        var far = thaumcraft.Thaumcraft.id("textures/misc/particlefield32.png");
        var field = thaumcraft.Thaumcraft.id("textures/misc/particlefield.png");
        collector.submitCustomGeometry(poseStack, TcRenderTypes.eldritchSurface(nearby ? tunnel : far), (pose, buffer) ->
            EldritchSurfaceGeometry.faces(new EntityQuadSink(pose, buffer), mask, nearby ? 0.125F : 1.0F, 0, nearby ? 0xFF191919 : 0xFF808080, 0xF000F0));
        if (nearby) {
            collector.order(1).submitCustomGeometry(poseStack, TcRenderTypes.additive(field), (pose, buffer) -> {
                EntityQuadSink sink = new EntityQuadSink(pose, buffer);
                for (int layer = 1; layer < 16; layer++) {
                    EldritchSurfaceGeometry.faces(sink, mask, 0.5F, EldritchSurfaceGeometry.rotationForLayer(layer, time), EldritchSurfaceGeometry.colorForLayer(layer), 0xF000F0);
                }
            });
        }
    }

    public static void submitPortalRectangle(ImmediateGeometry collector, PoseStack poseStack, boolean nearby, float time, float x0, float y0, float z0, float x1, float y1, float z1) {
        var tunnel = thaumcraft.Thaumcraft.id("textures/misc/tunnel.png");
        var far = thaumcraft.Thaumcraft.id("textures/misc/particlefield32.png");
        var field = thaumcraft.Thaumcraft.id("textures/misc/particlefield.png");
        submitRectangle(collector, poseStack, TcRenderTypes.eldritchSurface(nearby ? tunnel : far), x0, y0, z0, x1, y1, z1, nearby ? 0.125F : 1.0F, 0, nearby ? 0xFF191919 : 0xFF808080, 0xF000F0);
        if (nearby) {
            collector.order(1).submitCustomGeometry(poseStack, TcRenderTypes.additive(field), (pose, buffer) -> {
                EntityQuadSink sink = new EntityQuadSink(pose, buffer);
                for (int layer = 1; layer < 16; layer++) {
                    EldritchSurfaceGeometry.rectangle(sink, x0, y0, z0, x1, y1, z1, 0.5F, EldritchSurfaceGeometry.rotationForLayer(layer, time), EldritchSurfaceGeometry.colorForLayer(layer), 0xF000F0);
                }
            });
        }
    }

    public static void submitFace(ImmediateGeometry collector, PoseStack poseStack, RenderType renderType, int side, float uvScale, float rotation, int color, int light) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> EldritchSurfaceGeometry.cubeFace(new EntityQuadSink(pose, buffer), side, uvScale, rotation, color, light));
    }

    public static void submitRectangle(ImmediateGeometry collector, PoseStack poseStack, RenderType renderType, float x0, float y0, float z0, float x1, float y1, float z1, float uvScale, float rotation, int color, int light) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> EldritchSurfaceGeometry.rectangle(new EntityQuadSink(pose, buffer), x0, y0, z0, x1, y1, z1, uvScale, rotation, color, light));
    }
}
