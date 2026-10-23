package thaumcraft.client.render.armor;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.item.armor.ArmorSupport;
import thaumcraft.item.armor.CultistRobeArmorItem;
import thaumcraft.item.armor.VoidRobeArmorItem;

/** Keep the outer skin shell from intersecting the original, close-fitting robe. */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class RobeSkinLayers {
    private RobeSkinLayers() {
    }

    private static boolean covers(ItemStack stack, EquipmentSlot slot) {
        return ArmorSupport.slotOf(stack) == slot
            && (stack.getItem() instanceof CultistRobeArmorItem || stack.getItem() instanceof VoidRobeArmorItem);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforePlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity().isSpectator()) {
            return;
        }
        // PlayerRenderer has just restored these flags from this player's skin
        // settings. The next render restores them again, including other players.
        hideCoveredLayers(event.getRenderer().getModel(),
            event.getEntity().getItemBySlot(EquipmentSlot.HEAD),
            event.getEntity().getItemBySlot(EquipmentSlot.CHEST),
            event.getEntity().getItemBySlot(EquipmentSlot.LEGS));
    }

    public static void hideCoveredLayers(PlayerModel<?> model, ItemStack head, ItemStack chest, ItemStack legs) {
        if (covers(head, EquipmentSlot.HEAD)) {
            model.hat.visible = false;
        }
        if (covers(chest, EquipmentSlot.CHEST)) {
            model.jacket.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
        }
        if (covers(legs, EquipmentSlot.LEGS)) {
            model.leftPants.visible = false;
            model.rightPants.visible = false;
        }
    }
}
