package thaumcraft.entity.monster.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.entity.TaintedMob;
import thaumcraft.damage.ModDamageTypes;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;
import thaumcraft.platform.BiomeEdit;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.taint.TaintSpread;

public class Taintacle extends Monster implements TaintedMob {
    public float flailIntensity = 1.0F;
    @Nullable
    private LivingEntity target;
    private int attackTime;

    public Taintacle(EntityType<? extends Taintacle> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, net.minecraft.world.entity.MobSpawnType reason) {
        // Surface taint is active in daylight; Monster's pathfinding light
        // preference must not become a second, implicit spawn prohibition.
        return level instanceof net.minecraft.world.level.ServerLevelAccessor server
            && TaintSpawnRules.surface(server, this.blockPosition());
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ROOTS.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TENTACLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TENTACLE.get();
    }

    @Override
    protected float getSoundVolume() {
        return this.getBbHeight() / 8.0F;
    }

    @Override
    public float getVoicePitch() {
        return 1.3F - this.getBbHeight() / 10.0F;
    }

    public static boolean canSpawnNaturally(EntityType<Taintacle> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return TaintSpawnRules.surface(level, pos)
            && level.getEntitiesOfClass(Taintacle.class, new AABB(pos).inflate(12.0, 8.0, 12.0)).isEmpty();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void travel(Vec3 input) {
        super.travel(new Vec3(0.0, Math.min(input.y, 0.0), 0.0));
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, new Vec3(0.0, Math.min(movement.y, 0.0), 0.0));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.tickCount < this.getBbHeight() * 10.0F && this.onGround()) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.random.nextDouble() * this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    0.0, 0.02, 0.0);
            }
            boolean agitated = this.tickCount > this.getBbHeight() * 10.0F
                && (this.hurtTime > 0 || this.attackTime > 0 || this.target != null && this.distanceTo(this.target) < this.getBbHeight());
            if (agitated && this.flailIntensity < 3.0F) {
                this.flailIntensity = Math.min(3.0F, this.flailIntensity + 0.2F);
            } else if (!agitated && this.flailIntensity > 1.0F) {
                this.flailIntensity = Math.max(1.0F, this.flailIntensity - 0.2F);
            }
            return;
        }
        if (this.tickCount % 20 == 0 && !TaintBiomes.isTainted(this.level(), this.blockPosition())) {
            this.hurt(this.damageSources().starve(), 1.0F);
        }
        if (this.attackTime > 0) {
            this.attackTime--;
        }
        if (this.target == null) {
            this.target = this.findTarget();
            return;
        }
        if (!this.target.isAlive() || this.target instanceof TaintedMob || !this.isAgitated()) {
            this.target = null;
            return;
        }
        this.faceTarget(this.target);
        if (this.attackTime > 0 || !this.hasLineOfSight(this.target)) {
            return;
        }
        double distance = this.distanceTo(this.target);
        if (distance <= this.getBbHeight() && verticalOverlap(this, this.target)) {
            this.attackTime = 20;
            ServerLevel level = (ServerLevel) this.level();
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            MobEffectInstance strength = this.getEffect(MobEffects.DAMAGE_BOOST);
            if (strength != null) {
                damage += 3 << strength.getAmplifier();
            }
            MobEffectInstance weakness = this.getEffect(MobEffects.WEAKNESS);
            if (weakness != null) {
                damage -= 2 << weakness.getAmplifier();
            }
            this.target.hurt(level.damageSources().source(ModDamageTypes.TENTACLE, this), damage);
            this.playSound(ModSounds.TENTACLE.get(), this.getSoundVolume(), this.getVoicePitch());
        } else if (distance > this.getBbHeight() && this.target.onGround()) {
            this.spawnTentacle(this.target);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (!(this instanceof TaintacleTiny) && source.getEntity() instanceof Entity attacker && this.distanceTo(attacker) > 16.0) {
            this.spawnTentacle(attacker);
        }
        return super.hurt(source, damage);
    }

    protected void spawnTentacle(Entity victim) {
        if (this instanceof TaintacleTiny || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = victim.blockPosition();
        boolean onTaint = TaintBiomes.isTainted(level, pos)
            && (TaintSpread.isTaintBlock(level.getBlockState(pos)) || TaintSpread.isTaintBlock(level.getBlockState(pos.below())));
        boolean outerLands = BiomeEdit.getColumnBiome(level, pos).is(TaintBiomes.OUTER_LANDS);
        if (!onTaint && !outerLands) {
            return;
        }
        this.attackTime = 40 + level.getRandom().nextInt(20);
        TaintacleTiny tiny = ModEntities.TAINTACLE_TINY.get().create(level);
        if (tiny != null) {
            tiny.absMoveTo(victim.getX() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), victim.getY(),
                victim.getZ() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), 0.0F, 0.0F);
            if (level.addFreshEntity(tiny)) {
                this.playSound(ModSounds.TENTACLE.get(), this.getSoundVolume(), this.getVoicePitch());
            }
        }
        if (outerLands && level.isEmptyBlock(pos) && hasAdjacentSolid(level, pos)) {
            TaintBiomes.set(level, pos, TaintBiomes.TAINTED_LAND);
            level.setBlockAndUpdate(pos, (level.getRandom().nextInt(4) == 0 ? ModBlocks.TAINTED_GRASS.get() : ModBlocks.FIBROUS_TAINT.get()).defaultBlockState());
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(level.getRandom().nextBoolean() ? ModItems.TAINTED_GOO.get() : ModItems.TAINT_TENDRIL.get()));
    }

    @Nullable
    private LivingEntity findTarget() {
        double radius = this.getBbHeight() * 6.0;
        AABB area = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ())
            .inflate(radius, this.getBbHeight() * 3.0, radius);
        return this.level().getEntitiesOfClass(LivingEntity.class, area, living -> living != this && !(living instanceof TaintedMob))
            .stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private boolean isAgitated() {
        return this.target != null && this.distanceToSqr(this.target) < this.getBbHeight() * 7.0 * this.getBbHeight() * 7.0;
    }

    public boolean isAgitatedForRender() {
        return this.isAgitated();
    }

    public boolean isAttackingForRender() {
        return this.hurtTime > 0 || this.attackTime > 0;
    }

    private void faceTarget(Entity target) {
        float targetYaw = (float) (Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * 180.0 / Math.PI) - 90.0F;
        this.setYRot(this.getYRot() + Mth.clamp(Mth.wrapDegrees(targetYaw - this.getYRot()), -5.0F, 5.0F));
    }

    private static boolean verticalOverlap(Entity left, Entity right) {
        return right.getBoundingBox().maxY > left.getBoundingBox().minY && right.getBoundingBox().minY < left.getBoundingBox().maxY;
    }

    private static boolean hasAdjacentSolid(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }
}
