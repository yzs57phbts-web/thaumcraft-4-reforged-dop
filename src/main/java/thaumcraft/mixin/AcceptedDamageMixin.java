package thaumcraft.mixin;

import java.util.Stack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.item.armor.ArmorEvents;
import thaumcraft.item.gear.RunicShield;

/** Both classes define actuallyHurt; Player's override does not call the base method. */
@Mixin({LivingEntity.class, Player.class})
public abstract class AcceptedDamageMixin {
    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void thaumcraft$acceptedHit(DamageSource source, float amount, CallbackInfo callback) {
        LivingEntity victim = (LivingEntity) (Object) this;
        Stack<DamageContainer> containers = ((DamageContainerAccess) victim).thaumcraft$damageContainers();
        if (victim.level().isClientSide() || victim.isInvulnerableTo(source) || containers.empty()) {
            return;
        }
        DamageContainer container = containers.peek();
        float acceptedDamage = container.getNewDamage();
        ArmorEvents.onAcceptedDamage(victim, source, acceptedDamage);
        if (victim instanceof ServerPlayer player) {
            // Keep lastHurt untouched: it must remember the original accepted hit.
            // Subsequent vanilla armor damage and enchantments see only the remainder.
            container.setNewDamage(RunicShield.absorbAcceptedDamage(player, source, acceptedDamage));
        }
    }
}
