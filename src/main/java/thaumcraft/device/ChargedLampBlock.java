package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlockEntities;

public class ChargedLampBlock extends LampBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final boolean growth;

    public ChargedLampBlock(boolean growth, Properties properties) {
        super(properties);
        this.growth = growth;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return growth ? new LampOfGrowthBlockEntity(pos, state) : new LampOfFertilityBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        if (growth && type == ModBlockEntities.LAMP_OF_GROWTH.get()) {
            return (tickLevel, pos, tickState, entity) -> LampOfGrowthBlockEntity.serverTick(tickLevel, pos, (LampOfGrowthBlockEntity) entity);
        }
        if (!growth && type == ModBlockEntities.LAMP_OF_FERTILITY.get()) {
            return (tickLevel, pos, tickState, entity) -> LampOfFertilityBlockEntity.serverTick(tickLevel, pos, (LampOfFertilityBlockEntity) entity);
        }
        return null;
    }
}
