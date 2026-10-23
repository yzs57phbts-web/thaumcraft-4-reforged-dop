package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumcraft.device.BrainJarBlockEntity;

public class BrainJarRenderer implements BlockEntityRenderer<BrainJarBlockEntity> {
    public static class State {
        int lightCoords;
        net.minecraft.core.BlockPos blockPos;
        float rotation;
    }

    public BrainJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    private State extract(BrainJarBlockEntity entity, float partialTicks, int light) {
        State state = new State();
        state.lightCoords = light;
        state.blockPos = entity.getBlockPos();
        float difference = entity.rota - entity.rotb;
        while (difference >= 3.141593F) {
            difference -= 6.283185F;
        }
        while (difference < -3.141593F) {
            difference += 6.283185F;
        }
        state.rotation = entity.rotb + difference * partialTicks;
        return state;
    }

    @Override
    public void render(BrainJarBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource collector, int light, int overlay) {
        State state = extract(entity, partialTicks, light);
        WardedRendering.brainJar(collector, poseStack, state.lightCoords, state.rotation, WardedRendering.viewerTicks());
    }
}
