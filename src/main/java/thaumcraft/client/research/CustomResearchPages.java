package thaumcraft.client.research;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.research.ResearchPage;

/** Rendering extension for addon recipe types that cannot be represented by an ordinary item infusion. */
public final class CustomResearchPages {
    @FunctionalInterface public interface Renderer { void draw(GuiGraphics graphics, String data, int x, int y, int mouseX, int mouseY); }
    private static final Map<ResourceLocation,Renderer> RENDERERS=new HashMap<>();
    public static void register(ResourceLocation type,Renderer renderer){RENDERERS.put(type,renderer);}
    static void draw(ResearchPage.Custom page,GuiGraphics graphics,int x,int y,int mouseX,int mouseY){Renderer renderer=RENDERERS.get(page.type());if(renderer!=null)renderer.draw(graphics,page.data(),x,y,mouseX,mouseY);}
}
