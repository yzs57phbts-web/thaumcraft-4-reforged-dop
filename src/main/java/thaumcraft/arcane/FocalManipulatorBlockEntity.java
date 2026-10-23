package thaumcraft.arcane;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.menu.FocalManipulatorMenu;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;
import thaumcraft.vis.VisNet;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;

public class FocalManipulatorBlockEntity extends TcBlockEntity implements Container, MenuProvider {
    public static final int XP_MULT = 8;
    public static final int VIS_MULT = 200;

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private AspectList aspects = new AspectList();
    private int size;
    private int upgrade = -1;
    private int rank = -1;
    private int ticks;
    private boolean reset;

    public FocalManipulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    public AspectList getAspects() {
        return aspects;
    }

    public int getSize() {
        return size;
    }

    public int getUpgrade() {
        return upgrade;
    }

    public int getRank() {
        return rank;
    }

    public boolean consumeReset() {
        boolean value = reset;
        reset = false;
        return value;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FocalManipulatorBlockEntity table) {
        table.tickServer(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FocalManipulatorBlockEntity table) {
        if (table.size > 0) {
            RandomSource random = level.getRandom();
            Fx.get().focalManipulatorSparkle(
                level,
                pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                pos.getY() + 1.25 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3F
            );
        }
    }

    private void tickServer(Level level) {
        boolean complete = false;
        boolean failed = false;
        if (rank < 0) {
            rank = 0;
        }
        ticks++;
        if (ticks % 5 == 0 && size > 0) {
            ItemStack stack = items.get(0);
            if (aspects.visSize() <= 0 || stack.isEmpty()) {
                complete = true;
                failed = true;
                level.playSound(null, worldPosition, ModSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
            }
            for (Aspect aspect : aspects.getAspectsSortedAmount()) {
                int drain = VisNet.drainVis(level, worldPosition, aspect, Math.min(100, aspects.getAmount(aspect)));
                if (drain > 0) {
                    aspects.reduce(aspect, drain);
                    sync();
                }
            }
            if (!failed) {
                if (aspects.visSize() <= 0 && stack.getItem() instanceof FocusItem focus) {
                    complete = true;
                    FocusUpgradeType type = FocusUpgradeType.byId(upgrade);
                    if (type != null) {
                        focus.applyUpgrade(stack, type, rank);
                    }
                    level.playSound(null, worldPosition, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
        if (complete) {
            size = 0;
            rank = -1;
            aspects = new AspectList();
            sync();
        }
    }

    public boolean startCraft(int id, Player player) {
        ItemStack stack = items.get(0);
        if (size > 0 || !(stack.getItem() instanceof FocusItem focus)) {
            return false;
        }
        int[] applied = focus.getAppliedUpgrades(stack);
        rank = 1;
        while (rank <= FocusItem.MAX_UPGRADES && applied[rank - 1] != -1) {
            rank++;
        }
        int xp = rank * XP_MULT;
        if (player.experienceLevel < xp) {
            return false;
        }
        FocusUpgradeType[] possible = focus.getPossibleUpgradesByRank(stack, rank);
        if (possible == null) {
            return false;
        }
        boolean allowed = false;
        for (FocusUpgradeType type : possible) {
            if (type.id() == id) {
                allowed = true;
                break;
            }
        }
        FocusUpgradeType type = FocusUpgradeType.byId(id);
        if (!allowed || type == null || !focus.canApplyUpgrade(stack, player, type, rank)) {
            return false;
        }
        int amount = VIS_MULT;
        for (int index = 1; index < rank; index++) {
            amount *= 2;
        }
        AspectList cost = new AspectList();
        for (Aspect aspect : type.aspects().getAspects()) {
            cost.add(aspect, amount);
        }
        aspects = AspectHelper.reduceToPrimals(cost);
        size = aspects.visSize();
        upgrade = id;
        if (!player.isCreative()) {
            player.giveExperienceLevels(-xp);
        }
        sync();
        if (level != null) {
            level.playSound(null, worldPosition, ModSounds.CRAFTSTART.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
        }
        return true;
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
            sync();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) {
            sync();
        }
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        onSlotChanged();
    }

    private void onSlotChanged() {
        if (level != null && level.isClientSide()) {
            reset = true;
            return;
        }
        aspects = new AspectList();
        sync();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof FocusItem;
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
        return Component.translatable("block.thaumcraft.focal_manipulator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FocalManipulatorMenu(containerId, inventory, worldPosition);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        size = input.getIntOr("size", 0);
        upgrade = input.getIntOr("upgrade", -1);
        rank = input.getIntOr("rank", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), items, true, output.lookup());
        output.store("aspects", AspectList.CODEC, aspects);
        output.putInt("size", size);
        output.putInt("upgrade", upgrade);
        output.putInt("rank", rank);
    }
}
