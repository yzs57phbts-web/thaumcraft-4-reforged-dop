package thaumcraft.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.ThaumatoriumBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.menu.ThaumatoriumMenu;
import thaumcraft.registry.ModSounds;

public class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/gui_thaumatorium.png");

    private int index;
    private int lastSize;
    private int startAspect;

    public ThaumatoriumScreen(ThaumatoriumMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        menu.updateRecipes();
        lastSize = menu.recipes().size();
        refreshIndex();
    }

    private ThaumatoriumBlockEntity thaumatorium() {
        return menu.thaumatorium();
    }

    private List<String> selected() {
        return thaumatorium() == null ? List.of() : thaumatorium().getRecipeKeys();
    }

    private int maxRecipes() {
        return thaumatorium() == null ? 0 : thaumatorium().getMaxRecipes();
    }

    private void refreshIndex() {
        List<CrucibleRecipe> recipes = menu.recipes();
        for (int position = 0; position < recipes.size(); position++) {
            if (selected().contains(recipes.get(position).key())) {
                index = position;
                break;
            }
        }
        startAspect = 0;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        menu.updateRecipes();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private void sheet(GuiGraphics graphics, int x, int y, int width, int height, int u, int v, int color) {
        GuiBlit.blit(graphics, TEXTURE, x, y, width, height, u, v, width, height, 256, 256, color);
    }

    private void sheet(GuiGraphics graphics, int x, int y, int width, int height, int u, int v) {
        sheet(graphics, x, y, width, height, u, v, 0xFFFFFFFF);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        sheet(graphics, leftPos, topPos, imageWidth, imageHeight, 0, 0);
        List<CrucibleRecipe> recipes = menu.recipes();
        if (index >= recipes.size()) {
            index = recipes.size() - 1;
        }
        if (recipes.isEmpty()) {
            return;
        }
        if (lastSize != recipes.size()) {
            lastSize = recipes.size();
            refreshIndex();
        }
        if (index < 0) {
            index = 0;
        }
        CrucibleRecipe recipe = recipes.get(index);
        if (recipes.size() > 1) {
            sheet(graphics, leftPos + 128, topPos + 16, 16, 8, index > 0 ? 192 : 176, 16);
            sheet(graphics, leftPos + 128, topPos + 24, 16, 8, index < recipes.size() - 1 ? 192 : 176, 24);
        }
        if (recipe.aspects().size() > 6) {
            sheet(graphics, leftPos + 32, topPos + 40, 8, 16, startAspect > 0 ? 192 : 176, 32);
            sheet(graphics, leftPos + 136, topPos + 40, 8, 16, startAspect < recipe.aspects().size() - 1 ? 200 : 184, 32);
        } else {
            startAspect = 0;
        }
        List<String> keys = selected();
        if (!keys.isEmpty()) {
            int relativeX = mouseX - (leftPos + 112);
            int relativeY = mouseY - (topPos + 16);
            if (relativeX >= 0 && relativeY >= 0 && relativeX < 16 && relativeY < 16 || keys.contains(recipe.key())) {
                sheet(graphics, leftPos + 104, topPos + 8, 48, 48, 176, 96);
            }
            float alpha = 0.6F + Mth.sin(minecraft.player.tickCount / 5.0F) * 0.4F + 0.4F;
            sheet(graphics, leftPos + 88, topPos + 16, 24, 24, 176, 56, FastColor.ARGB32.colorFromFloat(Math.min(1.0F, alpha), 1.0F, 1.0F, 1.0F));
        }
        drawAspects(graphics, recipe);
        drawOutput(graphics, recipe, mouseX, mouseY);
        if (maxRecipes() > 1) {
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + 136, topPos + 33, 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            String text = keys.size() + "/" + maxRecipes();
            graphics.drawString(font, text, -font.width(text) / 2, 0, 0xFFFFFFFF, false);
            graphics.pose().popPose();
        }
    }

    private void drawAspects(GuiGraphics graphics, CrucibleRecipe recipe) {
        AspectList aspects = recipe.aspects();
        List<Aspect> sorted = aspects.getAspectsSorted();
        int count = 0;
        int position = 0;
        if (selected().contains(recipe.key())) {
            for (Aspect aspect : sorted) {
                if (count >= startAspect) {
                    sheet(graphics, leftPos + 41 + 16 * position, topPos + 57, 14, 6, 176, 8);
                    int filled = (int) ((float) thaumatorium().getEssentia().getAmount(aspect) / aspects.getAmount(aspect) * 12.0F);
                    if (filled > 0) {
                        sheet(graphics, leftPos + 42 + 16 * position, topPos + 58, filled, 4, 176, 0, FastColor.ARGB32.opaque(aspect.getColor()));
                    }
                    position++;
                }
                if (++count >= 6 + startAspect) {
                    break;
                }
            }
        }
        count = 0;
        position = 0;
        for (Aspect aspect : sorted) {
            if (count >= startAspect) {
                AspectRenderer.drawTag(graphics, leftPos + 40 + 16 * position, topPos + 40, aspect, aspects.getAmount(aspect));
                position++;
            }
            if (++count >= 6 + startAspect) {
                break;
            }
        }
    }

    private void drawOutput(GuiGraphics graphics, CrucibleRecipe recipe, int mouseX, int mouseY) {
        ItemStack output = recipe.result().create();
        int x = leftPos + 112;
        int y = topPos + 16;
        graphics.renderItem(output, x, y);
        graphics.renderItemDecorations(font, output, x, y);
        boolean selectable = selected().size() < maxRecipes() || selected().contains(recipe.key());
        if (selectable) {
            float alpha = 0.3F + Mth.sin(minecraft.player.tickCount / 4.0F) * 0.3F + 0.3F;
            graphics.fill(x, y, x + 16, y + 16, FastColor.ARGB32.colorFromFloat(Math.clamp(1.0F - alpha, 0.0F, 1.0F) * 0.6F, 0.0F, 0.0F, 0.0F));
        }
        if (mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 16) {
            graphics.renderTooltip(font, output, mouseX, mouseY);
        }
    }

    private void play(SoundEvent sound, float volume) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, volume));
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double eventX, double eventY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT && handleClick(eventX, eventY)) {
            return true;
        }
        return super.mouseClicked(eventX, eventY, button);
    }

    private boolean handleClick(double mouseX, double mouseY) {
        List<CrucibleRecipe> recipes = menu.recipes();
        if (recipes.isEmpty() || index < 0 || index >= recipes.size()) {
            return false;
        }
        CrucibleRecipe recipe = recipes.get(index);
        if (inside(mouseX, mouseY, leftPos + 112, topPos + 16, 16, 16) && (selected().size() < maxRecipes() || selected().contains(recipe.key()))) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
            play(ModSounds.HHON.get(), 0.3F);
            return true;
        }
        if (recipes.size() > 1) {
            if (index > 0 && inside(mouseX, mouseY, leftPos + 128, topPos + 16, 16, 8)) {
                index--;
                play(ModSounds.CAMERACLACK.get(), 0.4F);
                return true;
            }
            if (index < recipes.size() - 1 && inside(mouseX, mouseY, leftPos + 128, topPos + 24, 16, 8)) {
                index++;
                play(ModSounds.CAMERACLACK.get(), 0.4F);
                return true;
            }
        }
        if (recipe.aspects().size() > 6) {
            if (startAspect > 0 && inside(mouseX, mouseY, leftPos + 32, topPos + 40, 8, 16)) {
                startAspect--;
                play(ModSounds.CAMERACLACK.get(), 0.4F);
                return true;
            }
            if (startAspect < recipe.aspects().size() - 1 && inside(mouseX, mouseY, leftPos + 136, topPos + 40, 8, 16)) {
                startAspect++;
                play(ModSounds.CAMERACLACK.get(), 0.4F);
                return true;
            }
        }
        return false;
    }
}
