import java.util.ArrayList;
import java.util.List;
import thaumcraft.client.geom.FacingQuads;
import thaumcraft.client.geom.NodeGeometry;
import thaumcraft.client.geom.QuadSink;

public final class NodeGeometryTest {
    private static final class Output implements QuadSink {
        final List<Integer> colors = new ArrayList<>();

        public void vertex(float x, float y, float z, float u, float v, int color, int light) {
            check(light == 0xF000F0, "node glow must use full block and sky light");
            check(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z), "invalid node geometry");
            colors.add(color);
        }
    }

    private static void check(boolean pass, String reason) {
        if (!pass) throw new AssertionError(reason);
    }

    private static int render(boolean full, NodeGeometry.CoreType type, NodeGeometry.Modifier modifier, float distance) {
        Output additive = new Output();
        Output translucent = new Output();
        NodeGeometry.render(new NodeGeometry.Params(
            new FacingQuads.Camera(0, 0, 0.5F, 0.5F, 3), 0.5F, 0.4F, 0.5F, 0,
            full, distance, 64, 0.7F,
            List.of(new NodeGeometry.AspectLayer(0xFF0000, 40, NodeGeometry.ADDITIVE),
                new NodeGeometry.AspectLayer(0x0000FF, 20, NodeGeometry.TRANSLUCENT)),
            type, modifier, 0, 40000000L
        ), additive, translucent);
        if (!full) {
            check(additive.colors.size() == 4 && translucent.colors.isEmpty(), "unrevealed nodes keep their faint core");
        } else {
            boolean dark = type == NodeGeometry.CoreType.DARK || type == NodeGeometry.CoreType.TAINTED;
            check(additive.colors.size() == (dark ? 4 : 8), "incorrect additive node layers");
            check(translucent.colors.size() == (dark ? 8 : 4), "incorrect translucent node layers");
        }
        return additive.colors.getFirst() >>> 24;
    }

    public static void main(String[] args) {
        int faint = render(false, NodeGeometry.CoreType.NORMAL, NodeGeometry.Modifier.NONE, 2);
        int normal = render(true, NodeGeometry.CoreType.NORMAL, NodeGeometry.Modifier.NONE, 2);
        check(normal > faint, "revealed nodes must be brighter than the faint core");
        check(render(true, NodeGeometry.CoreType.NORMAL, NodeGeometry.Modifier.PALE, 2) < normal, "pale modifier lost its fade");
        check(render(true, NodeGeometry.CoreType.NORMAL, NodeGeometry.Modifier.FADING, 2) < normal, "fading modifier lost its fade");
        check(render(true, NodeGeometry.CoreType.NORMAL, NodeGeometry.Modifier.BRIGHT, 2) > normal, "bright modifier lost its glow");
        check(render(true, NodeGeometry.CoreType.NORMAL, NodeGeometry.Modifier.NONE, 63) < normal, "distance fade was lost");
        for (NodeGeometry.CoreType type : NodeGeometry.CoreType.values()) {
            render(true, type, NodeGeometry.Modifier.NONE, 2);
        }
        System.out.println("Node geometry checks passed (lighting, reveal, modifiers, distance, blend layers).");
    }
}
