package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.texture.TextureAtlas;

import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.EldritchSurfaceGeometry;
import thaumcraft.client.geom.ConnectedTextureGeometry;
import thaumcraft.client.geom.QuadSink;

public final class FocusSurfaces {
    private FocusSurfaces() {
    }

    public static void submitWards(ImmediateGeometry collector, PoseStack poses, int mask, int[] textures, int color) {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        poses.pushPose();
        poses.translate(-0.001, -0.001, -0.001);
        poses.scale(1.002F, 1.002F, 1.002F);
        collector.submitCustomGeometry(poses, TcRenderTypes.additive(atlas.location()), (pose, buffer) -> {
            EntityQuadSink sink = new EntityQuadSink(pose, buffer);
            for (int side = 0; side < 6; side++) {
                if ((mask & (1 << side)) == 0) continue;
                var sprite = atlas.getSprite(Thaumcraft.id("block/warded_glass_" + textures[side]));
                QuadSink mapped = (x, y, z, u, v, argb, light) -> sink.vertex(x, y, z, sprite.getU(u), sprite.getV(v), argb, light);
                ConnectedTextureGeometry.face(mapped, side, color, 0xF000F0);
            }
        });
        poses.popPose();
    }

    public static void submitHole(ImmediateGeometry collector, PoseStack poses, int mask, boolean nearby, float time) {
        poses.pushPose();
        poses.translate(0.001, 0.001, 0.001);
        poses.scale(0.998F, 0.998F, 0.998F);
        var base = Thaumcraft.id(nearby ? "textures/misc/tunnel.png" : "textures/misc/particlefield32.png");
        collector.submitCustomGeometry(poses, TcRenderTypes.eldritchSurface(base), (pose, buffer) ->
            EldritchSurfaceGeometry.faces(new EntityQuadSink(pose, buffer), mask, nearby ? 0.125F : 1.0F,
                0.0F, nearby ? 0xFF191919 : 0xFF808080, 0xF000F0));
        if (nearby) {
            collector.order(1).submitCustomGeometry(poses, TcRenderTypes.additive(Thaumcraft.id("textures/misc/particlefield.png")), (pose, buffer) -> {
                EntityQuadSink sink = new EntityQuadSink(pose, buffer);
                for (int layer = 1; layer < 16; layer++) {
                    EldritchSurfaceGeometry.faces(sink, mask, 0.5F, EldritchSurfaceGeometry.rotationForLayer(layer, time),
                        EldritchSurfaceGeometry.colorForLayer(layer), 0xF000F0);
                }
            });
        }
        poses.popPose();
    }
}
