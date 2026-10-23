package thaumcraft.wand;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.wand.focus.FocusItem;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class WandEvents {
    private WandEvents() {
    }

    @SubscribeEvent
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (!Wands.isWand(stack)) {
            return;
        }
        FocusItem focus = Wands.getFocus(stack);
        if (focus == null || WandManager.isOnCooldown(player, stack)) {
            return;
        }
        WandManager.setCooldown(player, stack, focus.getActivationCooldown(Wands.getFocusItem(stack)));
        if (focus.onFocusBlockStartBreak(stack, event.getPos(), player)) {
            event.setCanceled(true);
        }
    }
}
