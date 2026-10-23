package thaumcraft.client.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import thaumcraft.crafting.TcIngredient;

final class InfusionLayout {
    static final int WIDTH = 130;
    static final int HEIGHT = 102 + 18 * 3 + 12;
    static final int CENTER_X = 49;
    static final int CENTER_Y = 49;
    static final int PER_ROW = 5;
    static final int ASPECTS_Y = 102;

    private InfusionLayout() {
    }

    static void components(IRecipeLayoutBuilder builder, String key, List<TcIngredient> components) {
        var points=thaumcraft.crafting.InfusionDiagram.coordinates(key,components.size());
        for(int i=0;i<components.size();i++)TcJei.accept(builder.addInputSlot(CENTER_X+points.get(i)[0]-8,CENTER_Y+points.get(i)[1]-8).setStandardSlotBackground(),components.get(i));
    }

    static void instability(GuiGraphics graphics, int instability) {
        Language language = Language.getInstance();
        int level = Math.min(5, instability / 2);
        Component text = Component.literal(language.getOrDefault("tc.inst") + " " + language.getOrDefault("tc.inst." + level));
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, text, (WIDTH - font.width(text)) / 2, HEIGHT - 10, 0xFF404040, false);
    }
}
