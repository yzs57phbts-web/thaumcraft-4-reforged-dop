package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import thaumcraft.device.CrystalClusterBlock;
import thaumcraft.device.CrystalClusterBlockEntity;

public class CrystalClusterRenderer implements BlockEntityRenderer<CrystalClusterBlockEntity> {
    public static class State {
        int lightCoords;
        net.minecraft.core.BlockPos blockPos;
        int kind;
        Direction facing = Direction.UP;
        long seed;
        int rotation;
        int ticks;
    }

    public CrystalClusterRenderer(BlockEntityRendererProvider.Context context) {
    }

    private State extract(CrystalClusterBlockEntity entity, float partialTicks, int light) {
        State state = new State();
        state.lightCoords = light;
        state.blockPos = entity.getBlockPos();
        if (entity.getBlockState().getBlock() instanceof CrystalClusterBlock block) {
            state.kind = block.getKind();
            state.facing = entity.getBlockState().getValue(CrystalClusterBlock.FACING);
            state.seed = state.kind + entity.getBlockPos().getX() + entity.getBlockPos().getY() * entity.getBlockPos().getZ();
            state.rotation = Math.floorMod(entity.getBlockPos().hashCode(), 4);
        }
        state.ticks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
        return state;
    }

    @Override
    public void render(CrystalClusterBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource collector, int light, int overlay) {
        State state = extract(entity, partialTicks, light);
        if (state.kind == CrystalClusterBlock.STRANGE) {
            DeviceRendering.strangeCrystal(collector, poseStack, state.lightCoords, state.facing, state.rotation, state.ticks);
        } else {
            DeviceRendering.crystalCluster(collector, poseStack, state.kind, state.facing, state.seed, state.ticks);
        }
    }
}
