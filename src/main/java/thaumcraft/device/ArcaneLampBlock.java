package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneLampBlock extends LampBlock {
    public ArcaneLampBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneLampBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.ARCANE_LAMP.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ArcaneLampBlockEntity.serverTick(tickLevel, pos, (ArcaneLampBlockEntity) entity);
    }
}
