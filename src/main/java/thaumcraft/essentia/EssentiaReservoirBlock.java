package thaumcraft.essentia;

import thaumcraft.blockentity.TcBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class EssentiaReservoirBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public EssentiaReservoirBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaReservoirBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ESSENTIA_RESERVOIR.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> EssentiaReservoirBlockEntity.tick(tickLevel, pos, tickState, (EssentiaReservoirBlockEntity) entity);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof EssentiaReservoirBlockEntity reservoir ? reservoir.getComparatorSignal() : 0;
    }

    public static void burst(ServerLevel level, BlockPos pos, EssentiaReservoirBlockEntity reservoir) {
        int size = reservoir.getAspects().visSize() / 16;
        if (size <= 0) {
            return;
        }
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.NONE);
        int placed = 0;
        for (int attempt = 0; attempt < 50; attempt++) {
            BlockPos target = pos.offset(
                level.getRandom().nextInt(5) - level.getRandom().nextInt(5),
                level.getRandom().nextInt(5) - level.getRandom().nextInt(5),
                level.getRandom().nextInt(5) - level.getRandom().nextInt(5)
            );
            if (level.isEmptyBlock(target)) {
                Block flux = target.getY() < pos.getY() ? ModBlocks.FLUX_GOO.get() : ModBlocks.FLUX_GAS.get();
                level.setBlock(target, flux.defaultBlockState(), Block.UPDATE_ALL);
                if (placed++ >= size) {
                    break;
                }
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
