package thaumcraft.item.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;

public class HoverGirdleItem extends Item implements Bauble, RunicArmor {
    public HoverGirdleItem(Properties properties) {
        super(properties);
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.BELT;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer.fallDistance > 0.0) {
            wearer.fallDistance -= 0.33F;
        }
    }
}
