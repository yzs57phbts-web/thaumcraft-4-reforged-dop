package thaumcraft.client.jei;

import java.util.List;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.aspect.AspectRenderer;

final class AspectStrip {
    static final int SIZE = 18;

    private AspectStrip() {
    }

    static int rows(AspectList aspects, int perRow) {
        return aspects.size() == 0 ? 0 : (aspects.size() + perRow - 1) / perRow;
    }

    private static int offset(int count, int perRow, int width) {
        int inRow = Math.min(count, perRow);
        return (width - inRow * SIZE) / 2;
    }

    static void draw(GuiGraphics graphics, AspectList aspects, int perRow, int width, int y, int multiplier) {
        List<Aspect> sorted = aspects.getAspectsSorted();
        int shift = offset(sorted.size(), perRow, width);
        for (int index = 0; index < sorted.size(); index++) {
            Aspect aspect = sorted.get(index);
            AspectRenderer.drawTag(graphics, shift + index % perRow * SIZE, y + index / perRow * SIZE, aspect, aspects.getAmount(aspect) * multiplier);
        }
    }

    static void tooltip(ITooltipBuilder tooltip, AspectList aspects, int perRow, int width, int y, double mouseX, double mouseY) {
        List<Aspect> sorted = aspects.getAspectsSorted();
        int shift = offset(sorted.size(), perRow, width);
        for (int index = 0; index < sorted.size(); index++) {
            int x = shift + index % perRow * SIZE;
            int top = y + index / perRow * SIZE;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= top && mouseY < top + 16) {
                tooltip.add(Component.literal(sorted.get(index).getName()));
                tooltip.add(sorted.get(index).getDescription());
                return;
            }
        }
    }
}
