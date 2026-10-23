package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import thaumcraft.device.ArcaneBoreBlockEntity;

public class ArcaneBoreRenderer implements BlockEntityRenderer<ArcaneBoreBlockEntity> {
    public static class State {
        int lightCoords;
        net.minecraft.core.BlockPos blockPos;
        float rotX;
        float rotZ;
        boolean baseDown;
        int topRotation;
        boolean hasFocus;
        float vortexRotation;
    }

    public ArcaneBoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    private State extract(ArcaneBoreBlockEntity entity, float partialTicks, int light) {
        State state = new State();
        state.lightCoords = light;
        state.blockPos = entity.getBlockPos();
        state.rotX = entity.getRotX() - entity.getVRadX() + partialTicks * entity.getSpeedX();
        state.rotZ = entity.getRotZ() - entity.getVRadZ() + partialTicks * entity.getSpeedZ();
        state.baseDown = entity.getBaseOrientation() == Direction.DOWN;
        state.topRotation = entity.getTopRotation();
        state.hasFocus = entity.hasFocus();
        Minecraft minecraft = Minecraft.getInstance();
        int ticks = minecraft.getCameraEntity() == null ? 0 : minecraft.getCameraEntity().tickCount;
        state.vortexRotation = ticks % 45 + partialTicks;
        return state;
    }

    @Override
    public void render(ArcaneBoreBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource collector, int light, int overlay) {
        State state = extract(entity, partialTicks, light);
        BoreRendering.bore(collector, poseStack, state.lightCoords, new BoreRendering.BoreState(state.rotX, state.rotZ, state.baseDown, state.topRotation, state.hasFocus, state.vortexRotation));
    }

    @Override
    public AABB getRenderBoundingBox(ArcaneBoreBlockEntity entity) {
        return new AABB(entity.getBlockPos()).inflate(1.0);
    }
}
