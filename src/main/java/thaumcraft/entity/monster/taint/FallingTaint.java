package thaumcraft.entity.monster.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.registry.ModEntities;

public class FallingTaint extends Entity {
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE = SynchedEntityData.defineId(FallingTaint.class, EntityDataSerializers.BLOCK_STATE);
    private int age;

    public FallingTaint(EntityType<? extends FallingTaint> type, Level level) {
        super(type, level);
    }

    public static boolean fall(ServerLevel level, BlockPos source, BlockPos target, BlockState state) {
        FallingTaint falling = ModEntities.FALLING_TAINT.get().create(level);
        if (falling == null || !level.hasChunkAt(source) || !level.hasChunkAt(target)) {
            return false;
        }
        falling.setBlockState(state);
        falling.setPos(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        if (!level.addFreshEntity(falling)) {
            return false;
        }
        level.removeBlock(source, false);
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_BLOCK_STATE, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float damage) {
        return false;
    }

    private void setBlockState(BlockState state) {
        this.entityData.set(DATA_BLOCK_STATE, state);
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;
        if (this.age > 100) {
            this.discard();
            return;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0).scale(0.98));
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.level().isClientSide() && this.onGround()) {
            BlockPos landing = this.blockPosition();
            if (this.level().getBlockState(landing).canBeReplaced()) {
                this.level().setBlock(landing, this.getBlockState(), 3);
            } else {
                net.minecraft.world.level.block.Block.dropResources(this.getBlockState(), this.level(), landing);
            }
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        thaumcraft.compat.ValueOutput output = thaumcraft.compat.ValueOutput.of(tag, this.registryAccess());
        output.store("BlockState", BlockState.CODEC, this.getBlockState());
        output.putInt("Age", this.age);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        thaumcraft.compat.ValueInput input = thaumcraft.compat.ValueInput.of(tag, this.registryAccess());
        this.setBlockState(input.read("BlockState", BlockState.CODEC).orElse(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()));
        this.age = input.getIntOr("Age", 0);
    }
}
