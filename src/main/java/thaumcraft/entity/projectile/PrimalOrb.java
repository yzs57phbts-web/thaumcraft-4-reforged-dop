package thaumcraft.entity.projectile;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.compat.ValueInput;
import net.minecraft.nbt.CompoundTag;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import thaumcraft.entity.CombatUtils;
import thaumcraft.fx.Fx;
import thaumcraft.node.NodeGenerator;
import thaumcraft.registry.ModEntities;
import thaumcraft.platform.BiomeEdit;
import thaumcraft.registry.ModBlocks;
import thaumcraft.taint.TaintBiomes;

public class PrimalOrb extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> DATA_SEEKER = SynchedEntityData.defineId(PrimalOrb.class, EntityDataSerializers.BOOLEAN);

    private int count;

    public PrimalOrb(EntityType<? extends PrimalOrb> type, Level level) {
        super(type, level);
    }

    public static PrimalOrb create(Level level, LivingEntity shooter, boolean seeker) {
        PrimalOrb orb = new PrimalOrb(ModEntities.PRIMAL_ORB.get(), level);
        orb.setSeeker(seeker);
        CombatUtils.launch(orb, shooter, 0.5F);
        return orb;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SEEKER, false);
    }

    public boolean isSeeker() {
        return this.entityData.get(DATA_SEEKER);
    }

    public void setSeeker(boolean seeker) {
        this.entityData.set(DATA_SEEKER, seeker);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.001;
    }

    private boolean isInsidePortal() {
        return this.level().getBlockState(this.blockPosition()).is(Blocks.NETHER_PORTAL) || this.level().getBlockState(this.blockPosition()).is(Blocks.END_PORTAL);
    }

    @Override
    public void tick() {
        this.count++;
        if (this.isInsidePortal()) {
            this.explode(null);
            if (this.isRemoved()) {
                return;
            }
        }
        if (this.level().isClientSide()) {
            Fx.get().primalOrbTrail(this.level(), this);
        }
        Random random = new Random(this.getId() + this.count);
        if (this.tickCount > 20) {
            if (!this.isSeeker()) {
                this.setDeltaMovement(this.getDeltaMovement().add(
                    (random.nextFloat() - random.nextFloat()) * 0.01F,
                    (random.nextFloat() - random.nextFloat()) * 0.01F,
                    (random.nextFloat() - random.nextFloat()) * 0.01F
                ));
            } else {
                this.seek();
            }
        }
        super.tick();
        if (this.tickCount > 5000 && !this.level().isClientSide()) {
            this.discard();
        }
    }

    private void seek() {
        double best = Double.MAX_VALUE;
        Entity target = null;
        Entity owner = this.getOwner();
        for (Entity entity : CombatUtils.getEntitiesInRange(this.level(), this.getX(), this.getY(), this.getZ(), this, LivingEntity.class, 16.0)) {
            if (entity != owner && entity.isAlive()) {
                double distance = this.distanceToSqr(entity);
                if (distance < best) {
                    best = distance;
                    target = entity;
                }
            }
        }
        if (target != null) {
            double dx = (target.getX() - this.getX()) / best;
            double dy = (target.getBoundingBox().minY + target.getBbHeight() * 0.9 - this.getY()) / best;
            double dz = (target.getZ() - this.getZ()) / best;
            double pull = 0.2;
            this.setDeltaMovement(
                Mth.clamp((float) (this.getDeltaMovement().x + dx * pull), -0.2F, 0.2F),
                Mth.clamp((float) (this.getDeltaMovement().y + dy * pull), -0.2F, 0.2F),
                Mth.clamp((float) (this.getDeltaMovement().z + dz * pull), -0.2F, 0.2F)
            );
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        this.explode(hit instanceof BlockHitResult blockHit ? blockHit : null);
    }

    private void explode(BlockHitResult hit) {
        if (!(this.level() instanceof ServerLevel level)) {
            Fx.get().primalOrbBurst(this.level(), this.getX(), this.getY(), this.getZ());
            return;
        }
        float specialChance = 1.0F;
        float radius = 2.0F;
        if (hit != null && this.isInsidePortal()) {
            radius = 4.0F;
            specialChance = 10.0F;
        }
        level.explode(null, this.getX(), this.getY(), this.getZ(), radius, true, Level.ExplosionInteraction.BLOCK);
        if (!this.isSeeker() && this.random.nextInt(100) <= specialChance) {
            if (this.random.nextBoolean()) {
                this.taintSplosion();
            } else {
                BlockPos pos = hit != null ? hit.getBlockPos() : this.blockPosition();
                NodeGenerator.createRandomNodeAt(level, pos, this.random, false, false, true);
            }
        }
        this.discard();
    }

    public void taintSplosion() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        int x = (int)this.getX();
        int z = (int)this.getZ();
        var tainted = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(TaintBiomes.TAINTED_LAND);
        for (int attempt = 0; attempt < 10; attempt++) {
            int xx = x + (int)(this.random.nextFloat() - this.random.nextFloat() * 6.0F);
            int zz = z + (int)(this.random.nextFloat() - this.random.nextFloat() * 6.0F);
            BlockPos column = new BlockPos(xx, 0, zz);
            if (this.random.nextBoolean() && level.hasChunkAt(column)) {
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, xx, zz);
                BlockPos surface = new BlockPos(xx, y, zz);
                if (!BiomeEdit.getColumnBiome(level, surface).is(TaintBiomes.TAINTED_LAND)) {
                    BiomeEdit.setColumn(level, surface, tainted);
                    if (y > level.getMinBuildHeight() && !level.isEmptyBlock(surface.below())) {
                        level.setBlock(surface, ModBlocks.FIBROUS_TAINT.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putBoolean("seeker", this.isSeeker());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.setSeeker(input.getBooleanOr("seeker", false));
    }
}
