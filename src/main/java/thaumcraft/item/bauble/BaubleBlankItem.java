package thaumcraft.item.bauble;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.wand.VisDiscountGear;

public class BaubleBlankItem extends Item implements Bauble, VisDiscountGear, RunicArmor {
    private final int kind;

    public BaubleBlankItem(Properties properties, int kind) {
        super(properties);
        this.kind = kind;
    }

    public int getKind() {
        return kind;
    }

    public @Nullable Aspect getAspect() {
        return kind >= 3 && kind <= 8 ? Aspect.getPrimalAspects().get(kind - 3) : null;
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return kind == 0 ? BaubleType.AMULET : kind == 2 ? BaubleType.BELT : BaubleType.RING;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, @Nullable Aspect aspect) {
        Aspect own = getAspect();
        return own != null && own == aspect ? 1 : 0;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        Aspect aspect = getAspect();
        if (aspect != null) {
            builder.add(Component.literal(aspect.getName() + " ").append(Component.translatable("tc.discount")).append(": 1%").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
