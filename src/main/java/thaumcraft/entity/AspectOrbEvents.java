package thaumcraft.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.research.ScanManager;
import thaumcraft.entity.TaintedMob;
import thaumcraft.registry.ModEffects;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AspectOrbEvents {
    private AspectOrbEvents() {}

    @SubscribeEvent
    static void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide() || entity.lastHurtByPlayer == null || entity.lastHurtByPlayerTime <= 0 || (!(entity instanceof TaintedMob) && entity.hasEffect(ModEffects.TAINT_POISON))) return;
        AspectList aspects = ScanManager.entityAspects(entity);
        if (aspects == null || aspects.isEmpty()) return;
        AspectList primal = AspectHelper.reduceToPrimals(aspects, true);
        var random = entity.getRandom();
        for (Aspect aspect : primal.getAspects()) {
            if (random.nextBoolean()) {
                int amount = 1 + random.nextInt(Math.max(1, primal.getAmount(aspect)));
                AspectOrb orb = AspectOrb.create(entity.level(), entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), aspect, amount);
                if (orb != null) entity.level().addFreshEntity(orb);
            }
        }
    }
}
