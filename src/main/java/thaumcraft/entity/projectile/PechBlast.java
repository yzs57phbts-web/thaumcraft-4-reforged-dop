package thaumcraft.entity.projectile;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import thaumcraft.compat.ValueInput;
import net.minecraft.nbt.CompoundTag;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.HitResult;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.CombatUtils;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModEntities;

public class PechBlast extends ThrowableProjectile {
    public int strength;
    public int duration;
    public boolean nightshade;

    public PechBlast(EntityType<? extends PechBlast> type, Level level) {
        super(type, level);
    }

    public static PechBlast create(Level level, LivingEntity shooter, int strength, int duration, boolean nightshade) {
        PechBlast blast = new PechBlast(ModEntities.PECH_BLAST.get(), level);
        blast.strength = strength;
        blast.duration = duration;
        blast.nightshade = nightshade;
        CombatUtils.launch(blast, shooter, 1.5F);
        return blast;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.025;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            Fx.get().pechBlastTrail(this.level(), this);
        }
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > 500) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        if (!(this.level() instanceof ServerLevel level)) {
            Fx.get().pechBlastBurst(this.level(), this.getX(), this.getY(), this.getZ());
            return;
        }
        Entity owner = this.getOwner();
        for (Entity entity : level.getEntities(owner, this.getBoundingBox().inflate(2.0))) {
            if (entity instanceof LivingEntity living && !isPech(entity)) {
                living.hurt(level.damageSources().thrown(this, owner), this.strength + 2);
                int time = 100 + this.duration * 40;
                if (this.nightshade) {
                    this.apply(living, MobEffects.POISON, time, this.strength);
                    this.apply(living, MobEffects.MOVEMENT_SLOWDOWN, time, this.strength + 1);
                    this.apply(living, MobEffects.WEAKNESS, time, this.strength);
                } else {
                    switch (this.random.nextInt(3)) {
                        case 0 -> this.apply(living, MobEffects.POISON, time, this.strength);
                        case 1 -> this.apply(living, MobEffects.MOVEMENT_SLOWDOWN, time, this.strength + 1);
                        default -> this.apply(living, MobEffects.WEAKNESS, time, this.strength);
                    }
                }
            }
        }
        this.discard();
    }

    private void apply(LivingEntity entity, Holder<MobEffect> effect, int time, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, time, amplifier), this);
    }

    private static boolean isPech(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(Thaumcraft.id("pech"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putInt("strength", this.strength);
        output.putInt("duration", this.duration);
        output.putBoolean("nightshade", this.nightshade);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.strength = input.getIntOr("strength", 0);
        this.duration = input.getIntOr("duration", 0);
        this.nightshade = input.getBooleanOr("nightshade", false);
    }
}
