package thaumcraft.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import net.minecraft.nbt.CompoundTag;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entity.CombatUtils;
import thaumcraft.registry.ModEntities;

public class Ember extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_DURATION = SynchedEntityData.defineId(Ember.class, EntityDataSerializers.INT);

    public int firey;
    public float damage = 1.0F;

    public Ember(EntityType<? extends Ember> type, Level level) {
        super(type, level);
    }

    public static Ember create(Level level, LivingEntity shooter, float scatter) {
        Ember ember = new Ember(ModEntities.EMBER.get(), level);
        CombatUtils.launch(ember, shooter, 1.0F, scatter);
        return ember;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DURATION, 20);
    }

    public int getDuration() {
        return this.entityData.get(DATA_DURATION);
    }

    public void setDuration(int duration) {
        this.entityData.set(DATA_DURATION, duration);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.tickCount > this.getDuration()) {
            this.discard();
            return;
        }
        double scale = this.getDuration() <= 20 ? 0.95 : 0.975;
        Vec3 movement = this.getDeltaMovement().scale(scale);
        if (this.onGround()) {
            movement = movement.scale(0.66);
        }
        this.setDeltaMovement(movement);
        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Entity target = hit.getEntity();
        if (!target.fireImmune()) {
            Entity owner = this.getOwner();
            DamageSource source = level.damageSources().source(owner == null ? DamageTypes.UNATTRIBUTED_FIREBALL : DamageTypes.FIREBALL, this, owner);
            if (target.hurt(source, this.damage)) {
                target.igniteForSeconds(3 + this.firey);
            }
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.random.nextFloat() < 0.025F * this.firey) {
            BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
            if (level.isEmptyBlock(pos)) {
                BlockState fire = BaseFireBlock.getState(level, pos);
                if (fire.canSurvive(level, pos)) {
                    level.setBlockAndUpdate(pos, fire);
                }
            }
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putFloat("damage", this.damage);
        output.putInt("firey", this.firey);
        output.putInt("duration", this.getDuration());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.damage = input.getFloatOr("damage", 1.0F);
        this.firey = input.getIntOr("firey", 0);
        this.setDuration(input.getIntOr("duration", 20));
    }
}
