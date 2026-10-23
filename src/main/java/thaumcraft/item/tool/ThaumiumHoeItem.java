package thaumcraft.item.tool;

import net.minecraft.world.item.HoeItem;
import thaumcraft.platform.Registration;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.item.gear.Repairable;

public class ThaumiumHoeItem extends HoeItem implements Repairable {
    public ThaumiumHoeItem(Properties properties) {
        super(Registration.tier(TcMaterials.THAUMIUM_TOOL), properties);
    }
}
