package thaumcraft.node;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.vis.VisNode;

public class EnergizedNodeBlockEntity extends VisNode implements NodeView {
    private AspectList auraBase = new AspectList()
        .add(Aspect.AIR, 20)
        .add(Aspect.FIRE, 20)
        .add(Aspect.EARTH, 20)
        .add(Aspect.WATER, 20)
        .add(Aspect.ORDER, 20)
        .add(Aspect.ENTROPY, 20);
    private AspectList visBase = new AspectList();
    private AspectList vis = new AspectList();
    private NodeType nodeType = NodeType.NORMAL;
    private @Nullable NodeModifier nodeModifier;
    private String id = "blank";

    public EnergizedNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGIZED_NODE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergizedNodeBlockEntity entity) {
        entity.tick(level);
    }

    private void tick(Level tickLevel) {
        tickNetwork(tickLevel);
        if (tickLevel.isClientSide()) {
            return;
        }
        if (nodeType == NodeType.UNSTABLE && tickLevel.getRandom().nextInt(500) == 1) {
            visBase = new AspectList();
        }
        if (visBase.size() == 0 && auraBase.size() > 0) {
            setupNode();
        }
        vis = visBase.copy();
    }

    public void setupNode() {
        visBase = new AspectList();
        AspectList primals = AspectHelper.reduceToPrimals(auraBase, true);
        for (Aspect aspect : primals.getAspects()) {
            int amount = primals.getAmount(aspect);
            if (nodeModifier == NodeModifier.BRIGHT) {
                amount = (int) (amount * 1.2F);
            }
            if (nodeModifier == NodeModifier.PALE) {
                amount = (int) (amount * 0.8F);
            }
            if (nodeModifier == NodeModifier.FADING) {
                amount = (int) (amount * 0.5F);
            }
            amount = Mth.floor(Mth.sqrt((float) amount));
            if (nodeType == NodeType.UNSTABLE && level != null) {
                amount += level.getRandom().nextInt(5) - 2;
            }
            if (amount >= 1) {
                visBase.merge(aspect, amount);
            }
        }
        sync();
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public int consumeVis(Aspect aspect, int amount) {
        int drain = Math.min(vis.getAmount(aspect), amount);
        if (drain > 0) {
            vis.reduce(aspect, drain);
        }
        return drain;
    }

    public String getId() {
        if ((id.isEmpty() || id.equals("blank")) && level != null) {
            id = level.dimension().location() + ":" + worldPosition.getX() + ":" + worldPosition.getY() + ":" + worldPosition.getZ();
            setChanged();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
        setChanged();
    }

    public AspectList getAuraBase() {
        return auraBase;
    }

    public AspectList getAspects() {
        return visBase;
    }

    public AspectList getCurrentVis() {
        return vis;
    }

    public void setAspects(AspectList aspects) {
        auraBase = aspects;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public @Nullable NodeModifier getNodeModifier() {
        return nodeModifier;
    }

    public void setNodeModifier(@Nullable NodeModifier nodeModifier) {
        this.nodeModifier = nodeModifier;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        id = input.getStringOr("nodeId", "blank");
        nodeType = input.read("type", NodeType.CODEC).orElse(NodeType.NORMAL);
        nodeModifier = input.read("modifier", NodeModifier.CODEC).orElse(null);
        visBase = input.read("visBase", AspectList.CODEC).orElseGet(AspectList::new);
        auraBase = input.read("auraBase", AspectList.CODEC).orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("nodeId", getId());
        output.store("type", NodeType.CODEC, nodeType);
        output.storeNullable("modifier", NodeModifier.CODEC, nodeModifier);
        output.store("visBase", AspectList.CODEC, visBase);
        output.store("auraBase", AspectList.CODEC, auraBase);
    }
}
