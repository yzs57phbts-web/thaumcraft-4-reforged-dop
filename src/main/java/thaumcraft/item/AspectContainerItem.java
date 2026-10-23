package thaumcraft.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class AspectContainerItem extends Item {
    private final String separator;
    private final boolean withAmount;

    public AspectContainerItem(Properties properties, String separator, boolean withAmount) {
        super(properties);
        this.separator = separator;
        this.withAmount = withAmount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        AspectTooltips.append(stack, tooltip::add, separator, withAmount);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
