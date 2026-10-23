package thaumcraft.client.research;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import thaumcraft.client.platform.BrowserDraw;
import thaumcraft.research.ResearchItem;

public class ResearchToast implements Toast {
    private static final String TITLE = "Research Completed!";
    private static final long DISPLAY_TIME = 3000L;
    private static final float MAX_NAME_WIDTH = 125.0F;

    private final ResearchItem research;
    private final ItemStack icon;
    private final Component name;

    public ResearchToast(ResearchItem research) {
        this.research = research;
        this.icon = research.iconItem() == null ? ItemStack.EMPTY : research.iconItem().create();
        this.name = Component.translatable(research.nameKey());
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent manager, long fullyVisibleForMs) {
        Font font = manager.getMinecraft().font;
        BrowserDraw.toastBackground(graphics, width(), height());
        graphics.drawString(font, TITLE, 30, 7, 0xFFFFFF00, false);
        int nameWidth = font.width(name);
        if (nameWidth <= MAX_NAME_WIDTH) {
            graphics.drawString(font, name, 30, 18, 0xFFFFFFFF, false);
        } else {
            float scale = MAX_NAME_WIDTH / nameWidth;
            graphics.pose().pushPose();
            graphics.pose().translate(30.0F, 16.0F + 2.0F / scale, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, name, 0, 0, 0xFFFFFFFF, false);
            graphics.pose().popPose();
        }
        if (!icon.isEmpty()) {
            graphics.renderFakeItem(icon, 8, 8);
        } else {
            ResearchIcons.drawFallback(graphics, research, 8, 8, 0xFFFFFFFF);
        }
        return fullyVisibleForMs >= DISPLAY_TIME ? Visibility.HIDE : Visibility.SHOW;
    }
}
