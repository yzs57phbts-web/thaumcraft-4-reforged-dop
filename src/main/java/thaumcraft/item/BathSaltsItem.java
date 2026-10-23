package thaumcraft.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BathSaltsItem extends Item {
    public BathSaltsItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return 200;
    }
}
