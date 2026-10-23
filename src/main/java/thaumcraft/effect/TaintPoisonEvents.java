package thaumcraft.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.AlchemyHooks;
import thaumcraft.entity.TaintedMob;
import thaumcraft.entity.monster.taint.TaintMobFactory;
import thaumcraft.registry.ModEffects;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class TaintPoisonEvents {
    private TaintPoisonEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity original = event.getEntity();
        if (!(original.level() instanceof ServerLevel level) || original instanceof TaintedMob || !original.hasEffect(ModEffects.TAINT_POISON)) {
            return;
        }
        Entity replacement = TaintMobFactory.createReplacement(original);
        if (replacement != null) {
            if (level.addFreshEntity(replacement)) {
                original.discard();
                return;
            }
            return;
        }
        int size = (int) (1.0F + Math.min(original.getMaxHealth() / 10.0F, 6.0F));
        if (AlchemyHooks.spawnThaumicSlime(level, new Vec3(original.getX(), original.getY(), original.getZ()), size)) {
            original.discard();
        }
    }
}
