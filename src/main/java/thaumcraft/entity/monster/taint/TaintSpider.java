package thaumcraft.entity.monster.taint;

import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import thaumcraft.entity.TaintedMob;
import thaumcraft.registry.ModItems;

public class TaintSpider extends Spider implements TaintedMob {
    public TaintSpider(EntityType<? extends TaintSpider> type, Level level) {
        super(type, level);
        this.xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.monster.Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 5.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 12.0)
            .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    public float getVoicePitch() {
        return 0.7F;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (level.getRandom().nextInt(6) == 0) {
            this.spawnAtLocation(level.getRandom().nextBoolean() ? ModItems.TAINTED_GOO.get() : ModItems.TAINT_TENDRIL.get());
        }
    }
}
