package thaumcraft.item.tool;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import thaumcraft.item.gear.Repairable;

public class BoneBowItem extends BowItem implements Repairable {
    private static final int AUTO_RELEASE = 18;

    public BoneBowItem(Properties properties) {
        super(properties);
    }

    public static float getBonePowerForTime(int timeHeld) {
        float pow = timeHeld / 10.0F;
        pow = (pow * pow + pow * 2.0F) / 3.0F;
        return Math.min(pow, 1.0F);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (getUseDuration(stack, entity) - ticksRemaining > AUTO_RELEASE) {
            entity.releaseUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack projectile = player.getProjectile(stack);
        if (projectile.isEmpty()) {
            return;
        }
        int timeHeld = EventHooks.onArrowLoose(stack, level, player, getUseDuration(stack, entity) - remainingTime, true);
        if (timeHeld < 0) {
            return;
        }
        float pow = getBonePowerForTime(timeHeld);
        if (pow < 0.1) {
            return;
        }
        List<ItemStack> fired = draw(stack, projectile, player);
        if (level instanceof ServerLevel serverLevel && !fired.isEmpty()) {
            shoot(serverLevel, player, player.getUsedItemHand(), stack, fired, pow * 2.5F, 1.0F, false, null);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + pow * 0.5F);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.setBaseDamage(2.5);
        return arrow;
    }
}
