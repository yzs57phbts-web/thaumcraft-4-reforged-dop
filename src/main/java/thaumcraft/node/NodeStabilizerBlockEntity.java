package thaumcraft.node;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class NodeStabilizerBlockEntity extends TcBlockEntity {
    private int count;

    public NodeStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NODE_STABILIZER.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, NodeStabilizerBlockEntity entity) {
        entity.tick(level, pos);
    }

    private void tick(Level level, BlockPos pos) {
        if (!level.isClientSide() || pos.getY() >= (level.getMaxBuildHeight() - 1)) {
            return;
        }
        BlockState above = level.getBlockState(pos.above());
        if ((above.is(ModBlocks.AURA_NODE.get()) || above.is(ModBlocks.ENERGIZED_AURA_NODE.get())) && !level.hasNeighborSignal(pos)) {
            if (count < 37) {
                count++;
            }
        } else if (count > 0) {
            count--;
        }
    }

    public int getCount() {
        return count;
    }

    public int getLock() {
        return getBlockState().getBlock() instanceof NodeStabilizerBlock block && block.isAdvanced() ? 2 : 1;
    }
}
