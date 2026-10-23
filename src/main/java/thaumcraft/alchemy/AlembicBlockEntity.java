package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.wand.WandInteractable;

public class AlembicBlockEntity extends TcBlockEntity implements AspectContainer, EssentiaTransport, WandInteractable {
    public static final int MAX_AMOUNT = 32;

    private @Nullable Aspect aspect;
    private @Nullable Aspect aspectFilter;
    private int amount;

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ALEMBIC.get(), pos, state);
    }

    protected AlembicBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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

    public int getMaxAmount() {
        return MAX_AMOUNT;
    }

    public void removeAmount(int removed) {
        amount -= removed;
        if (amount <= 0) {
            amount = 0;
            aspect = null;
        }
    }

    public void clear() {
        amount = 0;
        aspect = null;
    }

    public Direction getFacing() {
        return getBlockState().getValue(AlembicBlock.FACING);
    }

    public boolean isAboveFurnace() {
        return level != null && level.getBlockState(worldPosition.below()).is(ModBlocks.ALCHEMICAL_FURNACE.get());
    }

    public boolean isAboveAlembic() {
        return level != null && level.getBlockState(worldPosition.below()).is(ModBlocks.ALEMBIC.get());
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide() && aspectFilter != null) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.JAR_LABEL.get()));
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public InteractionResult onWandUseOn(Level level, BlockPos pos, Player player, ItemStack wand, Direction side) {
        if (side.getAxis().isVertical()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, getBlockState().setValue(AlembicBlock.FACING, side), Block.UPDATE_ALL);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public AspectList getAspects() {
        return aspect != null ? new AspectList().add(aspect, amount) : new AspectList();
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect added, int addedAmount) {
        int left = addedAmount;
        if (amount < MAX_AMOUNT && added == aspect || amount == 0) {
            aspect = added;
            int taken = Math.min(left, MAX_AMOUNT - amount);
            amount += taken;
            left -= taken;
        }
        sync();
        return left;
    }

    @Override
    public boolean takeFromContainer(Aspect taken, int takenAmount) {
        if (amount == 0 || aspect == null) {
            aspect = null;
            amount = 0;
        }
        if (aspect != null && amount >= takenAmount && taken == aspect) {
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
    public boolean isConnectable(Direction face) {
        return face != getFacing() && face != Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != getFacing() && face != Direction.DOWN;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect taken, int takenAmount, Direction face) {
        return canOutputTo(face) && takeFromContainer(taken, takenAmount) ? takenAmount : 0;
    }

    @Override
    public int addEssentia(Aspect added, int addedAmount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return aspect;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return amount;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read("aspect", Aspect.CODEC).orElse(null);
        aspectFilter = input.read("AspectFilter", Aspect.CODEC).orElse(null);
        amount = input.getIntOr("amount", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspect != null) {
            output.store("aspect", Aspect.CODEC, aspect);
        }
        if (aspectFilter != null) {
            output.store("AspectFilter", Aspect.CODEC, aspectFilter);
        }
        output.putInt("amount", amount);
    }
}
