package thaumcraft.client.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import thaumcraft.Thaumcraft;

public final class WarpVignetteOverlay {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/misc/vignette.png");

    private WarpVignetteOverlay() {
    }

    public static void draw(RenderGuiLayerEvent.Post event, int strength) {
        var graphics = event.getGuiGraphics();
        float brightness = Math.clamp(strength, 0, 255) / 255.0F;
        graphics.flush();
        float[] color = RenderSystem.getShaderColor().clone();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        try {
            RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0F);
            graphics.blit(TEXTURE, 0, 0, graphics.guiWidth(), graphics.guiHeight(),
                0.0F, 0.0F, 256, 256, 256, 256);
        } finally {
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
        }
    }
}
