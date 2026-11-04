package local.thaumonomiconui.mixin;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import thaumcraft.research.ResearchItem;

@Mixin(targets = "thaumcraft.client.research.ResearchIcons", remap = false)
public interface ResearchIconsAccess {
    @Invoker("drawFallback")
    static void thaumonomiconui$drawFallback(GuiGraphics graphics, ResearchItem item, int x, int y, int tint) {
        throw new AssertionError();
    }
}
