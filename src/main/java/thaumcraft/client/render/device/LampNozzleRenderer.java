package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.device.ArcaneBoreBaseBlockEntity;
import thaumcraft.device.LampBlock;

public class LampNozzleRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public static class State {
        int lightCoords;
        net.minecraft.core.BlockPos blockPos;
        Direction facing = Direction.DOWN;
        boolean boreBase;
    }

    public LampNozzleRenderer(BlockEntityRendererProvider.Context context) {
    }

    private State extract(T entity, float partialTicks, int light) {
        State state = new State();
        state.lightCoords = light;
        state.blockPos = entity.getBlockPos();
        state.facing = entity.getBlockState().getValue(LampBlock.FACING);
        state.boreBase = entity.getLevel() != null && entity.getLevel().getBlockEntity(entity.getBlockPos().relative(state.facing)) instanceof ArcaneBoreBaseBlockEntity;
        return state;
    }

    @Override
    public void render(T entity, float partialTicks, PoseStack poseStack, MultiBufferSource collector, int light, int overlay) {
        State state = extract(entity, partialTicks, light);
        DeviceRendering.lampNozzle(collector, poseStack, state.lightCoords, state.facing, state.boreBase);
    }
}
