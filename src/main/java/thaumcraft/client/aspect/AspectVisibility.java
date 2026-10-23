package thaumcraft.client.aspect;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ScanKind;
import thaumcraft.research.ScanManager;

public final class AspectVisibility {
    private AspectVisibility() {
    }

    public static boolean isScanned(ItemStack stack) {
        Player player = Minecraft.getInstance().player;
        return player != null && !stack.isEmpty()
            && (player.isCreative() || Knowledge.get(player).isScanned(ScanKind.OBJECT, ScanManager.itemKey(stack)));
    }

    public static boolean isKnown(Aspect aspect) {
        Player player = Minecraft.getInstance().player;
        return aspect.isEnabled() && player != null && (player.isCreative() || Knowledge.get(player).hasDiscoveredAspect(aspect));
    }
}
