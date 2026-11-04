package local.thaumonomiconui.mixin;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=AbstractContainerScreen.class,remap=false)
public interface ContainerGeometry {
 @Accessor("leftPos") void thaumonomiconui$left(int value);
 @Accessor("topPos") void thaumonomiconui$top(int value);
}
