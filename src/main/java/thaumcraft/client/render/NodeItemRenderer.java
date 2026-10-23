package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.node.NodeType;

public final class NodeItemRenderer {
    private static final AspectList ASPECTS = new AspectList().add(Aspect.AIR, 40).add(Aspect.FIRE, 40).add(Aspect.EARTH, 40).add(Aspect.WATER, 40);

    private NodeItemRenderer() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffers) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(2.0F, 2.0F, 2.0F);
        NodeRendering.submitCross(buffers, poseStack, ASPECTS, NodeType.NORMAL, null);
        poseStack.popPose();
    }
}
