package thaumcraft.node;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlockEntities;

public class NodeStabilizerBlock extends Block implements EntityBlock {
    private final boolean advanced;

    public NodeStabilizerBlock(boolean advanced, Properties properties) {
        super(properties);
        this.advanced = advanced;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeStabilizerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide() || type != ModBlockEntities.NODE_STABILIZER.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> NodeStabilizerBlockEntity.clientTick(tickLevel, pos, tickState, (NodeStabilizerBlockEntity) entity);
    }
}
