package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.blockentity.WardingStoneBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class WardingAuraBlock extends Block implements EntityBlock {
    public WardingAuraBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext
            && entityContext.getEntity() instanceof LivingEntity
            && !(entityContext.getEntity() instanceof Player)) {
            int offset = level.getBlockState(pos.below()).is(ModBlocks.WARDING_PAVING_STONE.get()) ? 1 : 2;
            if (!(level instanceof Level realLevel) || !realLevel.hasNeighborSignal(pos.below(offset))) {
                return Shapes.block();
            }
        }
        return Shapes.empty();
    }

    @Override
    public @Nullable PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        if (mob != null) {
            for (int offset = 1; offset < 3; offset++) {
                if (level.getBlockEntity(pos.below(offset)) instanceof WardingStoneBlockEntity) {
                    return mob.level().hasNeighborSignal(pos.below(offset)) ? null : PathType.BLOCKED;
                }
            }
        }
        return null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardingAuraBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.WARDING_AURA.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> WardingAuraBlockEntity.serverTick(tickLevel, pos, (WardingAuraBlockEntity) entity);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            thaumcraft.blockentity.TcBlockEntity.beforeRemove(state, level, pos, newState);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
