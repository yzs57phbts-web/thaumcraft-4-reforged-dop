package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.platform.Transfer;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneSpaBlockEntity extends TcBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int CAPACITY = 5000;
    public static final int PORTION = 1000;
    private static final int[] SLOTS_SIDES = {0};
    private static final int[] SLOTS_TOP = {};

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final Transfer.FluidTank tank = new Transfer.FluidTank(CAPACITY, stack -> true, true, this::onTankChanged);
    private boolean mix = true;
    private int counter;

    public ArcaneSpaBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ARCANE_SPA.get(), pos, state);
    }

    protected ArcaneSpaBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private void onTankChanged() {
        if (level != null && !level.isClientSide()) {
            sync();
        } else {
            setChanged();
        }
    }

    public @Nullable Transfer.FluidTank exposedFluids(@Nullable Direction side) {
        return side == Direction.UP ? null : tank;
    }

    public @Nullable WorldlyContainer exposedItems(@Nullable Direction side) {
        return side == Direction.UP ? null : this;
    }

    public Transfer.FluidTank getTank() {
        return tank;
    }

    public boolean getMix() {
        return mix;
    }

    public void toggleMix() {
        mix = !mix;
        sync();
    }

    public boolean fillFrom(FluidStack stack) {
        FluidStack current = tank.getFluid();
        if (tank.getAmount() >= CAPACITY || !current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, stack)) {
            return false;
        }
        int total = Math.min(CAPACITY, tank.getAmount() + stack.getAmount());
        tank.setFluid(stack.copyWithAmount(total));
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneSpaBlockEntity spa) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (spa.counter++ % 40 == 0 && !level.hasNeighborSignal(pos) && spa.hasIngredients()) {
            Block target = spa.mix ? SpaHooks.mixedFluidBlock() : spa.tank.getFluid().getFluid().defaultFluidState().createLegacyBlock().getBlock();
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.is(target) && aboveState.getFluidState().isSource()) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockPos candidate = above.offset(x, 0, z);
                        if (spa.isValidLocation(serverLevel, candidate, true, target)) {
                            spa.consumeIngredients();
                            level.setBlock(candidate, target.defaultBlockState(), Block.UPDATE_ALL);
                            return;
                        }
                    }
                }
            } else if (spa.isValidLocation(serverLevel, above, false, target)) {
                spa.consumeIngredients();
                level.setBlock(above, target.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private boolean hasIngredients() {
        FluidStack fluid = tank.getFluid();
        if (mix) {
            if (fluid.isEmpty() || !fluid.is(Fluids.WATER) || fluid.getAmount() < PORTION) {
                return false;
            }
            return SpaHooks.isBathSalts(items.get(0));
        }
        if (fluid.isEmpty() || fluid.getAmount() < PORTION) {
            return false;
        }
        return !fluid.getFluid().defaultFluidState().createLegacyBlock().isAir();
    }

    private void consumeIngredients() {
        if (mix) {
            items.get(0).shrink(1);
        }
        int remaining = tank.getAmount() - PORTION;
        FluidStack fluid = tank.getFluid();
        tank.setFluid(remaining > 0 ? fluid.copyWithAmount(remaining) : FluidStack.EMPTY);
        sync();
    }

    private boolean isValidLocation(ServerLevel level, BlockPos pos, boolean mustBeAdjacent, Block target) {
        if (target == Blocks.WATER && level.dimensionType().ultraWarm()) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        boolean alreadySource = state.is(target) && state.getFluidState().isSource();
        if (below.isFaceSturdy(level, belowPos, Direction.UP) && state.canBeReplaced() && !alreadySource) {
            if (!mustBeAdjacent) {
                return true;
            }
            for (Direction direction : Direction.values()) {
                BlockState neighbour = level.getBlockState(pos.relative(direction));
                if (neighbour.is(target) && neighbour.getFluidState().isSource()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, this);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ArcaneSpaMenu(containerId, inventory, worldPosition);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return SpaHooks.isBathSalts(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? SLOTS_TOP : SLOTS_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.UP && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side != Direction.UP;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        mix = input.getBooleanOr("mix", true);
        tank.setFluid(input.read("Fluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), items, output.lookup());
        output.putBoolean("mix", mix);
        output.store("Fluid", FluidStack.OPTIONAL_CODEC, tank.getFluid());
    }
}
