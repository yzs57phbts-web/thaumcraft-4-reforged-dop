package thaumcraft.client.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

public final class GuiLines {
    private static final float LINE_WIDTH = 3.0F;

    private GuiLines() {
    }

    public static void strip(GuiGraphics graphics, float[] xs, float[] ys, int[] colors, boolean additive) {
        if (xs.length < 2) {
            return;
        }
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        float inner = (LINE_WIDTH / 2.0F - 0.5F) / scale;
        float outer = (LINE_WIDTH / 2.0F + 0.5F) / scale;
        graphics.flush();
        Matrix4f pose = graphics.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.disableCull();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        build(buffer, pose, xs, ys, colors, inner, outer);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }

    private static void build(BufferBuilder consumer, Matrix4f pose, float[] xs, float[] ys, int[] colors, float inner, float outer) {
        int count = xs.length;
        float[] normalX = new float[count];
        float[] normalY = new float[count];
        for (int index = 0; index < count; index++) {
            float directionX = 0.0F;
            float directionY = 0.0F;
            if (index > 0) {
                float[] segment = direction(xs, ys, index - 1, index);
                directionX += segment[0];
                directionY += segment[1];
            }
            if (index < count - 1) {
                float[] segment = direction(xs, ys, index, index + 1);
                directionX += segment[0];
                directionY += segment[1];
            }
            float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);
            if (length > 0.0F) {
                normalX[index] = -directionY / length;
                normalY[index] = directionX / length;
            }
        }
        for (int index = 0; index < count - 1; index++) {
            int next = index + 1;
            int startColor = colors[index];
            int endColor = colors[next];
            int startClear = FastColor.ARGB32.color(0, startColor);
            int endClear = FastColor.ARGB32.color(0, endColor);
            band(consumer, pose, xs, ys, index, next, outer, inner, startClear, endClear, startColor, endColor, normalX, normalY);
            band(consumer, pose, xs, ys, index, next, inner, -inner, startColor, endColor, startColor, endColor, normalX, normalY);
            band(consumer, pose, xs, ys, index, next, -inner, -outer, startColor, endColor, startClear, endClear, normalX, normalY);
        }
    }

    private static float[] direction(float[] xs, float[] ys, int from, int to) {
        float deltaX = xs[to] - xs[from];
        float deltaY = ys[to] - ys[from];
        float length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return length > 0.0F ? new float[] {deltaX / length, deltaY / length} : new float[] {0.0F, 0.0F};
    }

    private static void band(
        BufferBuilder consumer,
        Matrix4f pose,
        float[] xs,
        float[] ys,
        int start,
        int end,
        float firstOffset,
        float secondOffset,
        int startFirst,
        int endFirst,
        int startSecond,
        int endSecond,
        float[] normalX,
        float[] normalY
    ) {
        consumer.addVertex(pose, xs[start] + normalX[start] * secondOffset, ys[start] + normalY[start] * secondOffset, 0.0F).setColor(startSecond);
        consumer.addVertex(pose, xs[start] + normalX[start] * firstOffset, ys[start] + normalY[start] * firstOffset, 0.0F).setColor(startFirst);
        consumer.addVertex(pose, xs[end] + normalX[end] * firstOffset, ys[end] + normalY[end] * firstOffset, 0.0F).setColor(endFirst);
        consumer.addVertex(pose, xs[end] + normalX[end] * secondOffset, ys[end] + normalY[end] * secondOffset, 0.0F).setColor(endSecond);
    }
}
