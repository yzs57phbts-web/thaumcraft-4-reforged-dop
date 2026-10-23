package thaumcraft.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.gear.Goggles;
import thaumcraft.item.gear.Repairable;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.node.Revealer;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.Registration;
import thaumcraft.platform.TcArmorMaterial;
import thaumcraft.wand.VisDiscountGear;

public class GogglesItem extends ArmorItem implements Revealer, VisDiscountGear, Goggles, Repairable, RunicArmor {
    public static final TcArmorMaterial MATERIAL = new TcArmorMaterial(
        25, 1, 3, 2, 1, 25, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, Tags.Items.INGOTS_GOLD, "goggles"
    );

    public GogglesItem(Properties properties) {
        super(Registration.armorMaterial(MATERIAL), Registration.armorType(ArmorSlot.HELMET), properties);
    }

    public static Properties properties(Properties properties) {
        return Registration.armor(properties.rarity(Rarity.RARE), MATERIAL, ArmorSlot.HELMET).durability(350);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tc.visdiscount").append(": " + getVisDiscount(stack, null, null) + "%").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return 5;
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
}
