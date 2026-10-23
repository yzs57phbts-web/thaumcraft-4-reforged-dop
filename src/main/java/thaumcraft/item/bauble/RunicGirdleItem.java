package thaumcraft.item.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.item.gear.RunicShield;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;

public class RunicGirdleItem extends Item implements Bauble, RunicArmor {
    private final int variant;

    public RunicGirdleItem(Properties properties, int variant) {
        super(properties);
        this.variant = variant;
    }

    public int getVariant() {
        return variant;
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.BELT;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return variant == 0 ? 10 : 9;
    }

    @Override
    public void onEquipped(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player) {
            RunicShield.markDirty(player);
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player) {
            RunicShield.markDirty(player);
        }
    }
}
