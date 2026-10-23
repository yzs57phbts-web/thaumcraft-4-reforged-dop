package thaumcraft.item.armor;

import net.minecraft.world.entity.player.Player;
import java.util.Map;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.TcArmorMaterial;
import thaumcraft.platform.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.infusion.InfusionKeys;
import thaumcraft.item.gear.Goggles;
import thaumcraft.item.gear.Repairable;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.node.Revealer;

public class FortressArmorItem extends ArmorItem implements Repairable, RunicArmor, Goggles, Revealer {
    public static final String GOGGLES_TAG = "goggles";
    public static final String MASK_TAG = "mask";

    public FortressArmorItem(TcArmorMaterial material, ArmorSlot slot, Properties properties) {
        super(Registration.armorMaterial(material), Registration.armorType(slot), properties);
    }

    public static boolean hasGoggles(ItemStack stack) {
        return InfusionKeys.TAGS.getOrDefault(stack, Map.of()).containsKey(GOGGLES_TAG);
    }

    public static boolean hasMask(ItemStack stack) {
        return getMask(stack) >= 0;
    }

    public static int getMask(ItemStack stack) {
        String value = InfusionKeys.TAGS.getOrDefault(stack, Map.of()).get(MASK_TAG);
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    public static int getMask(LivingEntity entity) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.getItem() instanceof FortressArmorItem ? getMask(helmet) : -1;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity entity) {
        return hasGoggles(stack);
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity entity) {
        return hasGoggles(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        if (hasGoggles(stack)) {
            builder.add(Component.translatable("item.thaumcraft.goggles_of_revealing").withStyle(ChatFormatting.DARK_PURPLE));
        }
        int mask = getMask(stack);
        if (mask >= 0) {
            builder.add(Component.translatable("item.HelmetFortress.mask." + mask).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, builder, flag);
    }
}
