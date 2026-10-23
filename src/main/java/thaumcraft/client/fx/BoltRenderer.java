package thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.LightningBolt;
import thaumcraft.client.geom.LightningGeometry;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.VertexQuadSink;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class BoltRenderer {
    private static final ResourceLocation LARGE = Thaumcraft.id("textures/misc/p_large.png");
    private static final ResourceLocation SMALL = Thaumcraft.id("textures/misc/p_small.png");
    private static final float[][] OUTER_COLORS = {
        {0.6F, 0.3F, 0.6F},
        {0.6F, 0.6F, 0.1F},
        {0.1F, 0.1F, 0.6F},
        {0.1F, 1.0F, 0.1F},
        {0.6F, 0.1F, 0.1F},
        {0.6F, 0.2F, 0.6F},
        {0.75F, 1.0F, 1.0F}
    };
    private static final float[][] INNER_COLORS = {
        {1.0F, 0.6F, 1.0F},
        {1.0F, 1.0F, 0.1F},
        {0.1F, 0.1F, 1.0F},
        {0.1F, 0.6F, 0.1F},
        {1.0F, 0.1F, 0.1F},
        {0.0F, 0.0F, 0.0F},
        {0.75F, 1.0F, 1.0F}
    };
    private static final double VISIBLE_DISTANCE = 100.0;
    private static final List<LightningBolt> BOLTS = new ArrayList<>();

    private BoltRenderer() {
    }

    public static void add(LightningBolt bolt) {
        BOLTS.add(bolt.finalizeBolt());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            BOLTS.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        BOLTS.removeIf(bolt -> {
            bolt.tick();
            return bolt.isDead();
        });
    }

    @SubscribeEvent
    static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || BOLTS.isEmpty()) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Vec3 look = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot());
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PoseStack.Pose pose = event.getPoseStack().last();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Set<RenderType> used = new LinkedHashSet<>();
        for (LightningBolt bolt : List.copyOf(BOLTS)) {
            if (cameraPos.distanceTo(new Vec3(bolt.startX(), bolt.startY(), bolt.startZ())) > VISIBLE_DISTANCE) {
                continue;
            }
            int type = Math.clamp(bolt.type(), 0, OUTER_COLORS.length - 1);
            boolean translucent = type == 5 || type == 6;
            renderPass(bufferSource, used, pose, LARGE, translucent, bolt, partialTick, cameraPos, look, OUTER_COLORS[type], 0);
            renderPass(bufferSource, used, pose, SMALL, translucent, bolt, partialTick, cameraPos, look, INNER_COLORS[type], 1);
        }
        for (RenderType renderType : used) {
            bufferSource.endBatch(renderType);
        }
    }

    private static void renderPass(MultiBufferSource.BufferSource bufferSource, Set<RenderType> used, PoseStack.Pose pose, ResourceLocation texture, boolean translucent, LightningBolt bolt, float partialTick, Vec3 cameraPos, Vec3 look, float[] color, int pass) {
        RenderType renderType = TcRenderTypes.of(translucent ? TcRenderTypes.Kind.TRANSLUCENT : TcRenderTypes.Kind.ADDITIVE, texture);
        used.add(renderType);
        LightningGeometry.render(
            bolt,
            bolt.age(),
            partialTick,
            (float) cameraPos.x,
            (float) cameraPos.y,
            (float) cameraPos.z,
            (float) look.x,
            (float) look.y,
            (float) look.z,
            color[0],
            color[1],
            color[2],
            pass,
            new VertexQuadSink(pose, bufferSource.getBuffer(renderType))
        );
    }
}
