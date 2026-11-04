"""Preserve the port's native renderer and add a map-only transform."""
from pathlib import Path
root = Path(__file__).resolve().parent
source = (root/'reference/ResearchBrowserScreen.java').read_text(encoding='utf8')
imports = source[source.index('import '):source.index('public class ')]
method = source[source.index('    private void drawMap('):source.index('    private void drawOverlays(')]
method = method.replace('    private void drawMap(', '    @Overwrite\n    private void drawMap(')
anchor = '        for (ResearchItem item : research) {'
transform = '''        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(mapLeft, mapTop, 0);
        graphics.pose().scale((float) thaumonomiconui$zoom, (float) thaumonomiconui$zoom, 1);
        graphics.pose().translate(-mapLeft, -mapTop, 0);
        boolean inside = mouseX >= mapLeft && mouseY >= mapTop && mouseX < mapLeft + VIEW_WIDTH && mouseY < mapTop + VIEW_HEIGHT;
        mouseX = inside ? (int) Math.floor(mapLeft + (mouseX - mapLeft) / thaumonomiconui$zoom) : Integer.MIN_VALUE / 2;
        mouseY = inside ? (int) Math.floor(mapTop + (mouseY - mapTop) / thaumonomiconui$zoom) : Integer.MIN_VALUE / 2;
'''
method = method.replace(anchor, transform + '\n' + anchor, 1)
method = method.replace('relativeX > 224 || relativeY > 196', 'relativeX > VIEW_WIDTH / thaumonomiconui$zoom || relativeY > VIEW_HEIGHT / thaumonomiconui$zoom')
method = method.replace('mouseX < mapLeft + VIEW_WIDTH && mouseY < mapTop + VIEW_HEIGHT\n', 'mouseX < mapLeft + VIEW_WIDTH / thaumonomiconui$zoom && mouseY < mapTop + VIEW_HEIGHT / thaumonomiconui$zoom\n')
method = method.replace('        graphics.disableScissor();', '        graphics.pose().popPose();\n        graphics.disableScissor();')
method = method.replace('ResearchIcons.drawFallback(', 'ResearchIconsAccess.thaumonomiconui$drawFallback(')
method = method.replace('GuiBlit.blit(graphics, TEXTURE, x - 2, y - 2, 26, 26, frameU, 230, 26, 26, 256, 256, tint);', 'local.thaumonomiconui.ResearchFrames.draw(graphics, frameU, x - 2, y - 2, tint);')
method = method.replace('GuiBlit.blit(graphics, TEXTURE, x - 2, y - 2, 26, 26, 26, 230, 26, 26, 256, 256, tint);', 'local.thaumonomiconui.ResearchFrames.draw(graphics, 26, x - 2, y - 2, tint);')
header = '''package local.thaumonomiconui.mixin;
'''+imports+'''
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
'''
target = root/'src/main/java/local/thaumonomiconui/mixin/ResearchZoomMixin.java'
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(header + method + '\n}\n', encoding='utf8')
