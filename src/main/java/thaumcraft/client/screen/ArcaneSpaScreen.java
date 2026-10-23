package thaumcraft.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.client.platform.GuiSprites;
import thaumcraft.device.ArcaneSpaBlockEntity;
import thaumcraft.device.ArcaneSpaMenu;
import thaumcraft.registry.ModSounds;

public class ArcaneSpaScreen extends AbstractContainerScreen<ArcaneSpaMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/gui_spa.png");

    public ArcaneSpaScreen(ArcaneSpaMenu menu, Inventory inventory, Component title) {
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
        ArcaneSpaBlockEntity spa = menu.spa();
        if (spa == null) {
            return;
        }
        if (spa.getMix()) {
            sheet(graphics, leftPos + 89, topPos + 35, 8, 8, 208, 16);
        } else {
            sheet(graphics, leftPos + 89, topPos + 35, 8, 8, 208, 32);
        }
        FluidStack fluid = spa.getTank().getFluid();
        if (spa.getTank().getAmount() > 0 && !fluid.isEmpty()) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
            TextureAtlasSprite sprite = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(extensions.getStillTexture(fluid));
            int tint = extensions.getTintColor(fluid);
            int color = FastColor.ARGB32.opaque(tint);
            float bar = (float) spa.getTank().getAmount() / spa.getTank().getCapacity();
            for (int cell = 0; cell < 6; cell++) {
                GuiSprites.blit(graphics, sprite, leftPos + 107, topPos + 15 + cell * 8, 8, 8, color);
            }
            sheet(graphics, leftPos + 107, topPos + 15, 10, (int) (48.0F - 48.0F * bar), 107, 15);
        }
        sheet(graphics, leftPos + 106, topPos + 11, 10, 55, 232, 0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        ArcaneSpaBlockEntity spa = menu.spa();
        if (spa == null) {
            return;
        }
        int relativeX = mouseX - (leftPos + 104);
        int relativeY = mouseY - (topPos + 10);
        if (relativeX >= 0 && relativeY >= 0 && relativeX < 10 && relativeY < 55) {
            FluidStack fluid = spa.getTank().getFluid();
            if (!fluid.isEmpty()) {
                List<Component> lines = new ArrayList<>();
                lines.add(fluid.getHoverName());
                lines.add(Component.literal(fluid.getAmount() + " mb"));
                graphics.renderTooltip(font, lines, java.util.Optional.empty(), mouseX, mouseY);
            }
        }
        relativeX = mouseX - (leftPos + 88);
        relativeY = mouseY - (topPos + 34);
        if (relativeX >= 0 && relativeY >= 0 && relativeX < 10 && relativeY < 10) {
            graphics.renderTooltip(font, Component.translatable(spa.getMix() ? "text.spa.mix.true" : "text.spa.mix.false"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double eventX, double eventY, int button) {
        boolean handled = super.mouseClicked(eventX, eventY, button);
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            double relativeX = eventX - (leftPos + 89);
            double relativeY = eventY - (topPos + 35);
            if (relativeX >= 0 && relativeY >= 0 && relativeX < 8 && relativeY < 8) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ArcaneSpaMenu.BUTTON_MIX);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CAMERACLACK.get(), 1.0F, 0.4F));
                return true;
            }
        }
        return handled;
    }
}
