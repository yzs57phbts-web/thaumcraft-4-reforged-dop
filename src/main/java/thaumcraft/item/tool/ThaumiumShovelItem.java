package thaumcraft.item.tool;

import net.minecraft.world.item.ShovelItem;
import thaumcraft.platform.Registration;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.item.gear.Repairable;

public class ThaumiumShovelItem extends ShovelItem implements Repairable {
    public ThaumiumShovelItem(Properties properties) {
        super(Registration.tier(TcMaterials.THAUMIUM_TOOL), properties);
    }
}
