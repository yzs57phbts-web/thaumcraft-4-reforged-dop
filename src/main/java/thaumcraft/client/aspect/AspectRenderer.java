package thaumcraft.client.aspect;

import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.platform.GuiBlit;

public final class AspectRenderer {
    public static final ResourceLocation BACK = Thaumcraft.id("textures/aspects/_back.png");
    public static final ResourceLocation UNKNOWN = Thaumcraft.id("textures/aspects/_unknown.png");
    public static final ResourceLocation PARTICLES = Thaumcraft.id("textures/misc/particles.png");

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#######.##");

    private AspectRenderer() {
    }

    public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect, float amount) {
        drawTag(graphics, x, y, aspect, amount, 0, 1.0F, false);
    }

    public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect) {
        drawTag(graphics, x, y, aspect, 0.0F, 0, 1.0F, true);
    }

    public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect, float amount, int bonus, float alpha, boolean blackAndWhite) {
        if (!aspect.isEnabled()) return;
        int color;
        if (blackAndWhite) {
            color = FastColor.ARGB32.colorFromFloat(alpha * 0.8F, 0.1F, 0.1F, 0.1F);
        } else {
            color = FastColor.ARGB32.color(Math.round(alpha * 255.0F), aspect.getColor());
        }
        GuiBlit.blit(graphics, aspect.getImage(), x, y, 16, 16, 0, 0, 32, 32, 32, 32, color);
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        if (amount > 0.0F) {
            drawAmount(graphics, font, AMOUNT_FORMAT.format(amount), x, y);
        }
        if (bonus > 0 && minecraft.player != null) {
            int particleU = 16 * (minecraft.player.tickCount % 16);
            GuiBlit.blit(graphics, PARTICLES, x - 4, y - 4, 16, 16, particleU, 80, 16, 16, 256, 256, 0xFFFFFFFF);
            if (bonus > 1) {
                String text = String.valueOf(bonus);
                graphics.pose().pushPose();
                graphics.pose().scale(0.5F, 0.5F, 1.0F);
                drawOutlined(graphics, font, text, 8 - font.width(text) / 2 + x * 2, 15 - font.lineHeight + y * 2);
                graphics.pose().popPose();
            }
        }
    }

    public static void drawBack(GuiGraphics graphics, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x - 2, y - 2, 0.0F);
        graphics.pose().scale(1.25F, 1.25F, 1.0F);
        GuiBlit.blit(graphics, BACK, 0, 0, 16, 16, 0, 0, 64, 64, 64, 64, 0xFFFFFFFF);
        graphics.pose().popPose();
    }

    public static void drawUnknown(GuiGraphics graphics, int x, int y) {
        GuiBlit.blit(graphics, UNKNOWN, x, y, 16, 16, 0, 0, 32, 32, 32, 32, 0xFFFFFFFF);
    }

    private static void drawAmount(GuiGraphics graphics, Font font, String text, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        drawOutlined(graphics, font, text, 32 - font.width(text) + x * 2, 32 - font.lineHeight + y * 2);
        graphics.pose().popPose();
    }

    private static void drawOutlined(GuiGraphics graphics, Font font, String text, int x, int y) {
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                if ((offsetX == 0 || offsetY == 0) && (offsetX != 0 || offsetY != 0)) {
                    graphics.drawString(font, text, x + offsetX, y + offsetY, 0xFF000000, false);
                }
            }
        }
        graphics.drawString(font, text, x, y, 0xFFFFFFFF, false);
    }
}
