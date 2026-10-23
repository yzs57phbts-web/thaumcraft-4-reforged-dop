import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import thaumcraft.world.gen.HilltopRuinPlacement;

public final class HilltopRuinPlacementTest {
    private record Point(int x, int z) {
    }

    private static List<Point> scan(long seed, int minX, int minZ, int width) {
        List<Point> points = new ArrayList<>();
        Set<Point> cells = new HashSet<>();
        for (int x = minX; x < minX + width; x++) {
            for (int z = minZ; z < minZ + width; z++) {
                if (!HilltopRuinPlacement.isCandidate(seed, x, z)) continue;
                points.add(new Point(x, z));
                if (!cells.add(new Point(Math.floorDiv(x, 16), Math.floorDiv(z, 16)))) {
                    throw new AssertionError("More than one ruin per placement cell");
                }
            }
        }
        for (int a = 0; a < points.size(); a++) {
            for (int b = a + 1; b < points.size(); b++) {
                Point first = points.get(a), second = points.get(b);
                long dx = Math.max(0L, Math.abs((long) first.x - second.x) * 16L - 15L);
                long dz = Math.max(0L, Math.abs((long) first.z - second.z) * 16L - 15L);
                if (dx * dx + dz * dz < 192L * 192L) throw new AssertionError("Nearby ruins: " + first + " / " + second);
            }
        }
        Set<Point> reversed = new HashSet<>();
        for (int x = minX + width - 1; x >= minX; x--) {
            for (int z = minZ + width - 1; z >= minZ; z--) {
                if (HilltopRuinPlacement.isCandidate(seed, x, z)) reversed.add(new Point(x, z));
            }
        }
        if (!reversed.equals(new HashSet<>(points))) throw new AssertionError("Placement depends on generation order");
        return points;
    }

    public static void main(String[] args) {
        long[] seeds = {0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, -591195973969373317L, 8616931143624757498L};
        for (long seed : seeds) {
            List<Point> points = scan(seed, -256, -256, 512);
            if (points.size() < 400 || points.size() > 1024) throw new AssertionError("Unexpected ruin density: " + points.size());
            System.out.println("Seed " + seed + ": " + points.size() + " candidate ruins across 262144 chunks; minimum spacing and reversed generation order passed.");
        }
        List<Point> local = scan(8616931143624757498L, Math.floorDiv(-3775, 16), Math.floorDiv(-1045, 16), 26);
        System.out.println("Reported 400-block area: " + local.size() + " candidate ruins before terrain checks: " + local);
        if (new HashSet<>(scan(1L, -128, -128, 256)).equals(new HashSet<>(scan(2L, -128, -128, 256)))) {
            throw new AssertionError("Placement ignores the world seed");
        }
        System.out.println("Hilltop ruin spacing passed across positive/negative coordinates, region boundaries, six seeds, and all possible within-chunk offsets.");
    }
}
