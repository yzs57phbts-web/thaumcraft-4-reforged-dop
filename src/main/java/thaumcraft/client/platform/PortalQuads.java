package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import thaumcraft.client.platform.ItemGeometry;
import net.minecraft.client.renderer.RenderType;

public final class PortalQuads {
    private PortalQuads() {
    }

    public static void submit(MultiBufferSource collector, PoseStack poseStack, float low, float high, float z) {
        ItemGeometry.submit(collector, poseStack, RenderType.endPortal(), (pose, buffer) -> {
            buffer.addVertex(pose.pose(), low, low, z);
            buffer.addVertex(pose.pose(), low, high, z);
            buffer.addVertex(pose.pose(), high, high, z);
            buffer.addVertex(pose.pose(), high, low, z);
        });
    }
}
