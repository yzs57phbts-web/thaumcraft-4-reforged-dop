package thaumcraft.client.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;

public final class GuiSprites {
    private GuiSprites() {
    }

    public static void blit(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int width, int height, int argb) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(FastColor.ARGB32.red(argb) / 255.0F, FastColor.ARGB32.green(argb) / 255.0F, FastColor.ARGB32.blue(argb) / 255.0F, FastColor.ARGB32.alpha(argb) / 255.0F);
        graphics.blit(x, y, 0, width, height, sprite);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
