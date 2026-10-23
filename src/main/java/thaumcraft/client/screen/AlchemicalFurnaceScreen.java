package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.menu.AlchemicalFurnaceMenu;

public class AlchemicalFurnaceScreen extends AbstractContainerScreen<AlchemicalFurnaceMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/gui_alchemyfurnace.png");

    public AlchemicalFurnaceScreen(AlchemicalFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private void sheet(GuiGraphics graphics, int x, int y, int width, int height, int u, int v) {
        if (width > 0 && height > 0) {
            GuiBlit.blit(graphics, TEXTURE, x, y, width, height, u, v, width, height, 256, 256, 0xFFFFFFFF);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        sheet(graphics, leftPos, topPos, imageWidth, imageHeight, 0, 0);
        if (menu.getBurnTime() > 0) {
            int burn = menu.getBurnTimeRemainingScaled(20);
            sheet(graphics, leftPos + 80, topPos + 26 + 20 - burn, 16, burn, 176, 20 - burn);
        }
        int cook = menu.getCookProgressScaled(46);
        sheet(graphics, leftPos + 106, topPos + 13 + 46 - cook, 9, cook, 216, 46 - cook);
        int contents = menu.getContentsScaled(48);
        sheet(graphics, leftPos + 61, topPos + 12 + 48 - contents, 8, contents, 200, 48 - contents);
        sheet(graphics, leftPos + 60, topPos + 8, 10, 55, 232, 0);
    }
}
