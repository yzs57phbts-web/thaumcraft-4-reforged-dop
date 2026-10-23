package thaumcraft.entity.monster.taint;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.effect.EffectHooks;
import thaumcraft.entity.TaintedMob;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.taint.TaintBiomes;

public class TaintedCreeper extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIRECTION = SynchedEntityData.defineId(TaintedCreeper.class, EntityDataSerializers.INT);
    private int taintFuse;
    private int oldTaintFuse;

    public TaintedCreeper(EntityType<? extends TaintedCreeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_SWELL_DIRECTION, -1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TaintSwellGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.oldTaintFuse = this.taintFuse;
            int direction = this.getSwellDirection();
            if (direction > 0 && this.taintFuse == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            }
            this.taintFuse += direction;
            if (this.taintFuse < 0) {
                this.taintFuse = 0;
            }
            if (this.taintFuse >= 30) {
                this.taintFuse = 30;
                if (!this.level().isClientSide()) {
                    ServerLevel level = (ServerLevel) this.level();
                    level.explode(this, this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), 1.5F, false, Level.ExplosionInteraction.NONE);
                    this.infectExplosionArea(level);
                    this.discard();
                }
            }
        }
        super.tick();
    }

    public float getSwelling(float partialTick) {
        return (this.oldTaintFuse + (this.taintFuse - this.oldTaintFuse) * partialTick) / 28.0F;
    }

    public int getSwellDirection() {
        return this.entityData.get(DATA_SWELL_DIRECTION);
    }

    public void setSwellDirection(int direction) {
        this.entityData.set(DATA_SWELL_DIRECTION, direction);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageModifier, DamageSource damageSource) {
        boolean damaged = super.causeFallDamage(fallDistance, damageModifier, damageSource);
        this.taintFuse += (int) (fallDistance * 1.5);
        if (this.taintFuse > 25) {
            this.taintFuse = 25;
        }
        return damaged;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        thaumcraft.compat.ValueOutput output = thaumcraft.compat.ValueOutput.of(tag, this.registryAccess());
        super.addAdditionalSaveData(tag);
        output.putInt("TaintFuse", this.taintFuse);
        output.putInt("TaintSwellDirection", this.getSwellDirection());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        thaumcraft.compat.ValueInput input = thaumcraft.compat.ValueInput.of(tag, this.registryAccess());
        super.readAdditionalSaveData(tag);
        this.taintFuse = input.getIntOr("TaintFuse", 0);
        this.oldTaintFuse = this.taintFuse;
        this.entityData.set(DATA_SWELL_DIRECTION, input.getIntOr("TaintSwellDirection", -1));
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(level.getRandom().nextBoolean() ? ModItems.TAINTED_GOO.get() : ModItems.TAINT_TENDRIL.get()));
    }

    private void infectExplosionArea(ServerLevel level) {
        AABB area = new AABB(this.blockPosition()).inflate(6.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target != this && !(target instanceof TaintedMob) && !target.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                EffectHooks.applyTaintPoison(target, 100, 0);
            }
        }
        int x = (int) this.getX();
        int y = (int) this.getY();
        int z = (int) this.getZ();
        for (int index = 0; index < 10; index++) {
            int targetX = x + (int) ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 5.0F);
            int targetZ = z + (int) ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 5.0F);
            BlockPos target = new BlockPos(targetX, y, targetZ);
            if (!level.getRandom().nextBoolean() || TaintBiomes.isTainted(level, target)) {
                continue;
            }
            TaintBiomes.set(level, target, TaintBiomes.TAINTED_LAND);
            BlockPos below = target.below();
            if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && level.getBlockState(target).canBeReplaced()) {
                level.setBlockAndUpdate(target, ModBlocks.FIBROUS_TAINT.get().defaultBlockState());
            }
        }
    }

    private static final class TaintSwellGoal extends Goal {
        private final TaintedCreeper creeper;
        @Nullable
        private LivingEntity target;

        private TaintSwellGoal(TaintedCreeper creeper) {
            this.creeper = creeper;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.creeper.getTarget();
            return this.creeper.getSwellDirection() > 0 || target != null && !target.isDeadOrDying() && this.creeper.distanceToSqr(target) < 9.0;
        }

        @Override
        public void start() {
            this.creeper.getNavigation().stop();
            this.target = this.creeper.getTarget();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.target != null && !this.target.isDeadOrDying()) {
                if (this.creeper.distanceToSqr(this.target) > 49.0 || !this.creeper.getSensing().hasLineOfSight(this.target)) {
                    this.creeper.setSwellDirection(-1);
                } else {
                    this.creeper.setSwellDirection(1);
                }
            } else {
                this.creeper.setSwellDirection(-1);
            }
        }
    }
}
