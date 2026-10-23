package thaumcraft.item.armor;

import net.minecraft.world.item.ArmorItem;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.TcArmorMaterial;
import thaumcraft.platform.Registration;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.Repairable;
import thaumcraft.item.gear.RunicArmor;

public class CultistLeaderArmorItem extends ArmorItem implements Repairable, RunicArmor {
    public CultistLeaderArmorItem(TcArmorMaterial material, ArmorSlot slot, Properties properties) {
        super(Registration.armorMaterial(material), Registration.armorType(slot), properties);
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
}
