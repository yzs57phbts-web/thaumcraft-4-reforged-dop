package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public final class NodeDeviceSpecialRenderers {
    private NodeDeviceSpecialRenderers() {
    }

    private static int viewerTicks() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? 0 : minecraft.player.tickCount;
    }

    public static void stabilizer(PoseStack poseStack, MultiBufferSource buffers, int light, boolean advanced) {
        NodeDeviceRendering.stabilizer(buffers, poseStack, light, advanced ? 2 : 1, 0, viewerTicks(), false);
    }

    public static void transducer(PoseStack poseStack, MultiBufferSource buffers, int light) {
        NodeDeviceRendering.transducer(buffers, poseStack, light, 0, 0, viewerTicks(), false);
    }

    public static void relay(PoseStack poseStack, MultiBufferSource buffers, int light) {
        NodeDeviceRendering.relay(buffers, poseStack, light, Direction.UP, -1, true, viewerTicks());
    }
}
