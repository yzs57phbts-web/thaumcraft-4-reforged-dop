import java.util.HashSet;
import java.util.Set;
import thaumcraft.client.geom.ConnectedTextureGeometry;

public final class ConnectedTextureGeometryTest {
    private static final int[][][] PLANES = {
        {{-1,0,-1},{0,0,-1},{1,0,-1},{-1,0,0},{1,0,0},{-1,0,1},{0,0,1},{1,0,1}},
        {{-1,0,-1},{0,0,-1},{1,0,-1},{-1,0,0},{1,0,0},{-1,0,1},{0,0,1},{1,0,1}},
        {{1,1,0},{0,1,0},{-1,1,0},{1,0,0},{-1,0,0},{1,-1,0},{0,-1,0},{-1,-1,0}},
        {{-1,1,0},{0,1,0},{1,1,0},{-1,0,0},{1,0,0},{-1,-1,0},{0,-1,0},{1,-1,0}},
        {{0,1,-1},{0,1,0},{0,1,1},{0,0,-1},{0,0,1},{0,-1,-1},{0,-1,0},{0,-1,1}},
        {{0,1,1},{0,1,0},{0,1,-1},{0,0,1},{0,0,-1},{0,-1,1},{0,-1,0},{0,-1,-1}}
    };

    public static void main(String[] args) {
        Set<Integer> variants = new HashSet<>();
        for (int side = 0; side < 6; side++) {
            int[][] offsets = PLANES[side];
            for (int mask = 0; mask < 256; mask++) {
                int selected = mask;
                int texture = ConnectedTextureGeometry.texture(side, (x, y, z) -> {
                    for (int index = 0; index < 8; index++) {
                        int[] pos = offsets[index];
                        if (pos[0] == x && pos[1] == y && pos[2] == z) return (selected & (1 << index)) != 0;
                    }
                    throw new AssertionError("connectivity queried outside the original face plane");
                });
                if (texture != ConnectedTextureGeometry.CONNECTED[mask] + 1) throw new AssertionError("connected texture orientation changed on side " + side);
                variants.add(texture);
            }
        }
        int[][] topLeft = {{0,0,0},{0,1,0},{1,1,0},{0,1,1},{0,1,0},{1,1,1}};
        for (int side = 0; side < 6; side++) {
            int[] corner = topLeft[side];
            int[] count = {0, 0};
            ConnectedTextureGeometry.face((x, y, z, u, v, color, light) -> {
                count[0]++;
                if (color != 0x80ABCDEF || light != 0xF000F0) throw new AssertionError("face lost color or light");
                if (u != 0 && u != 1 || v != 0 && v != 1) throw new AssertionError("ward face UV lies outside the sprite");
                if (u == 0 && v == 0) {
                    count[1]++;
                    if (x != corner[0] || y != corner[1] || z != corner[2]) throw new AssertionError("ward texture does not align with its connected neighbors");
                }
            }, side, 0x80ABCDEF, 0xF000F0);
            if (count[0] != 4 || count[1] != 1) throw new AssertionError("incomplete ward face");
        }
        if (variants.size() != 47 || !variants.contains(1) || !variants.contains(47)) throw new AssertionError("missing connected texture variants");
        System.out.println("All 1536 face neighborhoods preserve TC4 orientation and all 47 connected textures");
    }
}
