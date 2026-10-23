package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.arcane.ArcaneWorkbenchBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.crafting.ArcaneCrafting;
import thaumcraft.menu.ArcaneWorkbenchMenu;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;

public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/gui_arcaneworkbench.png");
    private static final int[][] ASPECT_LOCATIONS = {{72, 21}, {24, 43}, {24, 102}, {72, 124}, {120, 102}, {120, 43}};
    private static final int INSUFFICIENT_COLOR = 15625838;

    public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 190;
        imageHeight = 234;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiBlit.blit(graphics, TEXTURE, leftPos, topPos, imageWidth, imageHeight, 0, 0, imageWidth, imageHeight, 256, 256, 0xFFFFFFFF);
        ItemStack wandStack = menu.getWand();
        WandItem wand = wandStack.getItem() instanceof WandItem wandItem ? wandItem : null;
        AspectList cost = menu.getMatchedAspects();
        boolean hasCost = cost.size() > 0;
        if (hasCost) {
            int count = 0;
            for (Aspect primal : Aspect.getPrimalAspects()) {
                float amount = cost.getAmount(primal);
                if (amount > 0) {
                    float alpha = 0.5F + (Mth.sin((minecraft.player.tickCount + count * 10) / 2.0F) * 0.2F - 0.2F);
                    if (wand != null) {
                        amount *= wand.getConsumptionModifier(wandStack, minecraft.player, primal, true);
                        if (amount * 100.0F <= Wands.getVis(wandStack, primal)) {
                            alpha = 1.0F;
                        }
                    }
                    AspectRenderer.drawTag(graphics, leftPos + ASPECT_LOCATIONS[count][0] - 8, topPos + ASPECT_LOCATIONS[count][1] - 8, primal, amount, 0, alpha, false);
                }
                if (++count > 5) {
                    break;
                }
            }
        }
        if (wand != null && hasCost && !wand.consumeAllVisCrafting(wandStack, minecraft.player, cost, false)) {
            ArcaneWorkbenchBlockEntity workbench = menu.workbench();
            ArcaneCrafting.Match match = workbench == null ? null : ArcaneCrafting.find(workbench.getGrid(), minecraft.player);
            if (match != null && !match.result().isEmpty()) {
                graphics.renderItem(match.result(), leftPos + 160, topPos + 64);
                graphics.fill(leftPos + 160, topPos + 64, leftPos + 176, topPos + 80, 0xA8000000);
            }
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + 168, topPos + 46, 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            Component text = Component.literal("Insufficient vis");
            graphics.drawString(font, text, -font.width(text) / 2, 0, 0xFF000000 | INSUFFICIENT_COLOR, false);
            graphics.pose().popPose();
        }
    }
}
