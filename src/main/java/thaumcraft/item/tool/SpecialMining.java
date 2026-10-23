package thaumcraft.item.tool;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;

public final class SpecialMining {
    private record Rule(Predicate<BlockState> ore, Supplier<Item> cluster, float chance) {
    }

    private static final List<Rule> RULES = List.of(
        new Rule(state -> state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE), ModItems.NATIVE_IRON_CLUSTER, 1.0F),
        new Rule(state -> state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE), ModItems.NATIVE_GOLD_CLUSTER, 0.9F),
        new Rule(state -> state.is(ModBlocks.CINNABAR_ORE.get()) || state.is(ModBlocks.DEEPSLATE_CINNABAR_ORE.get()), ModItems.NATIVE_CINNABAR_CLUSTER, 0.9F),
        new Rule(state -> state.is(oreTag("copper")), ModItems.NATIVE_COPPER_CLUSTER, 1.0F),
        new Rule(state -> state.is(oreTag("tin")), ModItems.NATIVE_TIN_CLUSTER, 1.0F),
        new Rule(state -> state.is(oreTag("silver")), ModItems.NATIVE_SILVER_CLUSTER, 1.0F),
        new Rule(state -> state.is(oreTag("lead")), ModItems.NATIVE_LEAD_CLUSTER, 1.0F)
    );

    private SpecialMining() {
    }

    private static TagKey<Block> oreTag(String metal) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/" + metal));
    }

    private static @Nullable Rule rule(BlockState state) {
        for (Rule rule : RULES) {
            if (rule.ore().test(state)) {
                return rule;
            }
        }
        return null;
    }

    public static ItemStack find(BlockState state, ItemStack drop, float chance, RandomSource random) {
        Rule match = rule(state);
        if (match == null) {
            return drop;
        }
        Item cluster = match.cluster().get();
        if (drop.is(cluster) || random.nextFloat() > chance * match.chance()) {
            return drop;
        }
        return new ItemStack(cluster, drop.getCount());
    }

    public static boolean replaceDrops(BlockState state, List<ItemEntity> drops, float chance, RandomSource random) {
        boolean replaced = false;
        for (ItemEntity drop : drops) {
            ItemStack stack = drop.getItem();
            ItemStack result = find(state, stack, chance, random);
            if (result != stack) {
                drop.setItem(result);
                replaced = true;
            }
        }
        return replaced;
    }
}
