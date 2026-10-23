package thaumcraft.item.bauble;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.research.ResearchGate;
import thaumcraft.vis.VisRelayBlockEntity;
import thaumcraft.wand.VisStorage;
import thaumcraft.wand.Wands;

public class VisAmuletItem extends Item implements Bauble, RunicArmor {
    public static final String RESEARCH = "VISAMULET";

    private final boolean stone;

    public VisAmuletItem(Properties properties, boolean stone) {
        super(properties);
        this.stone = stone;
    }

    public boolean isStone() {
        return stone;
    }

    public int getMaxVis() {
        return stone ? 2500 : 25000;
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.AMULET;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean canEquip(ItemStack stack, LivingEntity wearer) {
        return stone || wearer instanceof Player player && ResearchGate.isComplete(player, RESEARCH);
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer.level().isClientSide() || wearer.tickCount % 5 != 0 || !(wearer instanceof Player player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (Wands.isWand(held)) {
            AspectList room = Wands.getAspectsWithRoom(held);
            for (Aspect aspect : room.getAspects()) {
                int available = VisStorage.getVis(stack, aspect);
                if (available > 0) {
                    int amount = Math.min(5, Wands.getMaxVis(held) - Wands.getVis(held, aspect));
                    amount = Math.min(amount, available);
                    VisStorage.storeVis(stack, aspect, available - amount);
                    Wands.storeVis(held, aspect, Wands.getVis(held, aspect) + amount);
                }
            }
        }
        VisRelayBlockEntity relay = VisRelayBlockEntity.getNearbyRelay(player);
        if (relay == null) {
            return;
        }
        if (relay.isRemoved() || relay.getBlockPos().distToCenterSqr(player.position()) >= 26.0) {
            VisRelayBlockEntity.NEARBY_PLAYERS.remove(player.getId());
            return;
        }
        AspectList room = VisStorage.getAspectsWithRoom(stack);
        for (Aspect aspect : room.getAspects()) {
            int amount = relay.consumeVis(aspect, Math.min(5, getMaxVis() - VisStorage.getVis(stack, aspect)));
            if (amount > 0) {
                VisStorage.addRealVis(stack, aspect, amount, true);
                relay.triggerConsumeEffect(aspect);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        if (stone) {
            builder.add(Component.translatable("item.ItemAmuletVis.text").withStyle(ChatFormatting.AQUA));
        }
        builder.add(Component.translatable("item.capacity.text").append(" " + getMaxVis() / 100).withStyle(ChatFormatting.GOLD));
        DecimalFormat format = new DecimalFormat("#######.##");
        AspectList vis = VisStorage.getAllVis(stack);
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (vis.getAmount(aspect) > 0) {
                builder.add(Component.literal(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + format.format(vis.getAmount(aspect) / 100.0F)));
            }
        }
    }
}
