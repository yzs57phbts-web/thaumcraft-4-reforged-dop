package thaumcraft.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumcraft.node.NodeBlockEntity;

public class AuraNodeBlockRenderer extends NodeRenderer<NodeBlockEntity> {
    public AuraNodeBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void fill(NodeBlockEntity node, State state, float partialTicks) {
        fillNodeData(state, node.getAspects(), node.getNodeType(), node.getNodeModifier());
        fillRevealed(state, node.getBlockPos());
        state.drain = drainFor(node, partialTicks);
    }
}
