package thaumcraft.world.gen;

public final class HilltopRuinPlacement {
    public static final int CELL_CHUNKS = 16;
    public static final int MIN_DISTANCE_BLOCKS = 192;
    private static final long SALT = 0x48A19B72D6E350CFL;

    private HilltopRuinPlacement() {
    }

    public static boolean isCandidate(long seed, int chunkX, int chunkZ) {
        int cellX = Math.floorDiv(chunkX, CELL_CHUNKS);
        int cellZ = Math.floorDiv(chunkZ, CELL_CHUNKS);
        Candidate selected = candidate(seed, cellX, cellZ);
        if (selected.x != chunkX || selected.z != chunkZ) {
            return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                Candidate other = candidate(seed, cellX + dx, cellZ + dz);
                long distanceX = Math.max(0L, Math.abs(selected.x - other.x) * 16L - 15L);
                long distanceZ = Math.max(0L, Math.abs(selected.z - other.z) * 16L - 15L);
                if (distanceX * distanceX + distanceZ * distanceZ < (long) MIN_DISTANCE_BLOCKS * MIN_DISTANCE_BLOCKS
                    && higherPriority(other, selected)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean higherPriority(Candidate first, Candidate second) {
        int comparison = Long.compareUnsigned(first.priority, second.priority);
        return comparison > 0 || comparison == 0 && (first.x < second.x || first.x == second.x && first.z < second.z);
    }

    private static Candidate candidate(long seed, int cellX, int cellZ) {
        long value = mix(seed ^ mix(cellX * 341873128712L) ^ mix(cellZ * 132897987541L) ^ SALT);
        long x = (long) cellX * CELL_CHUNKS + (mix(value) & (CELL_CHUNKS - 1));
        long z = (long) cellZ * CELL_CHUNKS + (mix(value + 0x9E3779B97F4A7C15L) & (CELL_CHUNKS - 1));
        return new Candidate(x, z, mix(value + 0x3C6EF372FE94F82AL));
    }

    private static long mix(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private record Candidate(long x, long z, long priority) {
    }
}
