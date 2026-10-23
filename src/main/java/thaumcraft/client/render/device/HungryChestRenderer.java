package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import thaumcraft.device.HungryChestBlock;
import thaumcraft.device.HungryChestBlockEntity;

public class HungryChestRenderer implements BlockEntityRenderer<HungryChestBlockEntity> {
    public static class State {
        int lightCoords;
        net.minecraft.core.BlockPos blockPos;
        Direction facing = Direction.NORTH;
        float lid;
    }

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    private State extract(HungryChestBlockEntity entity, float partialTicks, int light) {
        State state = new State();
        state.lightCoords = light;
        state.blockPos = entity.getBlockPos();
        state.facing = entity.getBlockState().getValue(HungryChestBlock.FACING);
        state.lid = Mth.lerp(partialTicks, entity.prevLidAngle, entity.lidAngle);
        return state;
    }

    @Override
    public void render(HungryChestBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource collector, int light, int overlay) {
        State state = extract(entity, partialTicks, light);
        WardedRendering.hungryChest(collector, poseStack, state.lightCoords, state.facing, state.lid);
    }
}
