package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.geom.QuadSink;

public final class ItemGeometry {
    private ItemGeometry() {
    }

    public static void submit(MultiBufferSource buffers, PoseStack poseStack, RenderType renderType, BiConsumer<PoseStack.Pose, VertexConsumer> geometry) {
        geometry.accept(poseStack.last(), buffers.getBuffer(renderType));
    }

    public static void solid(MultiBufferSource buffers, PoseStack poseStack, ResourceLocation texture, boolean translucent, Consumer<QuadSink> geometry) {
        RenderType renderType = translucent ? RenderType.entityTranslucent(texture) : RenderType.entityCutout(texture);
        submit(buffers, poseStack, renderType, (pose, buffer) -> geometry.accept(new EntityQuadSink(pose, buffer)));
    }

    public static void blended(MultiBufferSource buffers, PoseStack poseStack, ResourceLocation texture, int blendMode, Consumer<QuadSink> geometry) {
        submit(buffers, poseStack, TcRenderTypes.byBlend(texture, blendMode), (pose, buffer) -> geometry.accept(new VertexQuadSink(pose, buffer)));
    }
}
