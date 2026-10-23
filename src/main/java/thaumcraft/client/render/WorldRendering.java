package thaumcraft.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import thaumcraft.client.platform.ItemGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.FacingQuads;
import thaumcraft.client.geom.FloatyLineGeometry;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.VertexQuadSink;

public final class WorldRendering {
    public static final QuadSink NOOP = (x, y, z, u, v, argb, light) -> {
    };
    public static final ResourceLocation WISPY = Thaumcraft.id("textures/misc/wispy.png");

    private WorldRendering() {
    }

    public static FacingQuads.Camera camera(Camera camera, BlockPos origin) {
        return new FacingQuads.Camera(
            camera.getYRot(),
            camera.getXRot(),
            (float) (camera.getPosition().x - origin.getX()),
            (float) (camera.getPosition().y - origin.getY()),
            (float) (camera.getPosition().z - origin.getZ())
        );
    }

    public static int ambientLight(@Nullable Level level, BlockPos pos) {
        if (level == null) {
            return LightTexture.FULL_BRIGHT;
        }
        int light = LevelRenderer.getLightColor(level, pos);
        for (Direction direction : Direction.values()) {
            int other = LevelRenderer.getLightColor(level, pos.relative(direction));
            light = LightTexture.pack(Math.max(LightTexture.block(light), LightTexture.block(other)), Math.max(LightTexture.sky(light), LightTexture.sky(other)));
        }
        return light;
    }

    public static int emissiveLight(float level) {
        return Math.clamp((int) level, 0, 240);
    }

    public static void submitFloatyLine(
        MultiBufferSource buffers,
        PoseStack poseStack,
        BlockPos origin,
        double startX,
        double startY,
        double startZ,
        double endX,
        double endY,
        double endZ,
        int color,
        float distance,
        long nanoTime
    ) {
        FloatyLineGeometry.Params params = new FloatyLineGeometry.Params(
            startX,
            startY,
            startZ,
            endX,
            endY,
            endZ,
            origin.getX(),
            origin.getY(),
            origin.getZ(),
            (float) (nanoTime / 30000000L),
            color,
            -0.02F,
            distance,
            FloatyLineGeometry.DEFAULT_WIDTH
        );
        ItemGeometry.submit(
            buffers,
            poseStack,
            TcRenderTypes.additive(WISPY),
            (pose, buffer) -> FloatyLineGeometry.render(params, new VertexQuadSink(pose, buffer))
        );
    }
}
