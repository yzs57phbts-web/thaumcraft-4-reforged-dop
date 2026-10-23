package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSource;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class JarBlockEntity extends TcBlockEntity implements AspectSource, EssentiaTransport {
    private @Nullable Aspect aspect;
    private @Nullable Aspect aspectFilter;
    private int amount;
    private final int maxAmount = 64;
    private Direction facing = Direction.NORTH;
    private int count;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.JAR.get(), pos, state);
    }

    protected JarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, JarBlockEntity jar) {
        jar.tick();
    }

    protected void tick() {
        if (++count % 5 == 0 && canFill()) {
            fillJar();
        }
    }

    protected boolean canFill() {
        return amount < maxAmount;
    }

    public @Nullable Aspect getAspect() {
        return aspect;
    }

    public void setAspect(@Nullable Aspect aspect) {
        this.aspect = aspect;
    }

    public @Nullable Aspect getAspectFilter() {
        return aspectFilter;
    }

    public void setAspectFilter(@Nullable Aspect aspectFilter) {
        this.aspectFilter = aspectFilter;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public Direction getFacing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    public boolean isVoid() {
        return false;
    }

    public int getComparatorSignal() {
        float ratio = (float) amount / maxAmount;
        return (int) Math.floor(ratio * 14.0F) + (amount > 0 ? 1 : 0);
    }

    @Override
    public AspectList getAspects() {
        AspectList aspects = new AspectList();
        if (aspect != null && amount > 0) {
            aspects.add(aspect, amount);
        }
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect added, int addedAmount) {
        if (addedAmount <= 0 || !doesContainerAccept(added)) {
            return addedAmount;
        }
        int remaining = addedAmount;
        if (amount < maxAmount && added == aspect || amount == 0) {
            aspect = added;
            int accepted = Math.min(remaining, maxAmount - amount);
            amount += accepted;
            remaining -= accepted;
        }
        sync();
        return remaining;
    }

    @Override
    public boolean takeFromContainer(Aspect taken, int takenAmount) {
        if (amount >= takenAmount && taken == aspect) {
            amount -= takenAmount;
            if (amount <= 0) {
                aspect = null;
                amount = 0;
            }
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect checked, int checkedAmount) {
        return amount >= checkedAmount && checked == aspect;
    }

    @Override
    public int containerContains(Aspect checked) {
        return checked == aspect ? amount : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect checked) {
        return aspectFilter == null || checked == aspectFilter;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    @Override
    public int getMinimumSuction() {
        return aspectFilter != null ? 64 : 32;
    }

    @Override
    public @Nullable Aspect getSuctionType(@Nullable Direction face) {
        return aspectFilter != null ? aspectFilter : aspect;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (amount < maxAmount) {
            return aspectFilter != null ? 64 : 32;
        }
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(@Nullable Direction face) {
        return aspect;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return amount;
    }

    @Override
    public int takeEssentia(Aspect taken, int takenAmount, Direction face) {
        return canOutputTo(face) && takeFromContainer(taken, takenAmount) ? takenAmount : 0;
    }

    @Override
    public int addEssentia(Aspect added, int addedAmount, Direction face) {
        return canInputFrom(face) ? addedAmount - addToContainer(added, addedAmount) : 0;
    }

    protected void fillJar() {
        if (level == null) {
            return;
        }
        EssentiaTransport source = Essentia.getConnectable(level, worldPosition, Direction.UP);
        if (source == null || !source.canOutputTo(Direction.DOWN)) {
            return;
        }
        Aspect wanted = null;
        if (aspectFilter != null) {
            wanted = aspectFilter;
        } else if (aspect != null && amount > 0) {
            wanted = aspect;
        } else if (source.getEssentiaAmount(Direction.DOWN) > 0
            && source.getSuctionAmount(Direction.DOWN) < getSuctionAmount(Direction.UP)
            && getSuctionAmount(Direction.UP) >= source.getMinimumSuction()) {
            wanted = source.getEssentiaType(Direction.DOWN);
        }
        if (wanted != null && source.getSuctionAmount(Direction.DOWN) < getSuctionAmount(Direction.UP)) {
            addToContainer(wanted, source.takeEssentia(wanted, 1, Direction.DOWN));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read("Aspect", Aspect.CODEC).orElse(null);
        aspectFilter = input.read("AspectFilter", Aspect.CODEC).orElse(null);
        amount = input.getIntOr("Amount", 0);
        facing = Direction.from3DDataValue(input.getIntOr("facing", Direction.NORTH.get3DDataValue()));
        if (facing.getAxis().isVertical()) {
            facing = Direction.NORTH;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspect != null) {
            output.store("Aspect", Aspect.CODEC, aspect);
        }
        if (aspectFilter != null) {
            output.store("AspectFilter", Aspect.CODEC, aspectFilter);
        }
        output.putInt("Amount", amount);
        output.putInt("facing", facing.get3DDataValue());
    }
}
