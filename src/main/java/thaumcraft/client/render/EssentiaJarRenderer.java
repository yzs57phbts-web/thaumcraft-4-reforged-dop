package thaumcraft.client.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import thaumcraft.aspect.Aspect;
import thaumcraft.essentia.JarBlockEntity;

public class EssentiaJarRenderer implements BlockEntityRenderer<JarBlockEntity> {
    public static class State {
        int lightCoords;
        BlockPos blockPos = BlockPos.ZERO;
        int posX;
        @Nullable Aspect aspect;
        int amount;
        int maxAmount;
        @Nullable Aspect filter;
        Direction facing = Direction.NORTH;
    }

    public EssentiaJarRenderer(BlockEntityRendererProvider.Context context) {
    }


    @Override
    public void render(JarBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        State state = new State();
        state.lightCoords = packedLight;
        state.blockPos = entity.getBlockPos();
        extract(entity, state, partialTick, camera.getPosition());
        submit(state, poseStack, buffers, camera);
    }

    private void extract(JarBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition) {
        state.posX = entity.getBlockPos().getX();
        state.aspect = entity.getAspect();
        state.amount = entity.getAmount();
        state.maxAmount = entity.getMaxAmount();
        state.filter = entity.getAspectFilter();
        state.facing = entity.getFacing();
    }

    private void submit(State state, PoseStack poseStack, MultiBufferSource buffers, Camera camera) {
        EssentiaJarRendering.submit(buffers, poseStack, state.lightCoords, state.posX, state.aspect, state.amount, state.maxAmount, state.filter, state.facing);
    }
}
