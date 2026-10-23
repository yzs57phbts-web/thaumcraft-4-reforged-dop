package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlockEntities;

public class HungryChestBlockEntity extends BaseContainerBlockEntity {
    public static final int SIZE = 27;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int numUsingPlayers;
    public float lidAngle;
    public float prevLidAngle;

    public HungryChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HUNGRY_CHEST.get(), pos, state);
    }

    public @Nullable HungryChestBlockEntity exposedItems(@Nullable Direction side) {
        return this;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.hungry_chest");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    public int getComparatorSignal() {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(this);
    }

    public ItemStack insert(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < items.size() && !remaining.isEmpty(); slot++) {
            ItemStack inSlot = items.get(slot);
            if (!inSlot.isEmpty() && ItemStack.isSameItemSameComponents(inSlot, remaining)) {
                int room = Math.min(getMaxStackSize(), inSlot.getMaxStackSize()) - inSlot.getCount();
                int moved = Math.min(remaining.getCount(), room);
                if (moved > 0) {
                    inSlot.grow(moved);
                    remaining.shrink(moved);
                }
            }
        }
        for (int slot = 0; slot < items.size() && !remaining.isEmpty(); slot++) {
            if (items.get(slot).isEmpty()) {
                int moved = Math.min(remaining.getCount(), Math.min(getMaxStackSize(), remaining.getMaxStackSize()));
                items.set(slot, remaining.split(moved));
            }
        }
        if (remaining.getCount() != stack.getCount()) {
            setChanged();
        }
        return remaining;
    }

    @Override
    public void startOpen(Player user) {
        if (!remove && !user.isSpectator() && level != null) {
            numUsingPlayers++;
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, numUsingPlayers);
        }
    }

    @Override
    public void stopOpen(Player user) {
        if (!remove && !user.isSpectator() && level != null) {
            numUsingPlayers--;
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, numUsingPlayers);
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            numUsingPlayers = param;
            return true;
        }
        if (id == 2) {
            if (lidAngle < param / 10.0F) {
                lidAngle = param / 10.0F;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        chest.prevLidAngle = chest.lidAngle;
        float step = 0.1F;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (chest.numUsingPlayers > 0 && chest.lidAngle == 0.0F) {
            level.playLocalSound(x, y, z, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F, false);
        }
        if (chest.numUsingPlayers == 0 && chest.lidAngle > 0.0F || chest.numUsingPlayers > 0 && chest.lidAngle < 1.0F) {
            float previous = chest.lidAngle;
            if (chest.numUsingPlayers > 0) {
                chest.lidAngle += step;
            } else {
                chest.lidAngle -= step;
            }
            if (chest.lidAngle > 1.0F) {
                chest.lidAngle = 1.0F;
            }
            if (chest.lidAngle < 0.5F && previous >= 0.5F) {
                level.playLocalSound(x, y, z, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F, false);
            }
            if (chest.lidAngle < 0.0F) {
                chest.lidAngle = 0.0F;
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = ValueInput.of(tag, registries);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = ValueOutput.of(tag, registries);
        ContainerHelper.saveAllItems(output.tag(), items, output.lookup());
    }
}
