package thaumcraft.device;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.vis.VisNet;

public class MirrorBlockEntity extends LinkedMirrorBlockEntity {
    private int instability;
    private List<ItemStack> outputStacks = new ArrayList<>();
    private final Inbound inbound = new Inbound();

    public MirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_MIRROR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MirrorBlockEntity mirror) {
        mirror.tick();
    }

    public int getInstability() {
        return instability;
    }

    public void setInstability(int instability) {
        this.instability = instability;
    }

    public List<ItemStack> getOutputStacks() {
        return outputStacks;
    }

    public @Nullable Container exposedItems(@Nullable Direction side) {
        return inbound;
    }

    public boolean transport(ItemEntity entity) {
        ItemStack items = entity.getItem();
        if (!linked || !isLinkValid(true)) {
            return false;
        }
        if (target(true) instanceof MirrorBlockEntity other) {
            other.addStack(items.copy());
            addInstability(null, items.getCount());
            entity.discard();
            setChanged();
            other.setChanged();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            return true;
        }
        return false;
    }

    public void addStack(ItemStack stack) {
        outputStacks.add(stack);
        setChanged();
    }

    private void eject() {
        if (outputStacks.size() > 0 && count > 20) {
            int index = level.getRandom().nextInt(outputStacks.size());
            ItemStack stored = outputStacks.get(index);
            ItemStack out = stored.copyWithCount(1);
            if (spawnItem(out)) {
                stored.shrink(1);
                addInstability(null, 1);
                level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
                if (stored.getCount() <= 0) {
                    outputStacks.remove(index);
                }
                setChanged();
            }
        }
    }

    public boolean spawnItem(ItemStack stack) {
        return level.addFreshEntity(MirrorLinks.ejected(level, worldPosition, getFacing(), stack));
    }

    protected void addInstability(@Nullable MirrorBlockEntity other, int amount) {
        instability += amount;
        if (other != null) {
            other.instability += amount;
            if (other.instability < 0) {
                other.instability = 0;
            }
            other.setChanged();
        }
    }

    private void tick() {
        flushInbound();
        int tickrate = instability / 50;
        if (tickrate == 0 || count % (tickrate * tickrate) == 0) {
            eject();
        }
        checkInstability();
        linkTick();
    }

    private void checkInstability() {
        if (instability > 0 && count % 20 == 0) {
            instability--;
            sync();
        }
        if (instability > 0) {
            int drained = VisNet.drainVis(level, worldPosition, Aspect.ORDER, Math.min(instability, 1));
            if (drained > 0) {
                addInstability(target(true) instanceof MirrorBlockEntity other ? other : null, -drained);
            }
        }
    }

    private void flushInbound() {
        ItemStack stack = inbound.getItem(0);
        if (stack.isEmpty()) {
            return;
        }
        inbound.setItem(0, ItemStack.EMPTY);
        if (target(true) instanceof MirrorBlockEntity other) {
            other.addStack(stack.copy());
            addInstability(null, stack.getCount());
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
        } else {
            spawnItem(stack.copy());
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide()) {
            return;
        }
        for (ItemStack stack : outputStacks) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }
        outputStacks.clear();
        ItemStack buffered = inbound.getItem(0);
        if (!buffered.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, buffered);
            inbound.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != 1) {
            return super.triggerEvent(id, param);
        }
        if (level != null && level.isClientSide()) {
            Fx.get().mirrorTransfer(level, worldPosition, getFacing());
        }
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        instability = input.getIntOr("instability", 0);
        outputStacks = new ArrayList<>(input.read("Items", ItemStack.CODEC.listOf()).orElseGet(List::of));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("instability", instability);
        List<ItemStack> stored = new ArrayList<>();
        for (ItemStack stack : outputStacks) {
            if (!stack.isEmpty()) {
                stored.add(stack);
            }
        }
        output.store("Items", ItemStack.CODEC.listOf(), stored);
    }

    private final class Inbound extends SimpleContainer {
        private Inbound() {
            super(1);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return isEmpty() && link != null && level != null && !level.isClientSide();
        }

        @Override
        public boolean canTakeItem(Container into, int slot, ItemStack stack) {
            return false;
        }
    }
}
