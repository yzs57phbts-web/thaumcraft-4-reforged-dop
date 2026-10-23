package thaumcraft.item.tool;

import net.minecraft.world.item.SwordItem;
import thaumcraft.platform.Registration;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.item.gear.Repairable;

public class ThaumiumSwordItem extends SwordItem implements Repairable {
    public ThaumiumSwordItem(Properties properties) {
        super(Registration.tier(TcMaterials.THAUMIUM_TOOL), properties);
    }
}
