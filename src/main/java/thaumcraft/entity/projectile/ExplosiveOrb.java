package thaumcraft.entity.projectile;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import thaumcraft.compat.ValueInput;
import net.minecraft.nbt.CompoundTag;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entity.CombatUtils;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModEntities;

public class ExplosiveOrb extends ThrowableProjectile {
    public float strength = 1.0F;
    public boolean onFire;

    public ExplosiveOrb(EntityType<? extends ExplosiveOrb> type, Level level) {
        super(type, level);
    }

    public static ExplosiveOrb create(Level level, LivingEntity shooter) {
        ExplosiveOrb orb = new ExplosiveOrb(ModEntities.EXPLOSIVE_ORB.get(), level);
        CombatUtils.launch(orb, shooter, 1.5F);
        return orb;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    protected void onHit(HitResult hit) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (hit instanceof EntityHitResult entityHit) {
            Entity owner = this.getOwner();
            DamageSource source = level.damageSources().source(owner == null ? DamageTypes.UNATTRIBUTED_FIREBALL : DamageTypes.FIREBALL, this, owner);
            entityHit.getEntity().hurt(source, this.strength * 1.5F);
        }
        level.explode(null, this.getX(), this.getY(), this.getZ(), this.strength, this.onFire, Level.ExplosionInteraction.BLOCK);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Fx.get().fireOrbTrail(
                this.level(),
                this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
                this.yo + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
                this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.3F,
                2.0F + this.random.nextFloat()
            );
        } else if (this.tickCount > 500) {
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        this.markHurt();
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        Vec3 look = attacker.getLookAngle();
        this.setDeltaMovement(look.scale(0.9));
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putFloat("strength", this.strength);
        output.putBoolean("onFire", this.onFire);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.strength = input.getFloatOr("strength", 1.0F);
        this.onFire = input.getBooleanOr("onFire", false);
    }
}
