package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class MnemonicMatrixBlockEntity extends TcBlockEntity {
    public MnemonicMatrixBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MNEMONIC_MATRIX.get(), pos, state);
    }

    protected MnemonicMatrixBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getFacing() {
        return getBlockState().getValue(MnemonicMatrixBlock.FACING);
    }
}
