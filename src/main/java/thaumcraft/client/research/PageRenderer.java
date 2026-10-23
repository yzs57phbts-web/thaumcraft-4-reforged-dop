package thaumcraft.client.research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ScanKind;

final class PageRenderer {
    static final ResourceLocation OVERLAY = Thaumcraft.id("textures/gui/gui_researchbook_overlay.png");
    static final int TEXT_WIDTH = 139;
    static final int TITLE_COLOR = 0xFF505050;

    final Minecraft minecraft;
    final Font font;
    final ResearchItem research;
    final Map<Aspect, List<ItemStack>> aspectItems = new HashMap<>();
    final List<Hit> hits = new ArrayList<>();
    GuiGraphics graphics;
    int mouseX;
    int mouseY;
    int cycle = -1;
    @Nullable List<Component> tooltip;
    ItemStack tooltipStack = ItemStack.EMPTY;

    record Hit(int x, int y, PageIndex.Reference reference) {
    }

    PageRenderer(Minecraft minecraft, ResearchItem research) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.research = research;
        if (research.key().equals("ASPECTS")) {
            collectAspectItems();
        }
    }

    private void collectAspectItems() {
        for (String key : Knowledge.get(minecraft.player).scannedSet(ScanKind.OBJECT)) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            AspectList tags = AspectHelper.getBonusTags(stack, AspectHelper.getObjectTags(stack));
            for (Aspect aspect : tags.getAspects()) {
                ItemStack copy = stack.copy();
                copy.setCount(Math.max(1, Math.min(99, tags.getAmount(aspect))));
                aspectItems.computeIfAbsent(aspect, ignored -> new ArrayList<>()).add(copy);
            }
        }
    }

    void begin(GuiGraphics graphics, int mouseX, int mouseY) {
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    void resetFrame() {
        tooltip = null;
        tooltipStack = ItemStack.EMPTY;
        hits.clear();
    }

    boolean hovering(int x, int y, int width, int height) {
        int dx = mouseX - x;
        int dy = mouseY - y;
        return dx >= 0 && dy >= 0 && dx < width && dy < height;
    }

    int index(int size) {
        return size <= 1 ? 0 : Math.floorMod(cycle, size);
    }

    void overlay(int x, int y, int drawX, int drawY, int u, int v, int width, int height, float alpha) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        GuiBlit.blit(graphics, OVERLAY, drawX, drawY, width, height, u, v, width, height, 256, 256, FastColor.ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));
        graphics.pose().popPose();
    }

    void centered(Component text, int x, int y, int color) {
        graphics.drawString(font, text, x - font.width(text) / 2, y, color, false);
    }

    void title(String key, int x, int y) {
        centered(Component.translatable(key), x + 56, y, TITLE_COLOR);
    }

    void instability(int instability, int x, int y) {
        int level = Math.min(5, instability / 2);
        Component text = Component.literal(lang("tc.inst") + " " + lang("tc.inst." + level));
        float shrink = Math.min(1.0F, 128.0F / Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(x + 56, y, 0);
        graphics.pose().scale(shrink, shrink, 1);
        centered(text, 0, 0, TITLE_COLOR);
        graphics.pose().popPose();
    }

    ItemStack variant(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stacks.get((int) (System.currentTimeMillis() / 1000L % stacks.size()));
    }

    void slot(List<ItemStack> stacks, int x, int y, boolean output, boolean link) {
        slot(stacks, x, y, output, link, 1.0F);
    }

    void slot(List<ItemStack> stacks, int x, int y, boolean output, boolean link, float scale) {
        ItemStack stack = variant(stacks);
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, x, y);
        if (output) {
            graphics.renderItemDecorations(font, stack, x, y);
        }
        int size = (int) Math.ceil(16 * scale);
        if (hovering(x, y, size, size)) {
            hover(stack, x, y, link);
        }
    }

    void hover(ItemStack stack, int x, int y, boolean link) {
        List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(minecraft, stack));
        if (link) {
            PageIndex.Reference reference = PageIndex.find(minecraft.player, stack);
            if (reference != null && !reference.research().key().equals(research.key())) {
                lines.add(Component.translatable("recipe.clickthrough").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                hits.add(new Hit(x, y, reference));
            }
        }
        tooltip = lines;
        tooltipStack = stack;
    }

    void tag(Aspect aspect, float amount, int x, int y) {
        AspectRenderer.drawTag(graphics, x, y, aspect, amount);
        if (hovering(x, y, 16, 16)) {
            tooltip = List.of(Component.literal(aspect.getName()), aspect.getDescription());
            tooltipStack = ItemStack.EMPTY;
        }
    }

    void flushTooltip() {
        if (tooltip == null) {
            return;
        }
        if (tooltipStack.isEmpty()) {
            graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        } else {
            graphics.renderTooltip(font, tooltip, tooltipStack.getTooltipImage(), tooltipStack, mouseX, mouseY);
        }
    }

    void aspectRows(AspectList aspects, int perRow, int x, int y, int multiplier) {
        int size = aspects.size();
        int rows = (size - 1) / perRow;
        int shift = (perRow - size % perRow) * 10;
        List<Aspect> sorted = aspects.getAspectsSorted();
        int total = 0;
        for (Aspect aspect : sorted) {
            int mirrored = total / perRow >= rows && (rows > 1 || size < perRow) ? 1 : 0;
            tag(aspect, aspects.getAmount(aspect) * multiplier, x + total % perRow * 20 + shift * mirrored, y + total / perRow * 20 - 10 * rows);
            total++;
        }
    }

    void costLine(AspectList aspects, int x, int y) {
        int count = 0;
        for (Aspect aspect : aspects.getAspectsSortedAmount()) {
            tag(aspect, aspects.getAmount(aspect), x + 14 + 18 * count + (5 - aspects.size()) * 8, y);
            count++;
        }
    }

    static String lang(String key) {
        return Language.getInstance().getOrDefault(key);
    }

    void text(int side, int x, int y, List<BookText.Line> lines) {
        int textX = x - 15 + side * 152;
        BookText.draw(graphics, font, lines, textX, y, TEXT_WIDTH, 0xFF000000);
    }

    void aspects(int side, int x, int y, AspectList aspects) {
        if (aspects == null || aspects.size() == 0) {
            return;
        }
        int start = side * 152;
        int count = 0;
        List<Aspect> sorted = aspects.getAspectsSorted();
        for (Aspect aspect : sorted) {
            int tx = x + start;
            int ty = y + count * 50;
            if (hovering(tx, ty, 40, 40)) {
                graphics.pose().pushPose();
                graphics.pose().translate(tx - 5, ty - 5, 0.0F);
                graphics.pose().scale(2.5F, 2.5F, 1.0F);
                GuiBlit.blit(graphics, AspectRenderer.BACK, 0, 0, 16, 16, 0, 0, 64, 64, 64, 64, 0xFFFFFFFF);
                graphics.pose().popPose();
            }
            graphics.pose().pushPose();
            graphics.pose().scale(2.0F, 2.0F, 1.0F);
            AspectRenderer.drawTag(graphics, tx / 2, ty / 2, aspect, aspects.getAmount(aspect));
            graphics.pose().popPose();
            Component name = BookText.uniform(aspect.getName());
            graphics.drawString(font, name, tx + 16 - font.width(name) / 2, y + 33 + count * 50, TITLE_COLOR, false);
            Aspect[] components = aspect.getComponents();
            if (components != null) {
                graphics.pose().pushPose();
                graphics.pose().scale(1.5F, 1.5F, 1.0F);
                AspectRenderer.drawTag(graphics, (int) ((x + start + 54) / 1.5F), (int) ((y + 4 + count * 50) / 1.5F), components[0], 0.0F);
                AspectRenderer.drawTag(graphics, (int) ((x + start + 96) / 1.5F), (int) ((y + 4 + count * 50) / 1.5F), components[1], 0.0F);
                graphics.pose().popPose();
                Component first = BookText.uniform("§o" + components[0].getName());
                graphics.drawString(font, first, tx + 16 - font.width(first) / 2 + 50, y + 30 + count * 50, TITLE_COLOR, false);
                Component second = BookText.uniform("§o" + components[1].getName());
                graphics.drawString(font, second, tx + 16 - font.width(second) / 2 + 92, y + 30 + count * 50, TITLE_COLOR, false);
                graphics.drawString(font, "=", tx + 7 + 32, y + 12 + count * 50, 0xFF999999, false);
                graphics.drawString(font, "+", tx + 4 + 79, y + 12 + count * 50, 0xFF999999, false);
            } else {
                graphics.drawString(font, BookText.uniform(lang("tc.aspect.primal")), tx + 48, y + 12 + count * 50, 0xFF444444, false);
            }
            count++;
        }
        count = 0;
        for (Aspect aspect : sorted) {
            int tx = x + start;
            int ty = y + count * 50;
            List<ItemStack> items = aspectItems.get(aspect);
            if (hovering(tx, ty, 40, 40) && items != null && !items.isEmpty()) {
                graphics.flush();
                int column = 0;
                int row = 0;
                for (ItemStack item : items) {
                    int itemX = mouseX + 8 + column * 17;
                    int itemY = 17 * row + (mouseY - (4 + items.size() / 8 * 8));
                    graphics.renderItem(item, itemX, itemY);
                    graphics.renderItemDecorations(font, item, itemX, itemY);
                    if (++column >= 8) {
                        column = 0;
                        row++;
                    }
                }
            }
            count++;
        }
    }
}
