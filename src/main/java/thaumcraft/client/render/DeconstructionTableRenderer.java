package thaumcraft.client.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import thaumcraft.client.platform.ItemGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.arcane.DeconstructionTableBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.VertexQuadSink;
import thaumcraft.registry.ModItems;

public class DeconstructionTableRenderer implements BlockEntityRenderer<DeconstructionTableBlockEntity> {
    private static final float TAG_SCALE = 0.024F;

    public static class State {
        int lightCoords;
        BlockPos blockPos = BlockPos.ZERO;
        final ItemSlot thaumometer = new ItemSlot();
        final ItemSlot item = new ItemSlot();
        @Nullable Aspect aspect;
        float ticks;
        float hover;
    }

    public DeconstructionTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DeconstructionTableBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        State state = new State();
        state.lightCoords = packedLight;
        state.blockPos = entity.getBlockPos();
        extract(entity, state, partialTick, camera.getPosition());
        submit(state, poseStack, buffers, camera);
    }

    private void extract(DeconstructionTableBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition) {
        state.thaumometer.clear();
        state.item.clear();
        state.aspect = null;
        if (entity.getLevel() == null) {
            return;
        }
        int seed = (int) entity.getBlockPos().asLong();
        state.thaumometer.set(new ItemStack(ModItems.THAUMOMETER.get()), ItemDisplayContext.FIXED, entity.getLevel(), seed);
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partialTicks;
        state.hover = Mth.sin(state.ticks / 14.0F) * 0.2F + 0.2F;
        ItemStack stack = entity.getItem(0);
        if (!stack.isEmpty()) {
            state.item.set(stack.copyWithCount(1), ItemDisplayContext.GROUND, entity.getLevel(), seed + 1);
        }
        state.aspect = entity.getAspect();
    }

    private void submit(State state, PoseStack poseStack, MultiBufferSource buffers, Camera camera) {
        if (!state.thaumometer.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.92F, 0.5F);
            poseStack.scale(0.8F, 0.8F, 0.8F);
            state.thaumometer.submit(poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.15F + Mth.sin(state.hover) * 0.1F + 0.1F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks % 360.0F));
            state.item.submit(poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        Aspect aspect = state.aspect;
        if (aspect != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.081F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.ticks % 360.0F));
            poseStack.scale(TAG_SCALE, TAG_SCALE, TAG_SCALE);
            ResourceLocation image = aspect.getImage();
            int color = 0xCC000000 | aspect.getColor() & 0xFFFFFF;
            int light = state.lightCoords;
            ItemGeometry.submit(buffers, poseStack, TcRenderTypes.translucent(image), (pose, buffer) -> {
                VertexQuadSink sink = new VertexQuadSink(pose, buffer);
                sink.vertex(-8.0F, -8.0F, 0.0F, 0.0F, 0.0F, color, light);
                sink.vertex(-8.0F, 8.0F, 0.0F, 0.0F, 1.0F, color, light);
                sink.vertex(8.0F, 8.0F, 0.0F, 1.0F, 1.0F, color, light);
                sink.vertex(8.0F, -8.0F, 0.0F, 1.0F, 0.0F, color, light);
            });
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(DeconstructionTableBlockEntity entity) {
        return new AABB(entity.getBlockPos()).expandTowards(0.0, 1.0, 0.0);
    }
}
