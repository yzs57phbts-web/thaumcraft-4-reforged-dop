package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class ImmediateGeometry {
    public final MultiBufferSource buffers;

    public ImmediateGeometry(MultiBufferSource buffers) {
        this.buffers = buffers;
    }

    public interface Geometry {
        void draw(PoseStack.Pose pose, VertexConsumer buffer);
    }

    public ImmediateGeometry order(int order) {
        return this;
    }

    public void submitCustomGeometry(PoseStack poses, RenderType type, Geometry geometry) {
        geometry.draw(poses.last(), buffers.getBuffer(type));
    }

    public void submitModelPart(ModelPart model, PoseStack poses, RenderType type, int light, int overlay, Object sprite) {
        model.render(poses, buffers.getBuffer(type), light, overlay);
    }

    public void submitModel(net.minecraft.client.model.Model model, Object state, PoseStack poses, RenderType type, int light, int overlay, int color, Object sprite, int outline) {
        model.renderToBuffer(poses, buffers.getBuffer(type), light, overlay, color);
    }
}
