package local.thaumonomiconui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import mezz.jei.api.*;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IJeiRuntime;
@JeiPlugin
public final class JeiBookPlugin implements IModPlugin {
 public static IJeiRuntime runtime;
 @Override public ResourceLocation getPluginUid(){return ResourceLocation.fromNamespaceAndPath("thaumonomiconui","side_book");}
 @Override public void registerGuiHandlers(IGuiHandlerRegistration r){r.addGenericGuiContainerHandler(AbstractContainerScreen.class,new IGuiContainerHandler<AbstractContainerScreen<?>>(){
  @Override public java.util.List<net.minecraft.client.renderer.Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> owner){return BookPanels.extraAreas(owner);}
 });}
 @Override public void onRuntimeAvailable(IJeiRuntime r){runtime=r;}
 @Override public void onRuntimeUnavailable(){runtime=null;}
}
