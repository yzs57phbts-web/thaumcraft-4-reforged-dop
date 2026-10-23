package thaumcraft.client.render.entity.eldritch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.entity.projectile.EldritchOrb;

public final class EldritchOrbRenderer extends EntityRenderer<EldritchOrb> {
    private static final int[] ENERGY_COLORS = {16777215, 16777086, 16727041, 37119, 40960, 15650047, 5592439};

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(EldritchOrb entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        renderOrb(entity, partialTick, poseStack, buffers);
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    public static void renderOrb(Entity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        float age = entity.tickCount + partialTick;
        float spin = age / 80.0F;
        float grow = Math.min(age, 10.0F) / 10.0F;
        VertexConsumer buffer = buffers.getBuffer(TcRenderTypes.dragonRays());
        poseStack.pushPose();
        Random random = new Random(entity.getId());
        Quaternionf rotation = new Quaternionf();
        for (int index = 0; index < 12; index++) {
            rotation.rotationX(random.nextFloat() * (float) (Math.PI * 2))
                .rotateY(random.nextFloat() * (float) (Math.PI * 2))
                .rotateZ(random.nextFloat() * (float) (Math.PI * 2))
                .rotateX(random.nextFloat() * (float) (Math.PI * 2))
                .rotateY(random.nextFloat() * (float) (Math.PI * 2))
                .rotateZ(random.nextFloat() * (float) (Math.PI * 2) + spin * (float) (Math.PI * 2));
            poseStack.mulPose(rotation);
            float length = (random.nextFloat() * 20.0F + 5.0F) / (30.0F / grow);
            float width = (random.nextFloat() * 2.0F + 1.0F) / (30.0F / grow);
            int outer = FastColor.ARGB32.color(0, ENERGY_COLORS[index / 2 + 1]);
            PoseStack.Pose pose = poseStack.last();
            buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(0xFFFFFFFF);
            buffer.addVertex(pose, -0.866F * width, length, -0.5F * width).setColor(outer);
            buffer.addVertex(pose, 0.866F * width, length, -0.5F * width).setColor(outer);
            buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(0xFFFFFFFF);
            buffer.addVertex(pose, 0.866F * width, length, -0.5F * width).setColor(outer);
            buffer.addVertex(pose, 0.0F, length, width).setColor(outer);
            buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(0xFFFFFFFF);
            buffer.addVertex(pose, 0.0F, length, width).setColor(outer);
            buffer.addVertex(pose, -0.866F * width, length, -0.5F * width).setColor(outer);
        }
        poseStack.popPose();
        float u0 = ((int) age % 13) / 16.0F;
        Billboards.submit(poseStack, buffers, Minecraft.getInstance().gameRenderer.getMainCamera(),
            TcRenderTypes.translucent(ParticleFx.PARTICLES), u0, u0 + 0.0624375F, 0.1875F, 0.25F, 1.0F, 0xFFFFFFFF, 0xF000F0);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchOrb entity) {
        return ParticleFx.PARTICLES;
    }
}
