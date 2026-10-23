package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneBoreBaseBlockEntity extends TcBlockEntity implements EssentiaTransport {
    public ArcaneBoreBaseBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ARCANE_BORE_BASE.get(), pos, state);
    }

    protected ArcaneBoreBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getOrientation() {
        return getBlockState().getValue(ArcaneBoreBaseBlock.FACING);
    }

    public boolean drawEssentia() {
        for (Direction facing : Direction.values()) {
            EssentiaTransport source = Essentia.getConnectable(level, worldPosition, facing);
            if (source != null) {
                if (!source.canOutputTo(facing.getOpposite())) {
                    return false;
                }
                if (source.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing)
                    && source.takeEssentia(Aspect.ENTROPY, 1, facing.getOpposite()) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return Aspect.ENTROPY;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return face != getOrientation() ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }
}
