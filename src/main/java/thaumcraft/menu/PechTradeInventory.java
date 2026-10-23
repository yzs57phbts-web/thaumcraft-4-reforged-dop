package thaumcraft.menu;

import net.minecraft.world.SimpleContainer;
import thaumcraft.entity.monster.Pech;

public class PechTradeInventory extends SimpleContainer {
    private final Pech pech;

    public PechTradeInventory(Pech pech) {
        super(5);
        this.pech = pech;
    }

    public Pech getPech() {
        return pech;
    }
}
