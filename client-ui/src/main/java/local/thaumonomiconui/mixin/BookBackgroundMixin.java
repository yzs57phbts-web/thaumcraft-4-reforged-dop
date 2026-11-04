package local.thaumonomiconui.mixin;

import local.thaumonomiconui.BookPanels;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Screen.class, remap=false)
public abstract class BookBackgroundMixin {
    @Inject(method="renderBackground",at=@At("HEAD"),cancellable=true)
    private void thaumonomiconui$embeddedBackground(GuiGraphics graphics,int x,int y,float tick,CallbackInfo ci) {
        if (BookPanels.isRendering((Screen)(Object)this)) ci.cancel();
    }
}
