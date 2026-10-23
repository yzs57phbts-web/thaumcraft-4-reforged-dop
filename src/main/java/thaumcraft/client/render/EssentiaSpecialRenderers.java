package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.essentia.EssentiaKeys;

public final class EssentiaSpecialRenderers {
    private static final int JAR_MAX_AMOUNT = 64;

    private EssentiaSpecialRenderers() {
    }

    public static void filledJar(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int light, boolean voidJar) {
        AspectList aspects = AspectKeys.ASPECTS.get(stack);
        Aspect aspect = null;
        int amount = 0;
        if (aspects != null && aspects.size() == 1) {
            aspect = aspects.getAspects().get(0);
            amount = aspects.getAmount(aspect);
        }
        @Nullable Aspect filter = EssentiaKeys.FILTER.get(stack);
        EssentiaJarRendering.submit(buffers, poseStack, light, 0, aspect, amount, JAR_MAX_AMOUNT, filter, Direction.EAST);
        EssentiaJarRendering.glass(buffers, poseStack, light, voidJar);
    }

    public static void centrifuge(PoseStack poseStack, MultiBufferSource buffers, int light) {
        EssentiaRendering.centrifuge(buffers, poseStack, 0.0F, light);
    }

    public static void crystallizer(PoseStack poseStack, MultiBufferSource buffers, int light) {
        EssentiaRendering.crystallizer(buffers, poseStack, Direction.DOWN, 0.0F, 1.0F, 1.0F, 1.0F, EssentiaRendering.viewerTicks(), light);
    }

    public static void reservoir(PoseStack poseStack, MultiBufferSource buffers, int light) {
        EssentiaRendering.reservoir(buffers, poseStack, Direction.DOWN, light);
    }

    public static void tubeValve(PoseStack poseStack, MultiBufferSource buffers, int light) {
        EssentiaRendering.valve(buffers, poseStack, Direction.EAST, 0.0F, light);
    }
}
