package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.geom.FloatyLineGeometry;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.VertexQuadSink;
import thaumcraft.client.render.WorldRendering;
import thaumcraft.entity.monster.CultistCleric;

public final class CultistClericRenderer extends CultistMobRenderer<CultistCleric> {
    private float ritualBob;

    public CultistClericRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new RitualistBeamLayer(this));
    }

    @Override
    protected void scale(CultistCleric entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        if (entity.isRitualist()) {
            ritualBob = (float) Math.sin((entity.tickCount + partialTick + new Random(entity.getId()).nextInt(1000)) / 9.0F) * 0.1F + 0.21F;
            poseStack.translate(0.0, ritualBob, 0.0);
        } else {
            ritualBob = 0.0F;
        }
    }

    private static final class RitualistBeamLayer extends RenderLayer<CultistCleric, HumanoidModel<CultistCleric>> {
        private static final ResourceLocation TEXTURE = WorldRendering.WISPY;

        private RitualistBeamLayer(RenderLayerParent<CultistCleric, HumanoidModel<CultistCleric>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, CultistCleric entity,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            if (!entity.isRitualist() || !entity.hasRestriction()) {
                return;
            }
            BlockPos home = entity.getRestrictCenter();
            float ritualBob = (float) Math.sin((entity.tickCount + partialTick + new Random(entity.getId()).nextInt(1000)) / 9.0F) * 0.1F + 0.21F;
            float distance = Math.min(ageInTicks, 10.0F) / 10.0F;
            FloatyLineGeometry.Params params = new FloatyLineGeometry.Params(
                0.0, entity.getEyeHeight() * 1.2, 0.0,
                home.getX() + 0.5 - entity.getX(), home.getY() + 1.5 - entity.getY() - ritualBob,
                home.getZ() + 0.5 - entity.getZ(),
                0.0, 0.0, 0.0, (float) (System.nanoTime() / 30000000L),
                1114129, -0.03F, distance, 0.25F
            );
            FloatyLineGeometry.render(params, new VertexQuadSink(poseStack.last(), buffers.getBuffer(TcRenderTypes.translucent(TEXTURE))));
        }
    }
}
