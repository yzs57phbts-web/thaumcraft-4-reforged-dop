package thaumcraft.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.arcane.FocalManipulatorBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.menu.FocalManipulatorMenu;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;

public class FocalManipulatorScreen extends AbstractContainerScreen<FocalManipulatorMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/gui_wandtable.png");
    private static final DecimalFormat FORMAT = new DecimalFormat("#######.#");

    private final List<FocusUpgradeType> possibleUpgrades = new ArrayList<>();
    private final List<FocusUpgradeType> upgrades = new ArrayList<>();
    private final List<Sparkle> sparkles = new ArrayList<>();
    private AspectList aspects = new AspectList();
    private int selected = -1;
    private int rank;
    private long time;
    private long nextSparkle;

    private static final class Sparkle {
        private float x;
        private float y;
        private final float mx;
        private final float my;
        private final int color;
        private long nextFrame;
        private int frame;

        private Sparkle(float x, float y, float mx, float my, float red, float green, float blue) {
            this.x = x;
            this.y = y;
            this.mx = mx;
            this.my = my;
            this.color = (230 << 24) | ((int) (red * 255) << 16) | ((int) (green * 255) << 8) | (int) (blue * 255);
            this.nextFrame = System.currentTimeMillis() + 50L;
        }
    }

    public FocalManipulatorScreen(FocalManipulatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 192;
        imageHeight = 233;
        FocalManipulatorBlockEntity table = menu.table();
        if (table != null && table.getSize() > 0) {
            gatherInfo(table);
            selected = table.getUpgrade();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private void gatherInfo(FocalManipulatorBlockEntity table) {
        possibleUpgrades.clear();
        upgrades.clear();
        aspects = new AspectList();
        ItemStack stack = table.getItem(0);
        if (!(stack.getItem() instanceof FocusItem focus)) {
            return;
        }
        int[] applied = focus.getAppliedUpgrades(stack);
        rank = 1;
        int count = 0;
        while (rank <= FocusItem.MAX_UPGRADES && applied[rank - 1] != -1) {
            FocusUpgradeType type = FocusUpgradeType.byId(applied[rank - 1]);
            if (type != null) {
                upgrades.add(type);
            }
            count++;
            rank++;
        }
        if (count == FocusItem.MAX_UPGRADES) {
            rank = -1;
        } else {
            FocusUpgradeType[] possible = focus.getPossibleUpgradesByRank(stack, rank);
            if (possible == null) {
                return;
            }
            for (FocusUpgradeType type : possible) {
                if (focus.canApplyUpgrade(stack, minecraft.player, type, rank)) {
                    possibleUpgrades.add(type);
                }
            }
        }
        if (table.getSize() > 0) {
            selected = table.getUpgrade();
        }
    }

    private static boolean over(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        time = System.currentTimeMillis();
        int k = leftPos;
        int l = topPos;
        GuiBlit.blit(graphics, TEXTURE, k, l, imageWidth, imageHeight, 0, 0, imageWidth, imageHeight, 256, 256, 0xFFFFFFFF);
        FocalManipulatorBlockEntity table = menu.table();
        if (table == null) {
            return;
        }
        RandomSource random = minecraft.level.getRandom();
        if (table.getItem(0).isEmpty() || table.getRank() < 0 || table.consumeReset()) {
            rank = 0;
            selected = -1;
            possibleUpgrades.clear();
            upgrades.clear();
            aspects = new AspectList();
        }
        if (rank > 0) {
            for (int index = 0; index < possibleUpgrades.size(); index++) {
                if (selected == possibleUpgrades.get(index).id()) {
                    GuiBlit.blit(graphics, TEXTURE, k + 48 + index * 16, l + 104, 16, 16, 200, 0, 16, 16, 256, 256, 0xFFFFFFFF);
                }
            }
        }
        if (rank > 0 && selected >= 0 && !table.getItem(0).isEmpty()) {
            int xp = rank * FocalManipulatorBlockEntity.XP_MULT;
            if (table.getSize() == 0 && xp <= minecraft.player.experienceLevel) {
                GuiBlit.blit(graphics, TEXTURE, k + 48, l + 88, 96, 8, 8, 240, 96, 8, 256, 256, 0xFFFFFFFF);
            }
            GuiBlit.blit(graphics, TEXTURE, k + 108, l + 59, 16, 16, 200, 16, 16, 16, 256, 256, 0xFFFFFFFF);
            AspectList current = table.getAspects();
            int start = 0;
            if (current.size() > 0 && table.getSize() > 0) {
                for (Aspect aspect : current.getAspectsSorted()) {
                    if (current.getAmount(aspect) == 0) {
                        continue;
                    }
                    int size = (int) ((float) current.getAmount(aspect) / table.getSize() * 96.0F);
                    int color = aspect.getColor();
                    GuiBlit.blit(graphics, TEXTURE, k + 48 + start, l + 88, size, 8, 112 + start, 240, size, 8, 256, 256, (230 << 24) | color);
                    start += size;
                    if (random.nextInt(66) == 0) {
                        float x = 48 + start;
                        float y = 92.0F;
                        sparkles.add(new Sparkle(x, y, (46 + rank * 16 - x) / 9.0F, (38.0F - y) / 9.0F, (color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F));
                    }
                }
            }
            graphics.drawString(font, "" + xp, k + 125, l + 64, 0xFF000000 | (xp > minecraft.player.experienceLevel ? 16151160 : 10092429), true);
            AspectList shown = table.getSize() > 0 ? current : aspects;
            graphics.pose().pushPose();
            graphics.pose().translate(k + 49, (float) (l + 68 - shown.size() * 2.5), 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            int row = 0;
            for (Aspect aspect : shown.getAspectsSorted()) {
                graphics.drawString(font, aspect.getName(), 0, row * 10, 0xFF000000 | aspect.getColor(), true);
                graphics.drawString(font, FORMAT.format(shown.getAmount(aspect) / 100.0F), 48, row * 10, 0xFF000000 | aspect.getColor(), true);
                row++;
            }
            graphics.pose().popPose();
        }
        if (rank > 0) {
            if (nextSparkle < time) {
                nextSparkle = time + (table.getSize() > 0 ? 10 : 500) + random.nextInt(200);
                sparkles.add(new Sparkle(42 + rank * 16 + random.nextInt(12), 34 + random.nextInt(12), 0.0F, 0.0F, 0.5F + random.nextFloat() * 0.4F, 1.0F - random.nextFloat() * 0.4F, 1.0F - random.nextFloat() * 0.4F));
            }
            for (int index = 0; index < possibleUpgrades.size(); index++) {
                GuiBlit.blit(graphics, possibleUpgrades.get(index).icon(), k + 48 + index * 16, l + 104, 16, 16, 0, 0, 16, 16, 16, 16, 0xFFFFFFFF);
            }
        } else if (rank == 0 && !table.getItem(0).isEmpty()) {
            gatherInfo(table);
        }
        for (int index = 0; index < upgrades.size(); index++) {
            GuiBlit.blit(graphics, upgrades.get(index).icon(), k + 56 + index * 16, l + 32, 16, 16, 0, 0, 16, 16, 16, 16, 0xFFFFFFFF);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        Iterator<Sparkle> iterator = sparkles.iterator();
        while (iterator.hasNext()) {
            Sparkle sparkle = iterator.next();
            int x = leftPos + (int) sparkle.x;
            int y = topPos + (int) sparkle.y;
            GuiBlit.sparkle(graphics, ParticleFx.PARTICLES, x, y, sparkle.frame, sparkle.color);
            if (sparkle.nextFrame < time) {
                sparkle.frame++;
                sparkle.nextFrame = time + 50L;
                sparkle.x += sparkle.mx;
                sparkle.y += sparkle.my;
            }
            if (sparkle.frame >= 9) {
                iterator.remove();
            }
        }
        FocalManipulatorBlockEntity table = menu.table();
        int fixedX = leftPos + imageWidth - 36;
        int fixedY = topPos + 24;
        int wrap = Math.max(60, width - (leftPos + imageWidth - 16));
        if (rank > 0) {
            for (int index = 0; index < possibleUpgrades.size(); index++) {
                if (over(mouseX, mouseY, leftPos + 48 + index * 16, topPos + 104, 16, 16)) {
                    upgradeTooltip(graphics, possibleUpgrades.get(index), fixedX, fixedY, wrap);
                }
            }
        }
        if (selected >= 0 && table != null) {
            if (over(mouseX, mouseY, leftPos + 48, topPos + 48, 36, 36)) {
                graphics.renderTooltip(font, Component.translatable("wandtable.text1"), mouseX, mouseY);
            }
            if (over(mouseX, mouseY, leftPos + 108, topPos + 58, 36, 16)) {
                graphics.renderTooltip(font, Component.translatable("wandtable.text2"), mouseX, mouseY);
            }
            if (table.getSize() == 0 && rank * FocalManipulatorBlockEntity.XP_MULT <= minecraft.player.experienceLevel && over(mouseX, mouseY, leftPos + 48, topPos + 88, 96, 8)) {
                graphics.renderTooltip(font, Component.translatable("wandtable.text3"), mouseX, mouseY);
            }
        }
        for (int index = 0; index < upgrades.size(); index++) {
            if (over(mouseX, mouseY, leftPos + 56 + index * 16, topPos + 32, 16, 16)) {
                upgradeTooltip(graphics, upgrades.get(index), fixedX, fixedY, wrap);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void upgradeTooltip(GuiGraphics graphics, FocusUpgradeType type, int x, int y, int wrap) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.addAll(font.split(type.getLocalizedName().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE), wrap));
        lines.addAll(font.split(type.getLocalizedText(), wrap));
        graphics.renderTooltip(font, lines, x, y);
    }

    @Override
    public boolean mouseClicked(double eventX, double eventY, int button) {
        boolean handled = super.mouseClicked(eventX, eventY, button);
        FocalManipulatorBlockEntity table = menu.table();
        if (button != InputConstants.MOUSE_BUTTON_LEFT || table == null) {
            return handled;
        }
        double mx = eventX;
        double my = eventY;
        if (table.getSize() == 0 && selected >= 0 && rank * FocalManipulatorBlockEntity.XP_MULT <= minecraft.player.experienceLevel && over(mx, my, leftPos + 48, topPos + 88, 96, 8)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selected);
            playClick();
            return true;
        }
        if (table.getSize() == 0) {
            for (int index = 0; index < possibleUpgrades.size(); index++) {
                FocusUpgradeType type = possibleUpgrades.get(index);
                if (over(mx, my, leftPos + 48 + index * 16, topPos + 104, 16, 16)) {
                    aspects = new AspectList();
                    if (selected == type.id()) {
                        selected = -1;
                    } else {
                        selected = type.id();
                        int amount = FocalManipulatorBlockEntity.VIS_MULT;
                        for (int step = 1; step < rank; step++) {
                            amount *= 2;
                        }
                        AspectList cost = new AspectList();
                        for (Aspect aspect : type.aspects().getAspects()) {
                            cost.add(aspect, amount);
                        }
                        aspects = AspectHelper.reduceToPrimals(cost);
                    }
                    playClick();
                    return true;
                }
            }
        }
        return handled;
    }

    private void playClick() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CAMERACLACK.get(), 1.0F, 0.4F));
    }
}
