package thaumcraft.client.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class BrowserDraw {
    private static final ResourceLocation TOAST_BACKGROUND = ResourceLocation.withDefaultNamespace("toast/advancement");

    private BrowserDraw() {
    }

    public static void full(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int argb) {
        GuiBlit.blit(graphics, texture, x, y, width, height, 0, 0, 1, 1, 1, 1, argb);
    }

    public static void reversed(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int u, int v, int textureWidth, int textureHeight) {
        GuiBlit.blit(graphics, texture, x, y, width, height, u, v, -width, height, textureWidth, textureHeight, 0xFFFFFFFF);
    }

    public static void toastBackground(GuiGraphics graphics, int width, int height) {
        graphics.blitSprite(TOAST_BACKGROUND, 0, 0, width, height);
    }
}
