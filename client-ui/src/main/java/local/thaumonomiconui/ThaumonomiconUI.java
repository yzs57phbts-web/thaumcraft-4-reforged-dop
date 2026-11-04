package local.thaumonomiconui;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import thaumcraft.item.ThaumonomiconItem;

@Mod(value="thaumonomiconui",dist=Dist.CLIENT)
public final class ThaumonomiconUI {
 public ThaumonomiconUI(){BookPanels.register(NeoForge.EVENT_BUS);}
 public static ItemStack findBook(){
  var player=Minecraft.getInstance().player;if(player==null)return ItemStack.EMPTY;
  var inventory=player.getInventory();
  for(int i=0;i<inventory.getContainerSize();i++){var stack=inventory.getItem(i);if(stack.getItem() instanceof ThaumonomiconItem)return stack;}
  return ItemStack.EMPTY;
 }
}
