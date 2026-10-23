package thaumcraft.arcane;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.menu.ArcaneWorkbenchMenu;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.wand.Wands;

public class ArcaneWorkbenchBlockEntity extends TcBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int GRID_SLOTS = 9;
    public static final int OUTPUT_SLOT = 9;
    public static final int WAND_SLOT = 10;
    public static final int SLOT_COUNT = 11;

    private static final int[] FACE_SLOTS = {WAND_SLOT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final List<ArcaneWorkbenchMenu> menus = new ArrayList<>();

    public ArcaneWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ARCANE_WORKBENCH.get(), pos, state);
    }

    protected ArcaneWorkbenchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable WorldlyContainer exposedItems(@Nullable Direction side) {
        return this;
    }

    public ItemStack getWand() {
        return items.get(WAND_SLOT);
    }

    public List<ItemStack> getGrid() {
        List<ItemStack> grid = new ArrayList<>(GRID_SLOTS);
        for (int slot = 0; slot < GRID_SLOTS; slot++) {
            grid.add(items.get(slot));
        }
        return grid;
    }

    public void addMenu(ArcaneWorkbenchMenu menu) {
        menus.add(menu);
    }

    public void removeMenu(ArcaneWorkbenchMenu menu) {
        menus.remove(menu);
    }

    public void markWandChanged() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            sync();
        }
    }

    private void contentsChanged(int slot) {
        setChanged();
        if (level != null && !level.isClientSide()) {
            sync();
        }
        if (slot != OUTPUT_SLOT) {
            for (ArcaneWorkbenchMenu menu : new ArrayList<>(menus)) {
                menu.onCraftMatrixChanged();
            }
        }
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            contentsChanged(slot);
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT || slot == OUTPUT_SLOT) {
            return;
        }
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        contentsChanged(slot);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) {
            return false;
        }
        return slot != WAND_SLOT || stack.isEmpty() || Wands.isWand(stack) && !Wands.isStaff(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return FACE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == WAND_SLOT && Wands.isWand(stack) && !Wands.isStaff(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == WAND_SLOT;
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
        return Component.translatable("block.thaumcraft.arcane_worktable");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ArcaneWorkbenchMenu(containerId, inventory, worldPosition);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        items.set(OUTPUT_SLOT, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), items, true, output.lookup());
    }
}
