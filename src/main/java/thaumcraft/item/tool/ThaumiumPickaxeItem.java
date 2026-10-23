package thaumcraft.item.tool;

import net.minecraft.world.item.PickaxeItem;
import thaumcraft.platform.Registration;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.item.gear.Repairable;

public class ThaumiumPickaxeItem extends PickaxeItem implements Repairable {
    public ThaumiumPickaxeItem(Properties properties) {
        super(Registration.tier(TcMaterials.THAUMIUM_TOOL), properties);
    }
}
