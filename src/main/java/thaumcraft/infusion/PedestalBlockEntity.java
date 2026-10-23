package thaumcraft.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;

public class PedestalBlockEntity extends TcBlockEntity implements WorldlyContainer {
    private static final int[] SLOTS = {0};

    private ItemStack stack = ItemStack.EMPTY;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.PEDESTAL.get(), pos, state);
    }

    protected PedestalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable WorldlyContainer exposedItems(@Nullable Direction side) {
        return this;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack value) {
        stack = value.isEmpty() ? ItemStack.EMPTY : value.copyWithCount(Math.min(value.getCount(), 1));
        onChanged();
    }

    public void setStackFromInfusion(ItemStack value) {
        stack = value;
        onChanged();
    }

    public ItemStack removeStack() {
        ItemStack removed = stack;
        stack = ItemStack.EMPTY;
        onChanged();
        return removed;
    }

    public void dropContents() {
        if (level != null && !level.isClientSide() && !stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, removeStack());
        }
    }

    private void onChanged() {
        if (level != null && !level.isClientSide()) {
            sync();
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        } else {
            setChanged();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !stack.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            stack = ItemStack.EMPTY;
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 11) {
            if (level != null && level.isClientSide()) {
                for (int index = 0; index < 5; index++) {
                    Fx.get().blockSparkle(level, worldPosition.above(), 12583104, 2);
                }
            }
            return true;
        }
        if (id == 12) {
            if (level != null && level.isClientSide()) {
                for (int index = 0; index < 10; index++) {
                    Fx.get().blockSparkle(level, worldPosition.above(), -9999, 2);
                }
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public int getComparatorSignal() {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(this);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack item, @Nullable Direction side) {
        return stack.isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack item, Direction side) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? stack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack.split(amount);
        if (stack.isEmpty()) {
            stack = ItemStack.EMPTY;
        }
        onChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack;
        stack = ItemStack.EMPTY;
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot == 0) {
            setStack(item);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack item) {
        return true;
    }

    @Override
    public void clearContent() {
        stack = ItemStack.EMPTY;
        onChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        stack = input.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("item", ItemStack.OPTIONAL_CODEC, stack);
    }
}
