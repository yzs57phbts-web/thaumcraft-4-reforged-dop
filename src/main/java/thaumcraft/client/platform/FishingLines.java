package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import thaumcraft.client.geom.FishingLineGeometry;

public final class FishingLines {
    private FishingLines() { }

    public static void submit(PoseStack poses, ImmediateGeometry collector, float dx, float dy, float dz) {
        collector.submitCustomGeometry(poses, RenderType.lines(), (pose, buffer) ->
            FishingLineGeometry.render(dx, dy, dz, (x, y, z, nx, ny, nz) ->
                buffer.addVertex(pose, x, y, z).setColor(0xFF000000).setNormal(pose, nx, ny, nz)));
    }
}
