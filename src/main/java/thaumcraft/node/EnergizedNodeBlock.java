package thaumcraft.node;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlockEntities;

public class EnergizedNodeBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

    public EnergizedNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergizedNodeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ENERGIZED_NODE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> EnergizedNodeBlockEntity.serverTick(tickLevel, pos, tickState, (EnergizedNodeBlockEntity) entity);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        BlockPos below = pos.below();
        if (level.getBlockState(below).getBlock() instanceof NodeStabilizerBlock && !level.hasNeighborSignal(below)) {
            if (!(level.getBlockEntity(pos.above()) instanceof NodeTransducerBlockEntity)) {
                explodify(level, pos);
            }
        } else {
            explodify(level, pos);
        }
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
    }

    public static void explodify(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            level.removeBlock(pos, false);
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F, Level.ExplosionInteraction.BLOCK);
            for (int attempt = 0; attempt < 50; attempt++) {
                BlockPos target = pos.offset(
                    level.getRandom().nextInt(8) - level.getRandom().nextInt(8),
                    level.getRandom().nextInt(8) - level.getRandom().nextInt(8),
                    level.getRandom().nextInt(8) - level.getRandom().nextInt(8)
                );
                if (level.isEmptyBlock(target)) {
                    NodeHooks.spillFlux(serverLevel, target, target.getY() >= pos.getY());
                }
            }
        }
    }
}
