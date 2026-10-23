package thaumcraft.client.render.entity.eldritch;

import com.mojang.blaze3d.vertex.PoseStack;
import thaumcraft.client.platform.ImmediateGeometry;
import thaumcraft.client.platform.StateBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumcraft.client.platform.BlockEntityRenderState;
import thaumcraft.client.platform.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.EldritchSurfaceGeometry;
import thaumcraft.client.platform.EntityQuadSink;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.eldritch.EldritchPortalBlockEntity;

public final class EldritchPortalBlockRenderer implements StateBlockRenderer<EldritchPortalBlockEntity, EldritchPortalBlockRenderer.State> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/misc/eldritch_portal.png");

    static final class State extends BlockEntityRenderState {
        int count;
        long gameTime;
    }

    public EldritchPortalBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchPortalBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, @Nullable Object breakProgress) {
        StateBlockRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.count = entity.openCount();
        state.gameTime = entity.getLevel() == null ? 0L : entity.getLevel().getGameTime();
    }

    @Override
    public void submit(State state, PoseStack poseStack, ImmediateGeometry collector, CameraRenderState camera) {
        if (state.count < 0) {
            return;
        }
        float width = Mth.clamp(state.count / 5.0F, 0.0F, 1.0F);
        float height = Mth.clamp(state.count / 30.0F, 0.0F, 1.0F);
        if (width == 0.0F || height == 0.0F) {
            return;
        }
        int frame = (int) (System.nanoTime() / 50_000_000L) & 15;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, TcRenderTypes.eldritchPortal(TEXTURE), (pose, buffer) -> {
            EntityQuadSink sink = new EntityQuadSink(pose, buffer);
            int color = FastColor.ARGB32.color(255, 255, 255, 255);
            EldritchSurfaceGeometry.portal(sink, width, height, frame, color, 0xF000F0);
        });
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(EldritchPortalBlockEntity entity) {
        return new AABB(entity.getBlockPos()).inflate(1.0);
    }
}
