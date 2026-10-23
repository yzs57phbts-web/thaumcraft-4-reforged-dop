package thaumcraft.vis;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;

public abstract class VisNode extends TcBlockEntity {
    private @Nullable WeakReference<VisNode> parent;
    private final List<WeakReference<VisNode>> children = new ArrayList<>();
    protected int nodeCounter;
    private boolean nodeRegged;
    private boolean nodeRefresh;
    private boolean shuttingDown;

    protected VisNode(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract int getRange();

    public abstract boolean isSource();

    public byte getAttunement() {
        return -1;
    }

    public GlobalPos getLocation() {
        return GlobalPos.of(level.dimension(), worldPosition);
    }

    public int consumeVis(Aspect aspect, int vis) {
        if (VisNet.isNodeValid(parent)) {
            int consumed = parent.get().consumeVis(aspect, vis);
            if (consumed > 0) {
                triggerConsumeEffect(aspect);
            }
            return consumed;
        }
        return 0;
    }

    public void triggerConsumeEffect(Aspect aspect) {
    }

    public void parentChanged() {
    }

    public @Nullable WeakReference<VisNode> getParent() {
        return parent;
    }

    public void setParent(@Nullable WeakReference<VisNode> parent) {
        this.parent = parent;
    }

    public List<WeakReference<VisNode>> getChildren() {
        return children;
    }

    public @Nullable WeakReference<VisNode> getRootSource() {
        if (VisNet.isNodeValid(parent)) {
            return parent.get().getRootSource();
        }
        return isSource() ? new WeakReference<>(this) : null;
    }

    public void requestNodeRefresh() {
        nodeRefresh = true;
    }

    protected boolean isShuttingDown() {
        return shuttingDown;
    }

    public void removeThisNode() {
        for (WeakReference<VisNode> reference : new ArrayList<>(children)) {
            VisNode child = reference.get();
            if (child != null) {
                child.removeThisNode();
            }
        }
        children.clear();
        if (VisNet.isNodeValid(parent)) {
            parent.get().nodeRefresh = true;
        }
        setParent(null);
        parentChanged();
        if (level != null) {
            if (isSource()) {
                VisNet.removeSource(level, this);
                nodeRegged = false;
            } else {
                VisNet.clearNearby();
            }
            if (!shuttingDown) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }
        }
    }

    @Override
    public void setRemoved() {
        shuttingDown = true;
        removeThisNode();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        shuttingDown = true;
        removeThisNode();
        super.onChunkUnloaded();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        shuttingDown = false;
    }

    protected void tickNetwork(Level tickLevel) {
        if (tickLevel.isClientSide()) {
            return;
        }
        if (nodeCounter++ % 40 == 0 || nodeRefresh) {
            if (!nodeRefresh && !children.isEmpty()) {
                for (WeakReference<VisNode> reference : children) {
                    VisNode child = reference.get();
                    if (child == null || !VisNet.canNodeBeSeen(this, child)) {
                        nodeRefresh = true;
                        break;
                    }
                }
            }
            if (nodeRefresh) {
                for (WeakReference<VisNode> reference : children) {
                    VisNode child = reference.get();
                    if (child != null) {
                        child.nodeRefresh = true;
                    }
                }
                children.clear();
                parent = null;
            }
            if (isSource() && !nodeRegged) {
                VisNet.addSource(tickLevel, this);
                nodeRegged = true;
            } else if (!isSource() && !VisNet.isNodeValid(parent)) {
                setParent(VisNet.addNode(tickLevel, this));
                nodeRefresh = true;
            }
            if (nodeRefresh) {
                tickLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                parentChanged();
            }
            nodeRefresh = false;
        }
    }
}
