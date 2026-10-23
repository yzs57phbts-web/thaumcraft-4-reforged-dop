package thaumcraft.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumcraft.node.EnergizedNodeBlockEntity;

public class EnergizedNodeRenderer extends NodeRenderer<EnergizedNodeBlockEntity> {
    public EnergizedNodeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void fill(EnergizedNodeBlockEntity node, State state, float partialTicks) {
        fillNodeData(state, node.getAuraBase(), node.getNodeType(), node.getNodeModifier());
        state.full = true;
        state.ring = true;
    }
}
