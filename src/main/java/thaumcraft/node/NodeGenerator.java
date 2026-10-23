package thaumcraft.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModBlocks;
import thaumcraft.world.WorldGenConfig;
import thaumcraft.world.gen.BiomeHandler;

public final class NodeGenerator {
    private static final List<Aspect> BASIC_ASPECTS = new ArrayList<>();
    private static final List<Aspect> COMPLEX_ASPECTS = new ArrayList<>();

    private NodeGenerator() {
    }

    private static void init() {
        if (!BASIC_ASPECTS.isEmpty()) {
            return;
        }
        for (Aspect aspect : Aspect.getEnabledAspects()) {
            if (aspect.getComponents() != null) {
                COMPLEX_ASPECTS.add(aspect);
            } else {
                BASIC_ASPECTS.add(aspect);
            }
        }
    }

    public static void createRandomNodeAt(LevelAccessor level, BlockPos pos, RandomSource random, boolean silverwood, boolean eerie, boolean small) {
        createRandomNodeAt(level, pos, new Random(random.nextLong()), silverwood, eerie, small);
    }

    public static void createRandomNodeAt(LevelAccessor level, BlockPos pos, Random random, boolean silverwood, boolean eerie, boolean small) {
        init();
        NodeType type = NodeType.NORMAL;
        if (silverwood) {
            type = NodeType.PURE;
        } else if (eerie) {
            type = NodeType.DARK;
        } else if (random.nextInt(WorldGenConfig.specialNodeRarity) == 0) {
            switch (random.nextInt(10)) {
                case 0, 1, 2 -> type = NodeType.DARK;
                case 3, 4, 5 -> type = NodeType.UNSTABLE;
                case 6, 7, 8 -> type = NodeType.PURE;
                default -> type = NodeType.HUNGRY;
            }
        }
        NodeModifier modifier = null;
        if (random.nextInt(WorldGenConfig.specialNodeRarity / 2) == 0) {
            modifier = switch (random.nextInt(3)) {
                case 0 -> NodeModifier.BRIGHT;
                case 1 -> NodeModifier.PALE;
                default -> NodeModifier.FADING;
            };
        }
        Holder<Biome> biome = level.getBiome(pos);
        int biomeAura = BiomeHandler.getBiomeAura(biome);
        if (type != NodeType.PURE && biome.is(Thaumcraft.id("tainted_land"))) {
            biomeAura = (int) (biomeAura * 1.5F);
            if (random.nextBoolean()) {
                type = NodeType.TAINTED;
                biomeAura = (int) (biomeAura * 1.5F);
            }
        }
        if (silverwood || small) {
            biomeAura /= 4;
        }
        int value = random.nextInt(biomeAura / 2) + biomeAura / 2;
        Aspect biomeAspect = BiomeHandler.getRandomBiomeTag(biome, random);
        AspectList aspects = new AspectList();
        if (biomeAspect != null) {
            aspects.add(biomeAspect, 2);
        } else {
            aspects.add(COMPLEX_ASPECTS.get(random.nextInt(COMPLEX_ASPECTS.size())), 1);
            aspects.add(BASIC_ASPECTS.get(random.nextInt(BASIC_ASPECTS.size())), 1);
        }
        for (int index = 0; index < 3; index++) {
            if (random.nextBoolean()) {
                if (random.nextInt(WorldGenConfig.specialNodeRarity) == 0) {
                    aspects.merge(COMPLEX_ASPECTS.get(random.nextInt(COMPLEX_ASPECTS.size())), 1);
                } else {
                    aspects.merge(BASIC_ASPECTS.get(random.nextInt(BASIC_ASPECTS.size())), 1);
                }
            }
        }
        if (type == NodeType.HUNGRY) {
            aspects.merge(Aspect.HUNGER, 2);
            if (random.nextBoolean()) {
                aspects.merge(Aspect.GREED, 1);
            }
        } else if (type == NodeType.PURE) {
            if (random.nextBoolean()) {
                aspects.merge(Aspect.LIFE, 2);
            } else {
                aspects.merge(Aspect.ORDER, 2);
            }
        } else if (type == NodeType.DARK) {
            if (random.nextBoolean()) {
                aspects.merge(Aspect.DEATH, 1);
            }
            if (random.nextBoolean()) {
                aspects.merge(Aspect.UNDEAD, 1);
            }
            if (random.nextBoolean()) {
                aspects.merge(Aspect.ENTROPY, 1);
            }
            if (random.nextBoolean()) {
                aspects.merge(Aspect.DARKNESS, 1);
            }
        }
        int water = 0;
        int lava = 0;
        int stone = 0;
        int foliage = 0;
        try {
            for (int offsetX = -5; offsetX <= 5; offsetX++) {
                for (int offsetY = -5; offsetY <= 5; offsetY++) {
                    for (int offsetZ = -5; offsetZ <= 5; offsetZ++) {
                        BlockState state = level.getBlockState(pos.offset(offsetX, offsetY, offsetZ));
                        if (state.getFluidState().is(FluidTags.WATER)) {
                            water++;
                        } else if (state.getFluidState().is(FluidTags.LAVA)) {
                            lava++;
                        } else if (state.is(Blocks.STONE)) {
                            stone++;
                        }
                        if (state.is(BlockTags.LEAVES) || state.is(Blocks.VINE)) {
                            foliage++;
                        }
                    }
                }
            }
        } catch (RuntimeException exception) {
        }
        if (water > 100) {
            aspects.merge(Aspect.WATER, 1);
        }
        if (lava > 100) {
            aspects.merge(Aspect.FIRE, 1);
            aspects.merge(Aspect.EARTH, 1);
        }
        if (stone > 500) {
            aspects.merge(Aspect.EARTH, 1);
        }
        if (foliage > 100) {
            aspects.merge(Aspect.PLANT, 1);
        }
        List<Aspect> sorted = aspects.getAspectsSorted();
        int[] spread = new int[sorted.size()];
        float total = 0.0F;
        for (int index = 0; index < spread.length; index++) {
            if (aspects.getAmount(sorted.get(index)) == 2) {
                spread[index] = 50 + random.nextInt(25);
            } else {
                spread[index] = 25 + random.nextInt(50);
            }
            total += spread[index];
        }
        for (int index = 0; index < spread.length; index++) {
            aspects.merge(sorted.get(index), (int) (spread[index] / total * value));
        }
        createNodeAt(level, pos, type, modifier, aspects);
    }

    public static void createNodeAt(LevelAccessor level, BlockPos pos, NodeType type, @Nullable NodeModifier modifier, AspectList aspects) {
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, ModBlocks.AURA_NODE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof NodeBlockEntity node) {
            node.setNodeType(type);
            node.setNodeModifier(modifier);
            node.setAspects(aspects);
            node.setChanged();
        }
        if (level instanceof Level live) {
            live.sendBlockUpdated(pos, live.getBlockState(pos), live.getBlockState(pos), Block.UPDATE_CLIENTS);
        }
    }
}
