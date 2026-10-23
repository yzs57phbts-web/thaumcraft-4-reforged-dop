package thaumcraft.client.render.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import thaumcraft.client.platform.ImmediateGeometry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import thaumcraft.client.platform.CameraRenderState;
import net.minecraft.util.Mth;
import thaumcraft.client.platform.FishingLines;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.golem.GolemBobber;

import net.minecraft.resources.ResourceLocation;

public final class GolemBobberRenderer extends EntityRenderer<GolemBobber> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");
    public GolemBobberRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    public GolemBobberRenderState createRenderState() {
        return new GolemBobberRenderState();
    }

    public void extractRenderState(GolemBobber entity, GolemBobberRenderState state, float partialTicks) {
        state.ageInTicks = entity.tickCount + partialTicks;
        state.bobberX = Mth.lerp(partialTicks, entity.xo, entity.getX());
        state.bobberY = Mth.lerp(partialTicks, entity.yo, entity.getY());
        state.bobberZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        var fisher = entity.getFisher();
        state.hasFisher = fisher != null;
        if (fisher != null) {
            float yaw = Mth.rotLerp(partialTicks, fisher.yBodyRotO, fisher.yBodyRot) * Mth.DEG_TO_RAD;
            state.fisherX = Mth.lerp(partialTicks, fisher.xo, fisher.getX()) - Mth.cos(yaw) * 0.25 - Mth.sin(yaw) * 0.7;
            state.fisherY = Mth.lerp(partialTicks, fisher.yo, fisher.getY()) + fisher.getEyeHeight() - 0.4;
            state.fisherZ = Mth.lerp(partialTicks, fisher.zo, fisher.getZ()) - Mth.sin(yaw) * 0.25 + Mth.cos(yaw) * 0.7;
        }
    }

    @Override
    public void render(GolemBobber entity, float yaw, float partialTicks, PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        var state = createRenderState();
        extractRenderState(entity, state, partialTicks);
        var collector = new ImmediateGeometry(buffers);
        var camera = new CameraRenderState();
        Billboards.submit(
            poseStack,
            collector,
            camera,
            TcRenderTypes.entityCutoutNoCull(TEXTURE),
            0, 1, 0, 1,
            0.5F,
            0xFFFFFFFF,
            light
        );
        if (state.hasFisher) {
            FishingLines.submit(poseStack, collector, (float) (state.fisherX - state.bobberX),
                (float) (state.fisherY - state.bobberY - 0.25), (float) (state.fisherZ - state.bobberZ));
        }
        super.render(entity, yaw, partialTicks, poseStack, buffers, light);
    }
    @Override
    public ResourceLocation getTextureLocation(GolemBobber entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(GolemBobber entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) { return entity.shouldRender(x, y, z); }
}
