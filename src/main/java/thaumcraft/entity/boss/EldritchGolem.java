package thaumcraft.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import thaumcraft.entity.ai.LongRangeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.entity.projectile.GolemOrb;
import thaumcraft.registry.ModSounds;

public class EldritchGolem extends ThaumcraftBoss implements RangedAttackMob, thaumcraft.entity.EldritchMob {
    private static final EntityDataAccessor<Boolean> DATA_HEADLESS = SynchedEntityData.defineId(EldritchGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BEAM_CHARGE = SynchedEntityData.defineId(EldritchGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING_BEAM = SynchedEntityData.defineId(EldritchGolem.class, EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.resources.ResourceLocation PHASE_DAMAGE = thaumcraft.Thaumcraft.id("golem_second_phase");
    private int attackTimer;
    private LongRangeAttackGoal rangedGoal;
    private int arcTicks;
    private int arcX;
    private int arcY;
    private int arcZ;

    public EldritchGolem(EntityType<? extends EldritchGolem> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(1.75F, 3.5F).withEyeHeight(this.isHeadless() ? 3.33F : 3.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 6.0).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HEADLESS, false);
        builder.define(DATA_BEAM_CHARGE, 0);
        builder.define(DATA_CHARGING_BEAM, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.rangedGoal = new LongRangeAttackGoal(this, 3.0, 1.0, 5, 5, 24.0F);
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isHeadless() {
        return this.entityData.get(DATA_HEADLESS);
    }

    public void setHeadless(boolean headless) {
        if (this.rangedGoal != null) {
            this.goalSelector.removeGoal(this.rangedGoal);
            if (headless) this.goalSelector.addGoal(2, this.rangedGoal);
        }
        this.entityData.set(DATA_HEADLESS, headless);
        if (!this.level().isClientSide()) {
            var damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.removeModifier(PHASE_DAMAGE);
                if (headless) damage.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    PHASE_DAMAGE, 1.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
        if (headless) {
            this.setBeamCharge(0);
        }
    }

    public int getBeamCharge() {
        return this.entityData.get(DATA_BEAM_CHARGE);
    }

    public void setBeamCharge(int charge) {
        this.entityData.set(DATA_BEAM_CHARGE, charge);
    }

    public int getAttackTimer() {
        return this.attackTimer;
    }

    public boolean isChargingBeam() {
        return this.entityData.get(DATA_CHARGING_BEAM);
    }

    public int getArcTicks() {
        return this.arcTicks;
    }

    public net.minecraft.core.BlockPos getArcTarget() {
        return new net.minecraft.core.BlockPos(this.arcX, this.arcY, this.arcZ);
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType reason, net.minecraft.world.entity.SpawnGroupData data) {
        net.minecraft.world.entity.SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        this.setSpawnTimer(100);
        return result;
    }

    @Override
    public void tick() {
        if (this.getSpawnTimer() > 0) {
            this.heal(2.0F);
        }
        super.tick();
        if (this.attackTimer > 0) {
            this.attackTimer--;
        }
        if (this.level().isClientSide()) {
            if (this.arcTicks > 0) {
                this.arcTicks--;
            }
            return;
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() > 2.5000003E-7 && this.random.nextInt(5) == 0) {
            BlockPos under = this.blockPosition().below();
            if (this.level().getBlockState(under).getBlock() instanceof thaumcraft.block.LootBlock && this.level() instanceof net.minecraft.server.level.ServerLevel level) {
                level.destroyBlock(under, true, this);
            }
        }
        if (this.level() instanceof net.minecraft.server.level.ServerLevel level) {
            BlockPos ahead = BlockPos.containing(this.getX() + this.getDeltaMovement().x, this.getBoundingBox().minY, this.getZ() + this.getDeltaMovement().z);
            float hardness = this.level().getBlockState(ahead).getDestroySpeed(this.level(), ahead);
            if (hardness >= 0.0F && hardness <= 0.15F) {
                level.destroyBlock(ahead, true, this);
            }
        }
        if (this.isHeadless() && this.getBeamCharge() <= 0) {
            this.entityData.set(DATA_CHARGING_BEAM, true);
        }
        if (this.isHeadless() && this.isChargingBeam()) {
            int charge = Math.min(150, this.getBeamCharge() + 1);
            this.setBeamCharge(charge);
            this.level().broadcastEntityEvent(this, (byte) 19);
            if (charge == 150) {
                this.entityData.set(DATA_CHARGING_BEAM, false);
            }
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float damage) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (damage >= this.getHealth() && !this.isHeadless()) {
            this.setHeadless(true);
            this.setSpawnTimer(100);
            double yaw = Math.toRadians(this.getYRot());
            level.explode(this, this.getX() - Math.sin(yaw) * 0.75, this.getY() + this.getEyeHeight(), this.getZ() + Math.cos(yaw) * 0.75, 2.0F, Level.ExplosionInteraction.NONE);
            return false;
        }
        return super.hurt(source, damage);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        ServerLevel level = (ServerLevel) level();
        if (this.attackTimer > 0) {
            return false;
        }
        this.attackTimer = 10;
        boolean hit = target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F);
        if (hit) {
            target.push(0.0, 0.2, 0.0);
            if (this.isHeadless()) {
                target.push(-Math.sin(Math.toRadians(this.getYRot())) * 1.5, 0.1, Math.cos(Math.toRadians(this.getYRot())) * 1.5);
            }
            this.level().broadcastEntityEvent(this, (byte) 4);
        }
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel level) || !this.hasLineOfSight(target) || !this.isHeadless() || this.isChargingBeam() || this.getBeamCharge() <= 0) {
            return;
        }
        this.setBeamCharge(Math.max(0, this.getBeamCharge() - 15 - this.random.nextInt(5)));
        level.addFreshEntity(GolemOrb.create(level, this, target, false));
        this.playSound(ModSounds.ELDRITCH_GUARDIAN_ATTACK.get(), 1.0F, 1.0F + this.random.nextFloat() * 0.1F);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) {
            this.attackTimer = 10;
        } else if (event == 19 && this.level().isClientSide()) {
            if (this.arcTicks == 0) {
                double radius = 2.0 + this.random.nextFloat() * 2.0;
                double radians = Math.toRadians(this.random.nextInt(360));
                this.arcX = (int) Math.floor(this.getX() + radius * Math.cos(radians));
                this.arcZ = (int) Math.floor(this.getZ() + radius * Math.sin(radians));
                this.arcY = this.getBlockY();
                for (int count = 0; count < 5 && this.level().isEmptyBlock(new net.minecraft.core.BlockPos(this.arcX, this.arcY, this.arcZ)); count++) {
                    this.arcY--;
                }
                if (!this.level().isEmptyBlock(new net.minecraft.core.BlockPos(this.arcX, this.arcY, this.arcZ)) && this.level().isEmptyBlock(new net.minecraft.core.BlockPos(this.arcX, this.arcY + 1, this.arcZ))) {
                    this.arcTicks = 8 + this.random.nextInt(5);
                }
            }
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        thaumcraft.compat.ValueOutput output = thaumcraft.compat.ValueOutput.of(tag, registryAccess());
        super.addAdditionalSaveData(tag);
        output.putBoolean("Headless", this.isHeadless());
        output.putInt("BeamCharge", this.getBeamCharge());
        output.putBoolean("ChargingBeam", this.isChargingBeam());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        thaumcraft.compat.ValueInput input = thaumcraft.compat.ValueInput.of(tag, registryAccess());
        super.readAdditionalSaveData(tag);
        this.setHeadless(input.getBooleanOr("Headless", false));
        this.setBeamCharge(input.getIntOr("BeamCharge", 0));
        this.entityData.set(DATA_CHARGING_BEAM, input.getBooleanOr("ChargingBeam", false));
    }
}
