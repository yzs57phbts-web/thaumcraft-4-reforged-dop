package thaumcraft.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.research.ScanClientHooks;

public class ThaumometerItem extends Item {
    public ThaumometerItem(Properties properties) {
        super(properties);
    }

    public static Properties properties(Properties properties) {
        return properties.stacksTo(1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = handleUse(level, player, hand);
        return InteractionResults.holder(result, player.getItemInHand(hand), level);
    }

    private InteractionResult handleUse(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (level.isClientSide()) {
            ScanClientHooks.useTick(level, entity, stack, ticksRemaining);
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int remainingTime) {
        if (entity.level().isClientSide()) {
            ScanClientHooks.stopUsing(entity);
        }
    }
}
