package thaumcraft.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import thaumcraft.blockentity.NodeJarBlockEntity;

public class JarNodeRenderer extends NodeRenderer<NodeJarBlockEntity> {
    public JarNodeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void fill(NodeJarBlockEntity jar, State state, float partialTicks) {
        fillNodeData(state, jar.getAspects(), jar.getNodeType(), jar.getNodeModifier());
        state.full = true;
        state.size = 0.7F;
        state.yOffset = -0.1F;
    }

    @Override
    public AABB getRenderBoundingBox(NodeJarBlockEntity jar) {
        return new AABB(jar.getBlockPos()).inflate(1.0);
    }
}
