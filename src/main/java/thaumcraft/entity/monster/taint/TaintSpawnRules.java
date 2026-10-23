package thaumcraft.entity.monster.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import thaumcraft.entity.monster.ThaumicSlime;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.taint.TaintBlock;

/** Surface ecology in infected land, independent of light and slime chunks. */
public final class TaintSpawnRules {
    private TaintSpawnRules() {}

    public static boolean surface(ServerLevelAccessor level, BlockPos pos) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || !TaintBiomes.isTainted(level, pos)) return false;
        var below = pos.below();
        var ground = level.getBlockState(below);
        return (ground.getBlock() instanceof TaintBlock || ground.is(BlockTags.DIRT) || ground.is(BlockTags.BASE_STONE_OVERWORLD))
            && ground.isFaceSturdy(level, below, Direction.UP)
            && ground.getFluidState().isEmpty() && level.getFluidState(pos).isEmpty()
            && level.getBlockState(pos).canBeReplaced()
            && level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) == pos.getY();
    }

    public static boolean swarm(EntityType<TaintSwarm> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return surface(level, pos) && level.getEntitiesOfClass(TaintSwarm.class, new AABB(pos).inflate(16, 8, 16)).size() < 2;
    }

    public static boolean nursery(EntityType<TaintSwarmer> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return surface(level, pos) && level.getEntitiesOfClass(TaintSwarmer.class, new AABB(pos).inflate(32, 8, 32)).isEmpty();
    }

    public static boolean slime(EntityType<ThaumicSlime> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return surface(level, pos) && level.getEntitiesOfClass(ThaumicSlime.class, new AABB(pos).inflate(16, 8, 16)).size() < 4;
    }
}
