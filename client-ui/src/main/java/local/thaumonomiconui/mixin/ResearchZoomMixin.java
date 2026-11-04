package local.thaumonomiconui.mixin;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.client.platform.BrowserDraw;
import thaumcraft.client.platform.ClientNetwork;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.client.platform.GuiLines;
import thaumcraft.network.PlayerCompletePayload;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.Knowledge;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchCategory;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNotes;


import thaumcraft.client.research.*;
import local.thaumonomiconui.ZoomControl;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ResearchBrowserScreen.class, remap = false)
public abstract class ResearchZoomMixin extends Screen implements ZoomControl {
    @Shadow @Final private static ResourceLocation TEXTURE;
    @Shadow @Final private static List<String> HIGHLIGHTED;
    @Shadow private static String selectedCategory;
    @Shadow private double prevMapX, prevMapY, mapX, mapY, targetX, targetY;
    @Shadow private int guiMapTop, guiMapLeft, guiMapBottom, guiMapRight;
    @Shadow @Final private List<ResearchItem> research;
    @Shadow private ResearchItem currentHighlight;
    @Shadow private boolean dragging;
    @Shadow private int left() { throw new AssertionError(); }
    @Shadow private int top() { throw new AssertionError(); }
    @Shadow private PlayerKnowledge knowledge() { throw new AssertionError(); }
    @Shadow private boolean canUnlock(ResearchItem item) { throw new AssertionError(); }
    @Shadow private List<ResearchCategory> visibleTabs() { throw new AssertionError(); }
    @Shadow private static float pulse() { throw new AssertionError(); }
    @Shadow private static int gray(float value) { throw new AssertionError(); }
    @Shadow private void drawLine(GuiGraphics g, int x, int y, int x2, int y2, float r, float gr, float b, float tick, boolean wiggle) { throw new AssertionError(); }
    @Shadow private void drawForbidden(GuiGraphics g, int x, int y) { throw new AssertionError(); }
    @Unique private static final int PANE_WIDTH = 256, PANE_HEIGHT = 230, VIEW_WIDTH = 224, VIEW_HEIGHT = 196;
    @Unique private static double thaumonomiconui$remembered = 1;
    @Unique private double thaumonomiconui$zoom = thaumonomiconui$remembered;
    protected ResearchZoomMixin(Component title) { super(title); }
    @Override public double thaumonomiconui$getZoom() { return thaumonomiconui$zoom; }
    @Override public void thaumonomiconui$setZoom(double value, double x, double y) {
        double next = Mth.clamp(value, 0.5, 2.0);
        double old = thaumonomiconui$zoom;
        if (next == old) return;
        double dx = x - left() - 16, dy = y - top() - 17;
        double ox = Mth.clamp(mapX, guiMapTop, Math.max(guiMapTop, guiMapBottom - 1));
        double oy = Mth.clamp(mapY, guiMapLeft, Math.max(guiMapLeft, guiMapRight - 1));
        prevMapX = mapX = targetX = Mth.clamp(ox + dx / old - dx / next, guiMapTop, Math.max(guiMapTop, guiMapBottom - 1));
        prevMapY = mapY = targetY = Mth.clamp(oy + dy / old - dy / next, guiMapLeft, Math.max(guiMapLeft, guiMapRight - 1));
        thaumonomiconui$remembered = thaumonomiconui$zoom = next;
        currentHighlight = null;
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= left() + 16 && x < left() + 240 && y >= top() + 17 && y < top() + 213) {
            thaumonomiconui$setZoom(thaumonomiconui$zoom * Math.pow(1.15, vertical), x, y);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }
    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void thaumonomiconui$drag(double x, double y, int button, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0 && dragging) {
            if (x >= left() + 16 && x < left() + 240 && y >= top() + 17 && y < top() + 213) {
                prevMapX = mapX = targetX = Mth.clamp(mapX - dx / thaumonomiconiconZoom(), guiMapTop, Math.max(guiMapTop, guiMapBottom - 1));
                prevMapY = mapY = targetY = Mth.clamp(mapY - dy / thaumonomiconiconZoom(), guiMapLeft, Math.max(guiMapLeft, guiMapRight - 1));
                currentHighlight = null;
            }
            cir.setReturnValue(true);
        }
    }
    @Unique private double thaumonomiconiconZoom() { return thaumonomiconui$zoom; }
    @Overwrite
    private void drawMap(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long time = Util.getNanos() / 50000000L;
        int offsetX = Mth.floor(prevMapX + (mapX - prevMapX) * partialTick);
        int offsetY = Mth.floor(prevMapY + (mapY - prevMapY) * partialTick);
        offsetX = Math.max(offsetX, guiMapTop);
        offsetY = Math.max(offsetY, guiMapLeft);
        if (offsetX >= guiMapBottom) {
            offsetX = guiMapBottom - 1;
        }
        if (offsetY >= guiMapRight) {
            offsetY = guiMapRight - 1;
        }
        int paneX = left();
        int paneY = top();
        int mapLeft = paneX + 16;
        int mapTop = paneY + 17;
        ResearchCategory category = ResearchCategories.get(selectedCategory);
        Set<String> done = knowledge().research();

        graphics.enableScissor(mapLeft, mapTop, mapLeft + VIEW_WIDTH, mapTop + VIEW_HEIGHT);
        int backgroundU = (int) ((float) (offsetX - guiMapTop) / Math.abs(guiMapTop - guiMapBottom) * 288.0F) / 2 * 2;
        int backgroundV = (int) ((float) (offsetY - guiMapLeft) / Math.abs(guiMapLeft - guiMapRight) * 316.0F) / 2 * 2;
        GuiBlit.blit(graphics, category.background(), mapLeft, mapTop, VIEW_WIDTH, VIEW_HEIGHT, backgroundU, backgroundV, VIEW_WIDTH, VIEW_HEIGHT, 512, 512, 0xFFFFFFFF);

        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(mapLeft, mapTop, 0);
        graphics.pose().scale((float) thaumonomiconui$zoom, (float) thaumonomiconui$zoom, 1);
        graphics.pose().translate(-mapLeft, -mapTop, 0);
        boolean inside = mouseX >= mapLeft && mouseY >= mapTop && mouseX < mapLeft + VIEW_WIDTH && mouseY < mapTop + VIEW_HEIGHT;
        mouseX = inside ? (int) Math.floor(mapLeft + (mouseX - mapLeft) / thaumonomiconui$zoom) : Integer.MIN_VALUE / 2;
        mouseY = inside ? (int) Math.floor(mapTop + (mouseY - mapTop) / thaumonomiconui$zoom) : Integer.MIN_VALUE / 2;

        for (ResearchItem item : research) {
            for (String parentKey : item.parents()) {
                ResearchItem parent = parentKey == null ? null : ResearchCategories.getResearch(parentKey);
                if (parent == null || !parent.category().equals(selectedCategory) || parent.isVirtual()) {
                    continue;
                }
                int x1 = item.column() * 24 - offsetX + 11 + mapLeft;
                int y1 = item.row() * 24 - offsetY + 11 + mapTop;
                int x2 = parent.column() * 24 - offsetX + 11 + mapLeft;
                int y2 = parent.row() * 24 - offsetY + 11 + mapTop;
                boolean itemDone = done.contains(item.key());
                boolean parentDone = done.contains(parent.key());
                if (itemDone) {
                    drawLine(graphics, x1, y1, x2, y2, 0.1F, 0.1F, 0.1F, partialTick, false);
                } else if (!item.isLost()
                    && (!item.isHidden() && !item.isLost() || done.contains("@" + item.key()))
                    && (!item.isConcealed() || canUnlock(item))) {
                    if (parentDone) {
                        drawLine(graphics, x1, y1, x2, y2, 0.0F, 1.0F, 0.0F, partialTick, true);
                    } else if ((!parent.isHidden() && !item.isLost() || done.contains("@" + parent.key()))
                        && (!parent.isConcealed() || canUnlock(parent))) {
                        drawLine(graphics, x1, y1, x2, y2, 0.0F, 0.0F, 1.0F, partialTick, true);
                    }
                }
            }
            for (String siblingKey : item.siblings()) {
                ResearchItem sibling = siblingKey == null ? null : ResearchCategories.getResearch(siblingKey);
                if (sibling == null || !sibling.category().equals(selectedCategory) || sibling.isVirtual() || sibling.parents().contains(item.key())) {
                    continue;
                }
                int x1 = item.column() * 24 - offsetX + 11 + mapLeft;
                int y1 = item.row() * 24 - offsetY + 11 + mapTop;
                int x2 = sibling.column() * 24 - offsetX + 11 + mapLeft;
                int y2 = sibling.row() * 24 - offsetY + 11 + mapTop;
                boolean itemDone = done.contains(item.key());
                boolean siblingDone = done.contains(sibling.key());
                if (itemDone) {
                    drawLine(graphics, x1, y1, x2, y2, 0.1F, 0.1F, 0.2F, partialTick, false);
                } else if (!item.isLost()
                    && (!item.isHidden() || done.contains("@" + item.key()))
                    && (!item.isConcealed() || canUnlock(item))) {
                    if (siblingDone) {
                        drawLine(graphics, x1, y1, x2, y2, 0.0F, 1.0F, 0.0F, partialTick, true);
                    } else if ((!sibling.isHidden() || done.contains("@" + sibling.key()))
                        && (!sibling.isConcealed() || canUnlock(sibling))) {
                        drawLine(graphics, x1, y1, x2, y2, 0.0F, 0.0F, 1.0F, partialTick, true);
                    }
                }
            }
        }

        currentHighlight = null;
        List<Runnable> icons = new ArrayList<>();
        for (ResearchItem item : research) {
            int relativeX = item.column() * 24 - offsetX;
            int relativeY = item.row() * 24 - offsetY;
            if (item.isVirtual() || relativeX < -24 || relativeY < -24 || relativeX > VIEW_WIDTH / thaumonomiconui$zoom || relativeY > VIEW_HEIGHT / thaumonomiconui$zoom) {
                continue;
            }
            int x = mapLeft + relativeX;
            int y = mapTop + relativeY;
            boolean complete = done.contains(item.key());
            boolean unlockable = canUnlock(item);
            int tint;
            if (complete) {
                if (item.warp() > 0) {
                    drawForbidden(graphics, x + 11, y + 11);
                }
                tint = 0xFFFFFFFF;
            } else {
                if (!done.contains("@" + item.key())
                    && (item.isLost() || item.isHidden() || item.isConcealed() && !unlockable)) {
                    continue;
                }
                if (item.warp() > 0) {
                    drawForbidden(graphics, x + 11, y + 11);
                }
                tint = unlockable ? gray(pulse() * 0.25F + 0.75F) : gray(0.3F);
            }
            boolean buyable = ResearchManager.isBuyable(item);
            int frameU;
            if (item.isRound()) {
                frameU = 54;
            } else if (item.isHidden()) {
                frameU = buyable ? 230 : 86;
            } else {
                frameU = buyable ? 110 : 0;
            }
            local.thaumonomiconui.ResearchFrames.draw(graphics, frameU, x - 2, y - 2, tint);
            if (item.isSpecial()) {
                local.thaumonomiconui.ResearchFrames.draw(graphics, 26, x - 2, y - 2, tint);
            }
            if (HIGHLIGHTED.contains(item.key())) {
                GuiBlit.blit(graphics, AspectRenderer.PARTICLES, x - 5, y - 5, 16, 16, (int) (time % 16L) * 16, 80, 16, 16, 256, 256, 0xFFFFFFFF);
            }
            final int iconX = x + 3;
            final int iconY = y + 3;
            final int iconTint = unlockable ? tint : gray(0.2F);
            ItemStack stack = item.iconItem() == null ? ItemStack.EMPTY : item.iconItem().create();
            icons.add(() -> {
                if (!stack.isEmpty()) {
                    graphics.renderItem(stack, iconX, iconY);
                } else {
                    ResearchIconsAccess.thaumonomiconui$drawFallback(graphics, item, iconX, iconY, iconTint);
                }
            });
            if (mouseX >= mapLeft && mouseY >= mapTop && mouseX < mapLeft + VIEW_WIDTH / thaumonomiconui$zoom && mouseY < mapTop + VIEW_HEIGHT / thaumonomiconui$zoom
                && mouseX >= x && mouseX <= x + 22 && mouseY >= y && mouseY <= y + 22) {
                currentHighlight = item;
            }
        }
        graphics.flush();
        for (Runnable icon : icons) {
            icon.run();
        }
        graphics.flush();
        graphics.pose().popPose();
        graphics.disableScissor();
        graphics.flush();

        List<ResearchCategory> tabs = visibleTabs();
        for (int index = 0; index < tabs.size(); index++) {
            ResearchCategory tab = tabs.get(index);
            boolean swop = index >= 9;
            int count = index % 9;
            boolean selected = tab.key().equals(selectedCategory);
            int s0 = swop ? 264 : 0;
            int s1 = 0;
            int s2 = swop ? 14 : 0;
            if (!selected) {
                s1 = 24;
                s2 = swop ? 6 : 8;
            }
            if (swop) {
                BrowserDraw.reversed(graphics, TEXTURE, paneX + s0 - 8, paneY + count * 24, 24, 24, 176 + s1, 232, 256, 256);
            } else {
                GuiBlit.blit(graphics, TEXTURE, paneX - 24 + s0, paneY + count * 24, 24, 24, 152 + s1, 232, 24, 24, 256, 256, 0xFFFFFFFF);
            }
            if (HIGHLIGHTED.contains(tab.key())) {
                GuiBlit.blit(graphics, AspectRenderer.PARTICLES, paneX - 27 + s2 + s0, paneY - 4 + count * 24, 16, 16, (int) (16L * (time % 16L)), 80, 16, 16, 256, 256, 0xFFFFFFFF);
            }
            BrowserDraw.full(graphics, tab.icon(), paneX - 19 + s2 + s0, paneY + 4 + count * 24, 16, 16, 0xFFFFFFFF);
            if (!selected) {
                if (swop) {
                    BrowserDraw.reversed(graphics, TEXTURE, paneX + s0 - 8, paneY + count * 24, 24, 24, 224, 232, 256, 256);
                } else {
                    GuiBlit.blit(graphics, TEXTURE, paneX - 24 + s0, paneY + count * 24, 24, 24, 200, 232, 24, 24, 256, 256, 0xFFFFFFFF);
                }
            }
        }
        GuiBlit.blit(graphics, TEXTURE, paneX, paneY, PANE_WIDTH, PANE_HEIGHT, 0, 0, PANE_WIDTH, PANE_HEIGHT, 256, 256, 0xFFFFFFFF);
    }


}
