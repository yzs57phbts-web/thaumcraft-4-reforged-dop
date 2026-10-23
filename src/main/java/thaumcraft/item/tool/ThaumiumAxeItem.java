package thaumcraft.item.tool;

import net.minecraft.world.item.AxeItem;
import thaumcraft.platform.Registration;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.item.gear.Repairable;

public class ThaumiumAxeItem extends AxeItem implements Repairable {
    public ThaumiumAxeItem(Properties properties) {
        super(Registration.tier(TcMaterials.THAUMIUM_TOOL), properties);
    }
}
