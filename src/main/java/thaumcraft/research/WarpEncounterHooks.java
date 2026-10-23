package thaumcraft.research;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

public final class WarpEncounterHooks {
    private static Spawner spawner;

    private WarpEncounterHooks() {
    }

    public static void install(Spawner encounterSpawner) {
        if (spawner != null) {
            throw new IllegalStateException("Warp encounter spawner is already installed");
        }
        spawner = encounterSpawner;
    }

    public static void spawnGuardians(ServerLevel level, ServerPlayer player, int count) {
        Spawner encounterSpawner = requireSpawner();
        RandomSource random = level.getRandom();
        BlockPos origin = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        for (int index = 0; index < Math.min(8, Math.max(0, count)); index++) {
            for (int attempt = 0; attempt < 50; attempt++) {
                BlockPos pos = candidate(random, origin);
                if (hasSolidTop(level, pos) && encounterSpawner.spawnGuardianAt(level, player, pos)) {
                    break;
                }
            }
        }
    }

    public static void spawnMindSpiders(ServerLevel level, ServerPlayer player, boolean harmless, int count) {
        Spawner encounterSpawner = requireSpawner();
        RandomSource random = level.getRandom();
        BlockPos origin = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        for (int index = 0; index < Math.min(50, Math.max(0, count)); index++) {
            for (int attempt = 0; attempt < 50; attempt++) {
                BlockPos pos = candidate(random, origin);
                if (hasSolidTop(level, pos) && encounterSpawner.spawnMindSpiderAt(level, player, pos, harmless)) {
                    break;
                }
            }
        }
    }

    private static BlockPos candidate(RandomSource random, BlockPos origin) {
        return origin.offset(randomOffset(random), randomOffset(random), randomOffset(random));
    }

    private static int randomOffset(RandomSource random) {
        return (random.nextInt(18) + 7) * (random.nextInt(3) - 1);
    }

    private static boolean hasSolidTop(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static Spawner requireSpawner() {
        if (spawner == null) {
            throw new IllegalStateException("Warp encounter spawner has not been installed");
        }
        return spawner;
    }

    public interface Spawner {
        boolean spawnGuardianAt(ServerLevel level, ServerPlayer player, BlockPos pos);

        boolean spawnMindSpiderAt(ServerLevel level, ServerPlayer player, BlockPos pos, boolean harmless);
    }
}
