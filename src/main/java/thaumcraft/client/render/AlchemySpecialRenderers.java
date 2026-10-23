package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class AlchemySpecialRenderers {
    private AlchemySpecialRenderers() {
    }

    public static void alembic(PoseStack poseStack, MultiBufferSource buffers, int light) {
        AlchemyRendering.alembic(buffers, poseStack, light, false, Direction.NORTH, false, false, null, new boolean[6]);
    }

    public static void fluxScrubber(PoseStack poseStack, MultiBufferSource buffers, int light) {
        AlchemyRendering.fluxScrubber(buffers, poseStack, light, Direction.DOWN, EssentiaRendering.viewerTicks());
    }

    public static void bellows(PoseStack poseStack, MultiBufferSource buffers, int light) {
        AlchemyRendering.bellows(buffers, poseStack, light, Direction.NORTH, Mth.sin(EssentiaRendering.viewerTicks() / 8.0F) * 0.3F + 0.7F);
    }
}
