package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.item.NodeJarItem;
import thaumcraft.node.Node;
import thaumcraft.node.NodeModifier;
import thaumcraft.node.NodeType;
import thaumcraft.registry.ModBlockEntities;

public class NodeJarBlockEntity extends TcBlockEntity implements Node {
    private AspectList aspects = new AspectList();
    private AspectList aspectsBase = new AspectList();
    private NodeType nodeType = NodeType.NORMAL;
    private @Nullable NodeModifier nodeModifier;
    private String id = "";
    private long animate;
    private boolean drop = true;

    public NodeJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NODE_JAR.get(), pos, state);
    }

    public long getAnimate() {
        return animate;
    }

    public void setAnimate(long animate) {
        this.animate = animate;
    }

    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public AspectList getAspectsBase() {
        return aspectsBase;
    }

    @Override
    public void setAspects(AspectList aspects) {
        this.aspects = aspects.copy();
        this.aspectsBase = aspects.copy();
    }

    @Override
    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public void setNodeType(NodeType type) {
        nodeType = type;
    }

    @Override
    public @Nullable NodeModifier getNodeModifier() {
        return nodeModifier;
    }

    @Override
    public void setNodeModifier(@Nullable NodeModifier modifier) {
        nodeModifier = modifier;
    }

    @Override
    public int getNodeVisBase(Aspect aspect) {
        return aspectsBase.getAmount(aspect);
    }

    @Override
    public void setNodeVisBase(Aspect aspect, int amount) {
        if (aspectsBase.getAmount(aspect) < amount) {
            aspectsBase.merge(aspect, amount);
        } else {
            aspectsBase.reduce(aspect, aspectsBase.getAmount(aspect) - amount);
        }
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        int overflow = 0;
        if (aspects.getAmount(aspect) + amount > aspectsBase.getAmount(aspect)) {
            overflow = aspects.getAmount(aspect) + amount - aspectsBase.getAmount(aspect);
        }
        aspects.add(aspect, amount - overflow);
        sync();
        return overflow;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (aspects.getAmount(aspect) >= amount) {
            aspects.remove(aspect, amount);
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return aspects.getAmount(aspect) >= amount;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return aspects.getAmount(aspect);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != 9) {
            return super.triggerEvent(id, param);
        }
        if (level != null && level.isClientSide()) {
            animate = System.currentTimeMillis() + 1000L;
        }
        return true;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (drop && level != null) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, NodeJarItem.create(aspects, nodeType, nodeModifier, id));
            drop = false;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        aspectsBase = input.read("aspectsBase", AspectList.CODEC).orElseGet(AspectList::new);
        id = input.getStringOr("nodeId", "");
        nodeType = input.read("type", NodeType.CODEC).orElse(NodeType.NORMAL);
        nodeModifier = input.read("modifier", NodeModifier.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("aspects", AspectList.CODEC, aspects);
        output.store("aspectsBase", AspectList.CODEC, aspectsBase);
        output.putString("nodeId", id);
        output.store("type", NodeType.CODEC, nodeType);
        output.storeNullable("modifier", NodeModifier.CODEC, nodeModifier);
    }
}
