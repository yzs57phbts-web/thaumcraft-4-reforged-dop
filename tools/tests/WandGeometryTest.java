import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.geom.WandGeometry;

public final class WandGeometryTest {
    private record Vertex(float x, float y, float z, float u, float v) { }
    private static final class Output implements QuadSink {
        final List<Vertex> vertices = new ArrayList<>();
        public void vertex(float x, float y, float z, float u, float v, int color, int light) {
            vertices.add(new Vertex(x, y, z, u, v));
        }
    }
    public static void main(String[] args) {
        for (boolean staff : new boolean[]{false, true}) {
            for (boolean sceptre : new boolean[]{false, true}) {
                Output out = new Output();
                WandGeometry.caps(out, staff, sceptre, 240);
                int count = 2 + (staff ? 1 : 0) + (sceptre ? 1 : 0);
                if (out.vertices.size() != count * 24) throw new AssertionError("Missing cap faces");
                checkBoxes(out);
                float[][] uv = {{4, 2, 6, 4}, {0, 2, 2, 4}, {2, 0, 4, 2}, {4, 2, 6, 0}, {2, 2, 4, 4}, {6, 2, 8, 4}};
                for (int box = 0; box < count; box++) {
                    for (int face = 0; face < 6; face++) {
                        float[] r = uv[face];
                        float[][] expected = {{r[2], r[1]}, {r[0], r[1]}, {r[0], r[3]}, {r[2], r[3]}};
                        for (int corner = 0; corner < 4; corner++) {
                            Vertex vertex = out.vertices.get(box * 24 + face * 4 + corner);
                            if (Math.abs(vertex.u * 32 - expected[corner][0]) > 0.00001F
                                || Math.abs(vertex.v * 32 - expected[corner][1]) > 0.00001F) {
                                throw new AssertionError("Cap UV differs from the original 32x32 ModelBox");
                            }
                        }
                    }
                }
            }
            Output rod = new Output();
            WandGeometry.rod(rod, staff, 240, -1);
            checkBoxes(rod);
            if (rod.vertices.stream().map(Vertex::v).max(Float::compare).orElseThrow() != 28.0F / 32.0F) {
                throw new AssertionError("Rod texture height differs from TC4");
            }
        }
        Output focus = new Output();
        WandGeometry.focus(focus, false, -1, 240);
        checkBoxes(focus);
        System.out.println("Wand cap UV, closed surfaces and outward winding checks passed");
    }
    private static void checkBoxes(Output out) {
        for (int box = 0; box < out.vertices.size(); box += 24) {
            Vector3f center = new Vector3f();
            for (int i = 0; i < 24; i++) {
                Vertex v = out.vertices.get(box + i);
                center.add(v.x, v.y, v.z);
            }
            center.div(24);
            for (int i = 0; i < 24; i += 4) {
                Vertex a = out.vertices.get(box + i), b = out.vertices.get(box + i + 1), d = out.vertices.get(box + i + 3);
                Vector3f normal = new Vector3f(b.x - a.x, b.y - a.y, b.z - a.z)
                    .cross(new Vector3f(d.x - a.x, d.y - a.y, d.z - a.z));
                if (normal.dot(new Vector3f(a.x, a.y, a.z).sub(center)) <= 0) {
                    throw new AssertionError("Inward or degenerate wand face");
                }
            }
        }
    }
}
