package thaumcraft.item.armor;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import thaumcraft.item.InteractionResults;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.TcArmorMaterial;
import thaumcraft.platform.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.item.AspectTooltips;
import thaumcraft.item.gear.Repairable;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.menu.HoverHarnessMenu;
import thaumcraft.platform.Menus;
import thaumcraft.wand.VisDiscountGear;

public class HoverHarnessItem extends ArmorItem implements Repairable, VisDiscountGear, RunicArmor {
    public HoverHarnessItem(TcArmorMaterial material, ArmorSlot slot, Properties properties) {
        super(Registration.armorMaterial(material), Registration.armorType(slot), properties);
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return aspect == Aspect.AIR ? 5 : 2;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResults.holder(handleUse(level, player, hand), player.getItemInHand(hand), level);
    }

    private InteractionResult handleUse(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Menus.open(serverPlayer, new SimpleMenuProvider((containerId, inventory, opener) -> new HoverHarnessMenu(containerId, inventory), player.getItemInHand(hand).getHoverName()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, builder, flag);
        ItemStack jar = Hover.getJar(stack);
        if (!jar.isEmpty()) {
            AspectList aspects = AspectKeys.ASPECTS.getOrDefault(jar, AspectList.EMPTY);
            for (Aspect aspect : aspects.getAspectsSorted()) {
                if (AspectTooltips.isDiscovered(aspect)) {
                    builder.add(Component.literal(aspect.getName() + " x " + aspects.getAmount(aspect)));
                } else {
                    builder.add(Component.translatable("tc.aspect.unknown"));
                }
            }
        }
        ArmorSupport.visDiscountLine(builder, getVisDiscount(stack, null, null));
        ArmorSupport.visDiscountLine(builder, " (Aer)", getVisDiscount(stack, null, Aspect.AIR));
    }
}
