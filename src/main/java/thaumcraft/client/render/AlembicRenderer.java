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
import thaumcraft.alchemy.AlembicBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.EssentiaBufferBlockEntity;
import thaumcraft.essentia.TubeBlockEntity;

public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity> {
    public static class State {
        int lightCoords;
        BlockPos blockPos = BlockPos.ZERO;
        Direction facing = Direction.NORTH;
        boolean aboveFurnace;
        boolean aboveAlembic;
        @Nullable Aspect filter;
        final boolean[] nozzles = new boolean[6];
    }

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
    }


    @Override
    public void render(AlembicBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        State state = new State();
        state.lightCoords = packedLight;
        state.blockPos = entity.getBlockPos();
        extract(entity, state, partialTick, camera.getPosition());
        submit(state, poseStack, buffers, camera);
    }

    private void extract(AlembicBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition) {
        state.facing = entity.getFacing();
        state.aboveFurnace = entity.isAboveFurnace();
        state.aboveAlembic = entity.isAboveAlembic();
        state.filter = entity.getAspectFilter();
        for (Direction direction : Direction.values()) {
            boolean shown = false;
            if (entity.getLevel() != null && entity.canOutputTo(direction)) {
                Object target = Essentia.getConnectable(entity.getLevel(), entity.getBlockPos(), direction);
                shown = target != null && !(target instanceof TubeBlockEntity) && !(target instanceof EssentiaBufferBlockEntity);
            }
            state.nozzles[direction.ordinal()] = shown;
        }
    }

    private void submit(State state, PoseStack poseStack, MultiBufferSource buffers, Camera camera) {
        AlchemyRendering.alembic(buffers, poseStack, state.lightCoords, true, state.facing, state.aboveFurnace, state.aboveAlembic, state.filter, state.nozzles);
    }
}
