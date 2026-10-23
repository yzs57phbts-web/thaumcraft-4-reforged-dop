package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import thaumcraft.device.ArcaneBoreBaseBlockEntity;

public class ArcaneBoreBaseRenderer implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity> {
    public static class State {
        int lightCoords;
        net.minecraft.core.BlockPos blockPos;
        Direction orientation = Direction.NORTH;
    }

    public ArcaneBoreBaseRenderer(BlockEntityRendererProvider.Context context) {
    }

    private State extract(ArcaneBoreBaseBlockEntity entity, float partialTicks, int light) {
        State state = new State();
        state.lightCoords = light;
        state.blockPos = entity.getBlockPos();
        state.orientation = entity.getOrientation();
        return state;
    }

    @Override
    public void render(ArcaneBoreBaseBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource collector, int light, int overlay) {
        State state = extract(entity, partialTicks, light);
        BoreRendering.boreBase(collector, poseStack, state.lightCoords, state.orientation);
    }
}
