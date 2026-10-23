package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModBlockEntities;

public class VoidJarBlockEntity extends JarBlockEntity {
    public VoidJarBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.VOID_JAR.get(), pos, state);
    }

    protected VoidJarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    protected boolean canFill() {
        return true;
    }

    @Override
    public int addToContainer(Aspect added, int addedAmount) {
        boolean changed = getAmount() < getMaxAmount();
        if (addedAmount <= 0 || !doesContainerAccept(added)) {
            return addedAmount;
        }
        int remaining = addedAmount;
        if (added == getAspect() || getAmount() == 0) {
            setAspect(added);
            setAmount(getAmount() + remaining);
            remaining = 0;
            if (getAmount() > getMaxAmount()) {
                setAmount(getMaxAmount());
            }
        }
        if (changed) {
            sync();
        }
        return remaining;
    }

    @Override
    public int getMinimumSuction() {
        return getAspectFilter() != null ? 48 : 32;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return getAspectFilter() != null && getAmount() < getMaxAmount() ? 48 : 32;
    }
}
