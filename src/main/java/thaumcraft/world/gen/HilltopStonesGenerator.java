package thaumcraft.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import thaumcraft.Thaumcraft;
import thaumcraft.block.ObsidianTotemBlock;
import thaumcraft.registry.ModBlocks;

public final class HilltopStonesGenerator {
    private HilltopStonesGenerator() {
    }

    private static boolean valid(LevelAccessor level, int x, int y, int z) {
        return y >= 85 && LegacyGen.locationIsValidSpawn(level, x, y, z);
    }

    public static boolean generate(LevelAccessor level, Random rand, int i, int j, int k) {
        if (thaumcraft.world.gen.StructurePlacementGuard.intersectsStructure(level, new net.minecraft.world.level.levelgen.structure.BoundingBox(i - 4, j - 6, k - 4, i + 4, j + 9, k + 4))) return false;
        if (!valid(level, i - 2, j, k - 2) || !valid(level, i, j, k) || !valid(level, i + 2, j, k) || !valid(level, i + 2, j, k + 2) || !valid(level, i, j, k + 2)) {
            return false;
        }
        Biome biome = level.getBiome(new BlockPos(i, j, k)).value();
        BlockState ground = LegacyGen.get(level, i, j - 1, k);
        BlockState replace = ground.is(BlockTags.DIRT) ? ground : Blocks.GRASS_BLOCK.defaultBlockState();
        boolean genVines = !biome.coldEnoughToSnow(new BlockPos(i, j, k));
        BlockState tile = ModBlocks.OBSIDIAN_TILE.get().defaultBlockState();
        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                if ((x == i - 3 || x == i + 3) && (z == k - 3 || z == k + 3)) {
                    continue;
                }
                LegacyGen.set(level, x, j, z, rand.nextBoolean() ? tile : Blocks.OBSIDIAN.defaultBlockState());
                boolean stop = false;
                List<BlockPos> column = new ArrayList<>();
                for (int y = 1; y < 5; y++) {
                    if (j - y >= level.getMinBuildHeight()) {
                        BlockState below = LegacyGen.get(level, x, j - y, z);
                        if (below.is(Blocks.SNOW) || below.is(BlockTags.SMALL_FLOWERS) || below.is(Blocks.SHORT_GRASS) || below.is(Blocks.FERN) || below.isAir()) {
                            LegacyGen.set(level, x, j - y, z, replace);
                        }
                    }
                    if (x == i && z == k && y == 1) {
                        LegacyGen.set(level, x, j + y, z, tile);
                        BlockPos chestPos = new BlockPos(x, j + y + 1, z);
                        LegacyGen.set(level, x, j + y + 1, z, Blocks.CHEST.defaultBlockState());
                        MoundGenerator.loot(level, chestPos, BuiltInLootTables.SIMPLE_DUNGEON, rand);
                        BlockPos spawnerPos = new BlockPos(x, j + y - 1, z);
                        LegacyGen.set(level, x, j + y - 1, z, Blocks.SPAWNER.defaultBlockState());
                        EntityType<?> wisp = BuiltInRegistries.ENTITY_TYPE.getOptional(Thaumcraft.id("wisp")).orElse(null);
                        if (wisp != null) {
                            MoundGenerator.spawner(level, spawnerPos, wisp);
                        }
                    }
                    boolean edge = (x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1
                        || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1;
                    if (!stop && edge) {
                        column.add(new BlockPos(x, j + y, z));
                        LegacyGen.set(level, x, j + y, z, ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState());
                        if (y >= 2 && rand.nextBoolean()) {
                            stop = true;
                            if (genVines) {
                                vinesAround(level, rand, x, j + y, z);
                            }
                        }
                    }
                }
                for (int index = 0; index < column.size(); index++) {
                    BlockPos pos = column.get(index);
                    LegacyGen.set(level, pos.getX(), pos.getY(), pos.getZ(), totemState(pos, index < column.size() - 1, index > 0));
                }
            }
        }
        return true;
    }

    private static BlockState totemState(BlockPos pos, boolean above, boolean below) {
        ObsidianTotemBlock.Part part;
        if (above) {
            part = ObsidianTotemBlock.Part.SHADED;
        } else if (below) {
            part = ObsidianTotemBlock.Part.CARVED;
        } else {
            part = ObsidianTotemBlock.Part.BASE;
        }
        int carving = Mth.positiveModulo(pos.getX() % 4 + pos.getZ() % 4 + pos.getY() % 4, 4);
        return ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState()
            .setValue(ObsidianTotemBlock.PART, part)
            .setValue(ObsidianTotemBlock.CARVING, carving);
    }

    private static void vinesAround(LevelAccessor level, Random rand, int x, int y, int z) {
        if (rand.nextInt(3) == 0 && LegacyGen.isAir(level, x - 1, y, z)) {
            growVines(level, x - 1, y, z, VineBlock.EAST);
        }
        if (rand.nextInt(3) == 0 && LegacyGen.isAir(level, x + 1, y, z)) {
            growVines(level, x + 1, y, z, VineBlock.WEST);
        }
        if (rand.nextInt(3) == 0 && LegacyGen.isAir(level, x, y, z - 1)) {
            growVines(level, x, y, z - 1, VineBlock.SOUTH);
        }
        if (rand.nextInt(3) == 0 && LegacyGen.isAir(level, x, y, z + 1)) {
            growVines(level, x, y, z + 1, VineBlock.NORTH);
        }
    }

    private static void growVines(LevelAccessor level, int x, int y, int z, BooleanProperty side) {
        BlockState vine = Blocks.VINE.defaultBlockState().setValue(side, true);
        LegacyGen.set(level, x, y, z, vine);
        for (int count = 4; LegacyGen.isAir(level, x, --y, z) && count > 0; count--) {
            LegacyGen.set(level, x, y, z, vine);
        }
    }
}
