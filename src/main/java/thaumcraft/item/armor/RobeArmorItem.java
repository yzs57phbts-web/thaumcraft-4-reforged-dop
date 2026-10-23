package thaumcraft.item.armor;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.TcArmorMaterial;
import thaumcraft.platform.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.gear.Repairable;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.wand.VisDiscountGear;

public class RobeArmorItem extends ArmorItem implements Repairable, VisDiscountGear, RunicArmor {
    public RobeArmorItem(TcArmorMaterial material, ArmorSlot slot, Properties properties) {
        super(Registration.armorMaterial(material), Registration.armorType(slot), properties);
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return ArmorSupport.slotOf(stack) == EquipmentSlot.FEET ? 1 : 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        ArmorSupport.visDiscountLine(builder, getVisDiscount(stack, null, null));
    }
}
