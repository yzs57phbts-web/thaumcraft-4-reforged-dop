package thaumcraft.alchemy;

import thaumcraft.blockentity.TcBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import thaumcraft.platform.Menus;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class ThaumatoriumBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final boolean top;

    public ThaumatoriumBlock(boolean top, Properties properties) {
        super(properties);
        this.top = top;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public boolean isTop() {
        return top;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(ModBlocks.ALCHEMICAL_CONSTRUCT.get().asItem()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockPos bottomPos = top ? pos.below() : pos;
        if (!(level.getBlockEntity(bottomPos) instanceof ThaumatoriumBlockEntity thaumatorium)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Menus.open(serverPlayer, thaumatorium, bottomPos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            if (top) {
                if (!level.getBlockState(pos.below()).is(ModBlocks.THAUMATORIUM.get())) {
                    level.setBlock(pos, ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState(), Block.UPDATE_ALL);
                    return;
                }
                if (level.getBlockEntity(pos.below()) instanceof ThaumatoriumBlockEntity thaumatorium) {
                    thaumatorium.getUpgrades();
                }
            } else {
                if (!level.getBlockState(pos.above()).is(ModBlocks.THAUMATORIUM_TOP.get()) || !level.getBlockState(pos.below()).is(ModBlocks.CRUCIBLE.get())) {
                    level.setBlock(pos, ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState(), Block.UPDATE_ALL);
                    return;
                }
                if (level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity thaumatorium) {
                    thaumatorium.getUpgrades();
                }
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return !top;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity thaumatorium) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer(thaumatorium);
        }
        return 0;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, param);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return top ? new ThaumatoriumTopBlockEntity(pos, state) : new ThaumatoriumBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (top || type != ModBlockEntities.THAUMATORIUM.get()) {
            return null;
        }
        if (level.isClientSide()) {
            return (tickLevel, pos, tickState, entity) -> ThaumatoriumBlockEntity.clientTick(tickLevel, pos, tickState, (ThaumatoriumBlockEntity) entity);
        }
        return (tickLevel, pos, tickState, entity) -> ThaumatoriumBlockEntity.serverTick(tickLevel, pos, tickState, (ThaumatoriumBlockEntity) entity);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
