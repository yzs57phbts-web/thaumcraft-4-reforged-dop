import java.util.ArrayList;
import java.util.List;
import thaumcraft.client.geom.FishingLineGeometry;

public final class FishingLineGeometryTest {
    public static void main(String[] args) {
        for (float[] delta : List.of(new float[]{8, 2, -4}, new float[]{-7, -3, 5}, new float[]{0, 2, 0}, new float[]{1, 0, 0})) {
            List<float[]> vertices = new ArrayList<>();
            FishingLineGeometry.render(delta[0], delta[1], delta[2], (x, y, z, nx, ny, nz) -> vertices.add(new float[]{x, y, z, nx, ny, nz}));
            if (vertices.size() != 32) throw new AssertionError("incomplete fishing line");
            checkPosition(vertices.getFirst(), 0, 0.25F, 0);
            checkPosition(vertices.getLast(), delta[0], delta[1] + 0.25F, delta[2]);
            for (int i = 0; i < vertices.size(); i++) {
                float[] vertex = vertices.get(i);
                for (float value : vertex) if (!Float.isFinite(value)) throw new AssertionError("invalid line vertex");
                if (Math.abs(vertex[3] * vertex[3] + vertex[4] * vertex[4] + vertex[5] * vertex[5] - 1) > 1.0E-5F) throw new AssertionError("invalid line normal");
                if (i > 0 && i % 2 == 0) {
                    float[] previous = vertices.get(i - 1);
                    checkPosition(vertex, previous[0], previous[1], previous[2]);
                }
            }
        }
        FishingLineGeometry.render(0, 0, 0, (x, y, z, nx, ny, nz) -> { throw new AssertionError("degenerate line should not emit vertices"); });
        System.out.println("Fishing line endpoints, continuity and normals passed");
    }

    private static void checkPosition(float[] vertex, float x, float y, float z) {
        if (Math.abs(vertex[0] - x) > 1.0E-5F || Math.abs(vertex[1] - y) > 1.0E-5F || Math.abs(vertex[2] - z) > 1.0E-5F) throw new AssertionError("line endpoint drifted");
    }
}
