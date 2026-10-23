package thaumcraft.entity.projectile;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.util.Mth;
import thaumcraft.entity.CombatUtils;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModSounds;

public class GolemOrb extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID = SynchedEntityData.defineId(GolemOrb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RED = SynchedEntityData.defineId(GolemOrb.class, EntityDataSerializers.BOOLEAN);

    public GolemOrb(EntityType<? extends GolemOrb> type, Level level) {
        super(type, level);
    }

    public static GolemOrb create(ServerLevel level, LivingEntity shooter, LivingEntity target, boolean red) {
        GolemOrb orb = new GolemOrb(ModEntities.GOLEM_ORB.get(), level);
        CombatUtils.launch(orb, shooter, 0.66F, 3.0F);
        orb.setRed(red);
        orb.setTargetId(target.getId());
        return orb;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET_ID, -1);
        builder.define(DATA_RED, false);
    }

    public boolean isRed() {
        return this.entityData.get(DATA_RED);
    }

    public void setRed(boolean red) {
        this.entityData.set(DATA_RED, red);
    }

    public void setTargetId(int id) {
        this.entityData.set(DATA_TARGET_ID, id);
    }

    private net.minecraft.world.entity.Entity targetEntity() {
        int id = this.entityData.get(DATA_TARGET_ID);
        return id < 0 ? null : this.level().getEntity(id);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected void onHit(HitResult hit) {
        if (this.level() instanceof ServerLevel level && hit instanceof net.minecraft.world.phys.EntityHitResult entityHit && this.getOwner() instanceof LivingEntity owner) {
            DamageSource source = level.damageSources().indirectMagic(this, owner);
            float multiplier = this.isRed() ? 1.0F : 0.6F;
            entityHit.getEntity().hurt(source, (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier);
        }
        this.level().playSound(null, this.blockPosition(), ModSounds.SHOCK.get(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        Fx.get().golemOrbBurst(this.level(), this.getX(), this.getY(), this.getZ(), 1.0F);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > (this.isRed() ? 240 : 160)) {
            this.discard();
            return;
        }
        net.minecraft.world.entity.Entity target = this.targetEntity();
        if (target != null && target.isAlive()) {
            double distance = this.distanceToSqr(target);
            if (distance > 0.0) {
                double dx = (target.getX() - this.getX()) / distance * 0.2;
                double dy = (target.getBoundingBox().minY + target.getBbHeight() * 0.6 - this.getY()) / distance * 0.2;
                double dz = (target.getZ() - this.getZ()) / distance * 0.2;
                this.setDeltaMovement(
                    Mth.clamp(this.getDeltaMovement().x + dx, -0.25, 0.25),
                    Mth.clamp(this.getDeltaMovement().y + dy, -0.25, 0.25),
                    Mth.clamp(this.getDeltaMovement().z + dz, -0.25, 0.25)
                );
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (this.isInvulnerableTo(source) || source.getEntity() == null) {
            return false;
        }
        this.markHurt();
        this.setDeltaMovement(source.getEntity().getLookAngle().scale(0.9));
        this.playSound(ModSounds.ZAP.get(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        return true;
    }
}
