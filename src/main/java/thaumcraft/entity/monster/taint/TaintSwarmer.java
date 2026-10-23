package thaumcraft.entity.monster.taint;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModSounds;

public class TaintSwarmer extends TaintSpore {
    private int spawnCounter = 500;
    private boolean burst;

    public TaintSwarmer(EntityType<? extends TaintSwarmer> type, Level level) {
        super(type, level);
        this.setSporeSize(10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.monster.Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 75.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, net.minecraft.world.entity.MobSpawnType reason) {
        // Surface taint is active in daylight; Monster's pathfinding light
        // preference must not become a second, implicit spawn prohibition.
        return level instanceof net.minecraft.world.level.ServerLevelAccessor server
            && TaintSpawnRules.surface(server, this.blockPosition());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.spawnCounter > 0) {
                this.spawnCounter--;
            }
            if (this.spawnCounter <= 0 && this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0,
                entity -> entity instanceof Player player && player.isAlive() && !player.getAbilities().invulnerable
                    && !player.isCreative() && !player.isSpectator()) != null) {
                this.spawnCounter = 500;
            }
            Fx.get().taintSwarm(this.level(), this, (500 - this.spawnCounter) / 25, 0.1F, 10.0F, 0.0F);
            return;
        }
        if (this.spawnCounter > 0) {
            this.spawnCounter--;
        }
        if (this.spawnCounter <= 0 && this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0,
            entity -> entity instanceof Player player && player.isAlive() && !player.getAbilities().invulnerable
                && !player.isCreative() && !player.isSpectator()) != null) {
            this.spawnCounter = 500;
            if (this.level().getEntitiesOfClass(TaintSwarm.class, this.getBoundingBox().inflate(24.0, 12.0, 24.0)).size() < 3) {
                this.spawnSwarm(1);
            }
        }
    }

    @Override
    protected boolean shouldBurstOnDetachment() {
        return false;
    }

    @Override
    protected void burstIntoSpiders() {
    }

    @Override
    public void playerTouch(Player player) {
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (!this.level().isClientSide() && !this.burst) {
            this.burst = true;
            this.spawnSwarm(1);
        }
        super.die(source);
    }

    private void spawnSwarm(int count) {
        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) this.level();
        level.playSound(null, this.blockPosition(), ModSounds.GORE.get(), SoundSource.HOSTILE, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.1F);
        for (int index = 0; index < count; index++) {
            TaintSwarm swarm = ModEntities.TAINT_SWARM.get().create(level);
            if (swarm != null) {
                swarm.absMoveTo(this.getX(), this.getY() + 0.5, this.getZ(), level.getRandom().nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(swarm);
            }
        }
    }
}
