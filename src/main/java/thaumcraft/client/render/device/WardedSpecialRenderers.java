package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import thaumcraft.device.WardedKeys;

public final class WardedSpecialRenderers {
    private WardedSpecialRenderers() {
    }

    private static void inventoryTurn(PoseStack poseStack) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
    }

    public static void hungryChest(MultiBufferSource buffers, PoseStack poseStack, int light) {
        poseStack.pushPose();
        inventoryTurn(poseStack);
        WardedRendering.hungryChest(buffers, poseStack, light, Direction.SOUTH, 0.0F);
        poseStack.popPose();
    }

    public static void brainJar(MultiBufferSource buffers, PoseStack poseStack, int light) {
        poseStack.pushPose();
        inventoryTurn(poseStack);
        WardedRendering.brainJar(buffers, poseStack, light, 0.0F, WardedRendering.viewerTicks());
        poseStack.popPose();
    }

    public static void banner(ItemStack stack, MultiBufferSource buffers, PoseStack poseStack, int light) {
        Integer color = WardedKeys.BANNER_COLOR.get(stack);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(-0.5F, -1.0F, -0.5F);
        WardedRendering.banner(buffers, poseStack, light, color == null ? -1 : color, WardedKeys.BANNER_ASPECT.get(stack), false, 0, false, WardedRendering.bannerSwing(0, 0, 0, 0L, 0.0F));
        poseStack.popPose();
    }
}
