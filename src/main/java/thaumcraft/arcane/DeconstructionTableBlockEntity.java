package thaumcraft.arcane;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.menu.DeconstructionTableMenu;
import thaumcraft.registry.ModBlockEntities;

public class DeconstructionTableBlockEntity extends TcBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int BREAK_TIME = 40;

    private static final int[] SIDE_SLOTS = {0};
    private static final int[] NO_SLOTS = {};

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private @Nullable Aspect aspect;
    private int breakTime;

    public DeconstructionTableBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.DECONSTRUCTION_TABLE.get(), pos, state);
    }

    protected DeconstructionTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static AspectList getTags(ItemStack stack) {
        return AspectHelper.getBonusTags(stack, AspectHelper.getObjectTags(stack));
    }

    public static boolean hasAspects(ItemStack stack) {
        return !stack.isEmpty() && getTags(stack).size() > 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeconstructionTableBlockEntity table) {
        boolean changed = false;
        if (table.breakTime == 0 && table.canBreak()) {
            table.breakTime = BREAK_TIME;
            changed = true;
        }
        if (table.breakTime > 0 && table.canBreak()) {
            table.breakTime--;
            if (table.breakTime == 0) {
                table.breakItem(level);
                changed = true;
            }
        } else {
            table.breakTime = 0;
        }
        if (changed) {
            table.sync();
        }
    }

    public @Nullable WorldlyContainer exposedItems(@Nullable Direction side) {
        return this;
    }

    public @Nullable Aspect getAspect() {
        return aspect;
    }

    public void clearAspect() {
        aspect = null;
        sync();
    }

    public int getBreakTime() {
        return breakTime;
    }

    public int getBreakTimeScaled(int scale) {
        return breakTime * scale / BREAK_TIME;
    }

    private boolean canBreak() {
        return !items.get(0).isEmpty() && aspect == null && hasAspects(items.get(0));
    }

    private void breakItem(Level tickLevel) {
        if (!canBreak()) {
            return;
        }
        AspectList primals = AspectHelper.reduceToPrimals(getTags(items.get(0)));
        if (tickLevel.getRandom().nextInt(80) < primals.visSize()) {
            aspect = primals.getAspects().get(tickLevel.getRandom().nextInt(primals.getAspects().size()));
        }
        items.get(0).shrink(1);
        if (items.get(0).isEmpty()) {
            items.set(0, ItemStack.EMPTY);
        }
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
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return hasAspects(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? NO_SLOTS : SIDE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.UP && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
            && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, items);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.deconstruction_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DeconstructionTableMenu(containerId, inventory, worldPosition);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        aspect = input.read("Aspect", Aspect.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), items, true, output.lookup());
        if (aspect != null) {
            output.store("Aspect", Aspect.CODEC, aspect);
        }
    }
}
