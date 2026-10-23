package thaumcraft.client.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.infusion.InfusionHooks;
import thaumcraft.research.Warp;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class GearTooltips {
    private GearTooltips() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        int charge = InfusionHooks.getFinalCharge(stack);
        if (charge > 0) {
            event.getToolTip().add(Component.translatable("item.runic.charge").append(" +" + charge).withStyle(ChatFormatting.GOLD));
        }
        Player player = event.getEntity();
        int warp = player == null ? 0 : Warp.getFinalWarp(stack, player);
        if (warp > 0) {
            event.getToolTip().add(Component.translatable("item.warping").append(" " + warp).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
