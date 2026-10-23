package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.device.ArcaneBoreBlockEntity;
import thaumcraft.device.ArcaneBoreMenu;

public class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/gui_arcanebore.png");

    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 141;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private void sheet(GuiGraphics graphics, int x, int y, int width, int height, int u, int v) {
        GuiBlit.blit(graphics, TEXTURE, x, y, width, height, u, v, width, height, 256, 256, 0xFFFFFFFF);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        sheet(graphics, leftPos, topPos, imageWidth, imageHeight, 0, 0);
        ArcaneBoreBlockEntity bore = menu.bore();
        if (bore == null) {
            return;
        }
        ItemStack pickaxe = bore.getItem(ArcaneBoreBlockEntity.PICKAXE_SLOT);
        if (!pickaxe.isEmpty() && pickaxe.getDamageValue() + 1 >= pickaxe.getMaxDamage()) {
            sheet(graphics, leftPos + 74, topPos + 18, 16, 16, 184, 0);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 112, topPos + 8, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, "Width: " + (1 + (bore.getArea() + ArcaneBoreBlockEntity.MAX_RADIUS) * 2), 0, 0, 0xFFFFFFFF, true);
        graphics.drawString(font, "Speed: +" + bore.getSpeed(), 0, 10, 0xFFFFFFFF, true);
        graphics.drawString(font, "Other properties:", 0, 24, 0xFFFFFFFF, true);
        int base = 0;
        if (bore.hasDowsing()) {
            graphics.drawString(font, "Native Clusters", 4, 34 + base, 0xFF000000 | 12632256, true);
            base += 9;
        }
        if (bore.getFortune() > 0) {
            graphics.drawString(font, "Fortune " + bore.getFortune(), 4, 34 + base, 0xFF000000 | 15648330, true);
            base += 9;
        }
        if (bore.hasSilkTouch()) {
            graphics.drawString(font, "Silk Touch", 4, 34 + base, 0xFF000000 | 8421631, true);
        }
        graphics.pose().popPose();
    }
}
