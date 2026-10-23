package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import thaumcraft.client.geom.QuadSink;

public final class VertexQuadSink implements QuadSink {
    private final PoseStack.Pose pose;
    private final VertexConsumer buffer;

    public VertexQuadSink(PoseStack.Pose pose, VertexConsumer buffer) {
        this.pose = pose;
        this.buffer = buffer;
    }

    @Override
    public void vertex(float x, float y, float z, float u, float v, int argb, int light) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(argb).setLight(light);
    }
}
