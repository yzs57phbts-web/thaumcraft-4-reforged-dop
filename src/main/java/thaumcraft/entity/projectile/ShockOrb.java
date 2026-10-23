package thaumcraft.entity.projectile;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.compat.ValueInput;
import net.minecraft.nbt.CompoundTag;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.CombatUtils;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModSounds;

public class ShockOrb extends ThrowableProjectile {
    public int area = 4;
    public int damage = 5;

    public ShockOrb(EntityType<? extends ShockOrb> type, Level level) {
        super(type, level);
    }

    public static ShockOrb create(Level level, LivingEntity shooter) {
        ShockOrb orb = new ShockOrb(ModEntities.SHOCK_ORB.get(), level);
        CombatUtils.launch(orb, shooter, 1.5F);
        return orb;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHit(HitResult hit) {
        if (this.level() instanceof ServerLevel level) {
            DamageSource source = level.damageSources().indirectMagic(this, this.getOwner());
            for (Entity entity : CombatUtils.getEntitiesInRange(level, this.getX(), this.getY(), this.getZ(), this, Entity.class, this.area)) {
                if (CombatUtils.canEntityBeSeen(this, entity)) {
                    entity.hurt(source, this.damage);
                }
            }
            this.placeStaticField(level);
            level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.SHOCK.get(),
                SoundSource.NEUTRAL,
                1.0F,
                1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
            this.discard();
        } else {
            Fx.get().shockBurst(this.level(), this.getX(), this.getY(), this.getZ(), 3.0F);
        }
    }

    private void placeStaticField(ServerLevel level) {
        Optional<Block> field = BuiltInRegistries.BLOCK.getOptional(Thaumcraft.id("static_field"));
        if (field.isEmpty()) {
            return;
        }
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = Mth.floor(this.getX()) + this.random.nextInt(this.area) - this.random.nextInt(this.area);
            int y = Mth.floor(this.getY()) + this.area;
            int z = Mth.floor(this.getZ()) + this.random.nextInt(this.area) - this.random.nextInt(this.area);
            BlockPos pos = new BlockPos(x, y, z);
            while (level.isEmptyBlock(pos) && pos.getY() > Mth.floor(this.getY()) - this.area) {
                pos = pos.below();
            }
            BlockPos above = pos.above();
            if (level.isEmptyBlock(above)
                && !level.isEmptyBlock(pos)
                && !level.getBlockState(above).is(field.get())
                && CombatUtils.canEntityBeSeen(this, x + 0.5, pos.getY() + 1.5, z + 0.5)) {
                level.setBlock(above, field.get().defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > 500) {
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
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ZAP.get(), SoundSource.NEUTRAL, 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putInt("area", this.area);
        output.putInt("damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.area = input.getIntOr("area", 4);
        this.damage = input.getIntOr("damage", 5);
    }
}
