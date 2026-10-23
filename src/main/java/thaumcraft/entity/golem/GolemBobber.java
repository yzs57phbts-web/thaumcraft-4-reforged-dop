package thaumcraft.entity.golem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;

public class GolemBobber extends Entity {
    private static final EntityDataAccessor<Integer> FISHER_ID = SynchedEntityData.defineId(GolemBobber.class, EntityDataSerializers.INT);
    private boolean inBlock;

    public GolemBobber(EntityType<? extends GolemBobber> type, Level level) {
        super(type, level);
    }

    public GolemBobber(Level level, GolemBase fisher) {
        this(thaumcraft.registry.ModEntities.GOLEM_BOBBER.get(), level);
        setFisher(fisher);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FISHER_ID, -1);
    }

    public void setFisher(@Nullable GolemBase fisher) {
        entityData.set(FISHER_ID, fisher == null ? -1 : fisher.getId());
    }

    public @Nullable GolemBase getFisher() {
        Entity entity = entityData.get(FISHER_ID) < 0 ? null : level().getEntity(entityData.get(FISHER_ID));
        return entity instanceof GolemBase golem ? golem : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (getFisher() == null || !getFisher().isAlive()) {
                discard();
                return;
            }
            if (random.nextFloat() < 0.02F && level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SPLASH, getX() + random.nextFloat() - random.nextFloat(), getY() + random.nextFloat(), getZ() + random.nextFloat() - random.nextFloat(), 2 + random.nextInt(2), 0.1, 0, 0.1, 0);
            }
        }
        if (tickCount > 4000) {
            discard();
            return;
        }
        Vec3 velocity = getDeltaMovement();
        if (inBlock) {
            inBlock = false;
            velocity = new Vec3(velocity.x * (random.nextFloat() * 0.2F), velocity.y * (random.nextFloat() * 0.2F), velocity.z * (random.nextFloat() * 0.2F));
        }
        Vec3 start = position();
        BlockHitResult hit = level().clip(new ClipContext(start, start.add(velocity), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            if (!level().getBlockState(hit.getBlockPos()).getFluidState().is(Fluids.WATER)) {
                discard();
                return;
            }
            inBlock = true;
        }
        if (!inBlock) {
            move(MoverType.SELF, velocity);
            float horizontal = Mth.sqrt((float) (velocity.x * velocity.x + velocity.z * velocity.z));
            float targetYaw = (float) (Math.atan2(velocity.x, velocity.z) * 180.0 / Math.PI);
            float targetPitch = (float) (Math.atan2(velocity.y, horizontal) * 180.0 / Math.PI);
            setYRot(getYRot() + Mth.wrapDegrees(targetYaw - getYRot()) * 0.2F);
            setXRot(getXRot() + Mth.wrapDegrees(targetPitch - getXRot()) * 0.2F);
            float drag = onGround() || horizontalCollision ? 0.5F : 0.92F;
            double water = waterFraction();
            if (!level().isClientSide() && water > 0 && level() instanceof ServerLevel server) updateUnusedWaterState(server);
            double gravity = water * 2.0 - 1.0;
            double vy = velocity.y + 0.04F * gravity;
            if (water > 0) {
                drag *= 0.9F;
                vy *= 0.8;
            }
            setDeltaMovement(velocity.x * drag, vy * drag, velocity.z * drag);
            setPos(getX(), getY(), getZ());
        } else {
            setDeltaMovement(velocity);
        }
    }

    private double waterFraction() {
        AABB box = getBoundingBox();
        double amount = 0;
        for (int index = 0; index < 5; index++) {
            double sampleY = box.minY + (box.maxY - box.minY) * (index + 0.5) / 5.0;
            FluidState fluid = level().getFluidState(net.minecraft.core.BlockPos.containing(getX(), sampleY, getZ()));
            if (fluid.is(Fluids.WATER)) amount += 0.2;
        }
        return amount;
    }

    private void updateUnusedWaterState(ServerLevel server) {
        int effect = 1;
        BlockPos pos = blockPosition().above();
        if (random.nextFloat() < 0.25F && server.isRainingAt(pos)) effect = 2;
        if (random.nextFloat() < 0.5F && !server.canSeeSky(pos)) effect--;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float damage) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        ValueOutput output = ValueOutput.of(tag, registryAccess());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        ValueInput input = ValueInput.of(tag, registryAccess());
    }
}
