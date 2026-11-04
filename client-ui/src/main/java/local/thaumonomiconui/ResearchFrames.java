package local.thaumonomiconui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.platform.GuiBlit;

public final class ResearchFrames {
    private ResearchFrames() {}
    public static void draw(GuiGraphics graphics,int frame,int x,int y,int tint) {
        GuiBlit.blit(graphics,ResourceLocation.fromNamespaceAndPath("thaumonomiconui","textures/gui/frame_"+frame+".png"),
            x,y,26,26,4,4,26,26,34,34,tint);
    }
}
