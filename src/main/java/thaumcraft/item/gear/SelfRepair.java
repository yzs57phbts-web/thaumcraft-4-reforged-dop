package thaumcraft.item.gear;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public final class SelfRepair {
    private SelfRepair() {
    }

    public static void tick(ItemStack stack, Entity owner) {
        if (!owner.level().isClientSide() && stack.isDamaged() && owner instanceof LivingEntity && owner.tickCount % 20 == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    public static void applyEffect(LivingEntity target, @Nullable Entity attacker, MobEffectInstance effect) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }
        if (target instanceof Player && attacker instanceof Player && !level.getServer().isPvpAllowed()) {
            return;
        }
        target.addEffect(effect);
    }
}
