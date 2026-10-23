package thaumcraft.world.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.registry.ModBlocks;
import thaumcraft.taint.TaintBiomes;

public final class TaintDecorationHook {
    private TaintDecorationHook() {
    }

    public static void decorate(WorldGenLevel level, RandomSource random, BlockPos chunkOrigin) {
        int originX = chunkOrigin.getX();
        int originZ = chunkOrigin.getZ();
        int blobs = random.nextInt(3);
        for (int index = 0; index < blobs; index++) {
            int x = originX + random.nextInt(16);
            int z = originZ + random.nextInt(16);
            BlockPos center = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
            placeCrustBlob(level, random, center);
        }
        // Seed the growth stages directly; otherwise new tainted land can stay
        // bare until random spreading happens to evolve ordinary fibres.
        for (int attempt = 0; attempt < 18; attempt++) {
            int x = originX + random.nextInt(16);
            int z = originZ + random.nextInt(16);
            BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            BlockPos ground = pos.below();
            BlockState soil = level.getBlockState(ground);
            if (!level.getBiome(pos).is(TaintBiomes.TAINTED_LAND) || !level.getFluidState(pos).isEmpty()
                || !level.getBlockState(pos).canBeReplaced()
                || !(soil.is(BlockTags.DIRT) || soil.is(ModBlocks.TAINTED_SOIL.get()))) continue;
            int choice = random.nextInt(12);
            var plant = choice == 0 ? ModBlocks.SPORE_STALK.get()
                : choice < 4 ? ModBlocks.TAINTED_PLANT.get() : ModBlocks.TAINTED_GRASS.get();
            level.setBlock(ground, ModBlocks.TAINTED_SOIL.get().defaultBlockState(), 2);
            level.setBlock(pos, plant.defaultBlockState(), 2);
        }
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = originX + random.nextInt(16);
            int z = originZ + random.nextInt(16);
            BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
            if (!level.getBiome(pos).is(TaintBiomes.TAINTED_LAND) || !level.isEmptyBlock(pos)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite())) {
                    level.setBlock(pos, ModBlocks.FIBROUS_TAINT.get().defaultBlockState(), 2);
                    break;
                }
            }
        }
    }

    private static void placeCrustBlob(WorldGenLevel level, RandomSource random, BlockPos ground) {
        if (!level.getBiome(ground).is(TaintBiomes.TAINTED_LAND)
            || !level.getFluidState(ground.above()).isEmpty()
            || !(level.getBlockState(ground).is(BlockTags.DIRT)
                || level.getBlockState(ground).is(BlockTags.BASE_STONE_OVERWORLD))) return;
        // TC4 used the small, three-pass surface blob. Replacing only existing
        // rock buried the entire growth and prevented exposed swarm nests.
        BlockPos center = ground.above();
        for (int pass = 0; pass < 3; pass++) {
            int rx = random.nextInt(2), ry = random.nextInt(2), rz = random.nextInt(2);
            float radius = (rx + ry + rz) / 3.0F + 0.5F;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-rx, -ry, -rz), center.offset(rx, ry, rz))) {
                if (pos.distSqr(center) > radius * radius || !level.getFluidState(pos).isEmpty()
                    || !level.getBiome(pos).is(TaintBiomes.TAINTED_LAND)) continue;
                BlockState old = level.getBlockState(pos);
                if (old.canBeReplaced() || old.is(BlockTags.DIRT) || old.is(BlockTags.BASE_STONE_OVERWORLD)) {
                    level.setBlock(pos, ModBlocks.CRUSTED_TAINT.get().defaultBlockState(), 2);
                }
            }
            center = center.offset(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
        }
    }
}
