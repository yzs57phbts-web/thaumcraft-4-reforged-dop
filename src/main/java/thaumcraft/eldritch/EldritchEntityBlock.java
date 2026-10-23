package thaumcraft.eldritch;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import thaumcraft.registry.EldritchContent;

public final class EldritchEntityBlock extends EldritchBlock implements EntityBlock {
    public EldritchEntityBlock(int kind, Properties properties) {
        super(kind, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (kind() == 1 && state.getValue(PART) == DoubleBlockHalf.UPPER) {
            return new EldritchPartBlockEntity(pos, state);
        }
        return EldritchBlockEntity.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != EldritchContent.ELDRITCH_BLOCK_ENTITY.get() || kind() == 3
            || kind() == 1 && state.getValue(PART) == DoubleBlockHalf.UPPER) return null;
        return (tickLevel, pos, tickState, entity) -> EldritchBlockEntity.tick(tickLevel, pos, tickState, (EldritchBlockEntity) entity);
    }
}
