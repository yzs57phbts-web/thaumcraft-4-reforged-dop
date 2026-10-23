package thaumcraft.client.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public final class GuiBlit {
    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        blit(graphics, texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight, -1);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int color) {
        blit(graphics, texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight, color);
    }

    public static void sparkle(GuiGraphics graphics, ResourceLocation texture, int x, int y, int frame, int color) {
        graphics.flush();
        float[] previous = RenderSystem.getShaderColor().clone();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        try {
            RenderSystem.setShaderColor(FastColor.ARGB32.red(color) / 255.0F, FastColor.ARGB32.green(color) / 255.0F,
                FastColor.ARGB32.blue(color) / 255.0F, FastColor.ARGB32.alpha(color) / 255.0F);
            graphics.blit(texture, x - 4, y - 4, 8, 8, frame * 16.0F, 112.0F, 16, 16, 256, 256);
        } finally {
            RenderSystem.setShaderColor(previous[0], previous[1], previous[2], previous[3]);
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
        }
    }

    private GuiBlit() {
    }

    public static void blit(
        GuiGraphics graphics,
        ResourceLocation texture,
        int x,
        int y,
        int width,
        int height,
        int u,
        int v,
        int regionWidth,
        int regionHeight,
        int textureWidth,
        int textureHeight,
        int argb
    ) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(
            FastColor.ARGB32.red(argb) / 255.0F,
            FastColor.ARGB32.green(argb) / 255.0F,
            FastColor.ARGB32.blue(argb) / 255.0F,
            FastColor.ARGB32.alpha(argb) / 255.0F
        );
        graphics.blit(texture, x, y, width, height, (float) u, (float) v, regionWidth, regionHeight, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
