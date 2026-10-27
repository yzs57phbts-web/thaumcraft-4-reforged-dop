package thaumcraft.test;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.aspect.*;
import thaumcraft.node.*;
import thaumcraft.registry.ModBlocks;
import thaumcraft.world.WorldGenConfig;

@GameTestHolder("thaumcraft") @PrefixGameTestTemplate(false)
public final class HungryNodeFeedingGameTests {
    private static NodeBlockEntity node(GameTestHelper h) {
        WorldGenConfig.hardNode = true;
        var pos = h.absolutePos(new BlockPos(5, 4, 5));
        NodeGenerator.createNodeAt(h.getLevel(), pos, NodeType.HUNGRY, null, new AspectList());
        return (NodeBlockEntity) h.getLevel().getBlockEntity(pos);
    }
    private static ItemEntity consume(GameTestHelper h, NodeBlockEntity node, ItemStack stack) throws Exception {
        var pos = node.getBlockPos();
        var item = new ItemEntity(h.getLevel(), pos.getX() + .6, pos.getY() + .5, pos.getZ() + .5, stack);
        h.getLevel().addFreshEntity(item);
        Method feed = NodeBlockEntity.class.getDeclaredMethod("handleHungryNodeFirst", Level.class, boolean.class);
        feed.setAccessible(true);
        for (int i = 0; i < 8 && item.isAlive(); i++) feed.invoke(node, h.getLevel(), false);
        h.assertTrue(!item.isAlive(), "Node did not consume the real ItemEntity");
        return item;
    }
    @GameTest(template="test/platform", timeoutTicks=40)
    public static void craftingTableFeedsFabricoWithoutPrimalReduction(GameTestHelper h) throws Exception {
        var node = node(h);
        consume(h, node, new ItemStack(Items.CRAFTING_TABLE));
        h.assertTrue(node.getAspectsBase().size() == 1 && node.getNodeVisBase(Aspect.CRAFT) == 1,
            "Crafting table must grow Fabrico directly by one");
        h.assertTrue(node.getAspects().size() == 0, "Capacity growth must not also refill current vis");
        h.succeed();
    }
    @GameTest(template="test/platform", timeoutTicks=40)
    public static void depletedCompoundRefillsOneAndWholeStackIsNotMultiplied(GameTestHelper h) throws Exception {
        var node = node(h);
        node.setAspects(new AspectList().add(Aspect.CRAFT, 8));
        node.takeFromContainer(Aspect.CRAFT, 5);
        consume(h, node, new ItemStack(Items.CRAFTING_TABLE, 64));
        h.assertTrue(node.getNodeVisBase(Aspect.CRAFT) == 8 && node.getAspects().getAmount(Aspect.CRAFT) == 4,
            "A consumed stack must restore only one point; capacity must stay unchanged");
        h.succeed();
    }
    @GameTest(template="test/platform", timeoutTicks=40)
    public static void livingFoodKeepsItsOriginalAspects(GameTestHelper h) throws Exception {
        var node = node(h); var pos = node.getBlockPos();
        var zombie = new Zombie(EntityType.ZOMBIE, h.getLevel());
        zombie.setPos(pos.getX()+.6, pos.getY()+.5, pos.getZ()+.5); zombie.setHealth(.5F);
        h.getLevel().addFreshEntity(zombie);
        var tags = AspectHelper.getEntityTags(zombie);
        h.assertTrue(tags != null && tags.size() > 0, "Missing living-food fixture tags");
        var method = NodeBlockEntity.class.getDeclaredMethod("handleHungryNodeFirst", Level.class, boolean.class);
        method.setAccessible(true); method.invoke(node, h.getLevel(), false);
        h.assertTrue(!zombie.isAlive() && node.getAspectsBase().size() == 1, "Living food no longer feeds node");
        var chosen = node.getAspectsBase().getAspects().getFirst();
        h.assertTrue(tags.getAmount(chosen) > 0 && node.getNodeVisBase(chosen) == 1, "Living food was reduced or overcounted");
        h.succeed();
    }
    @GameTest(template="test/platform", timeoutTicks=40)
    public static void capacityGrowthUsesOriginalRandomThreshold(GameTestHelper h) throws Exception {
        var node = node(h); var pos = node.getBlockPos();
        var method = NodeBlockEntity.class.getDeclaredMethod("feedOnKill", net.minecraft.server.level.ServerLevel.class, Entity.class, boolean.class);
        method.setAccessible(true); int grown = 0, unchanged = 0;
        for (int seed = 0; seed < 128; seed++) {
            node.setAspects(new AspectList().add(Aspect.CRAFT, 20));
            var reference = RandomSource.create(seed); reference.nextInt(1);
            boolean expected = reference.nextInt(41) < 4;
            var food = new ItemEntity(h.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CRAFTING_TABLE, 64));
            h.getLevel().getRandom().setSeed(seed);
            boolean changed = (boolean) method.invoke(node, h.getLevel(), food, false);
            h.assertTrue(changed == expected && node.getNodeVisBase(Aspect.CRAFT) == (expected ? 21 : 20),
                "Legacy random growth threshold or one-item valuation changed at seed " + seed + "; expected=" + expected + "; actual=" + changed + "; base=" + node.getNodeVisBase(Aspect.CRAFT));
            h.assertTrue(node.getAspects().getAmount(Aspect.CRAFT) == 20, "Growth changed current vis");
            if (expected) grown++; else unchanged++;
        }
        h.assertTrue(grown > 0 && unchanged > 0, "Random threshold did not exercise both outcomes");
        System.out.println("HUNGRY_FIXED random_trials=128 growth=" + grown + " unchanged=" + unchanged);
        h.succeed();
    }
    @GameTest(template="test/platform", timeoutTicks=40)
    public static void compoundCapacitySurvivesSaveAndLoad(GameTestHelper h) throws Exception {
        var node = node(h); consume(h, node, new ItemStack(Items.CRAFTING_TABLE));
        var saved = node.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored = new NodeBlockEntity(node.getBlockPos(), node.getBlockState());
        restored.setLevel(h.getLevel()); restored.loadWithComponents(saved, h.getLevel().registryAccess());
        h.assertTrue(restored.getNodeVisBase(Aspect.CRAFT) == 1 && restored.getAspectsBase().size() == 1,
            "Compound capacity lost on actual NBT roundtrip");
        h.succeed();
    }
    @GameTest(template="test/platform", timeoutTicks=40)
    public static void containedItemAspectsAreIncludedLikeLegacyBonusTags(GameTestHelper h) throws Exception {
        var node = node(h);
        var phial = new ItemStack(thaumcraft.registry.ModItems.ESSENTIA_PHIAL.get());
        AspectKeys.ASPECTS.set(phial, new AspectList().add(Aspect.CRAFT, 8));
        var tags = AspectHelper.getBonusTags(phial, AspectHelper.getObjectTags(phial));
        h.assertTrue(tags.getAmount(Aspect.CRAFT) >= 8, "Contained aspect fixture missing");
        // Observe the contained compound among the phial's ordinary aspects using
        // deterministic seeds, without changing the production scanner.
        var method = NodeBlockEntity.class.getDeclaredMethod("feedOnKill", net.minecraft.server.level.ServerLevel.class, Entity.class, boolean.class);
        method.setAccessible(true);
        boolean found = false;
        for (int seed = 0; seed < 128 && !found; seed++) {
            node.setAspects(new AspectList());
            var item = new ItemEntity(h.getLevel(), 0, 0, 0, phial.copy());
            h.getLevel().getRandom().setSeed(seed);
            method.invoke(node, h.getLevel(), item, false);
            found = node.getNodeVisBase(Aspect.CRAFT) == 1;
        }
        h.assertTrue(found, "Contained compound was omitted from food valuation");
        h.succeed();
    }
    @GameTest(template="test/platform", timeoutTicks=40)
    public static void energizedConversionStillReducesCompoundToPrimalCV(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(5, 4, 5));
        h.getLevel().setBlockAndUpdate(pos, ModBlocks.ENERGIZED_AURA_NODE.get().defaultBlockState());
        var energized = (EnergizedNodeBlockEntity) h.getLevel().getBlockEntity(pos);
        var compounds = new AspectList().add(Aspect.CRAFT, 64);
        energized.setNodeType(NodeType.NORMAL); energized.setAspects(compounds.copy()); energized.setupNode();
        var expected = AspectHelper.reduceToPrimals(compounds, true);
        h.assertTrue(energized.getAuraBase().getAmount(Aspect.CRAFT) == 64, "Conversion lost original compound capacity");
        h.assertTrue(energized.getAspects().getAmount(Aspect.CRAFT) == 0, "Compound remained in CV output");
        for (var primal : expected.getAspects()) {
            h.assertTrue(primal.getComponents() == null, "Expected decomposition was not primal");
            h.assertTrue(energized.getAspects().getAmount(primal) == (int) Math.sqrt(expected.getAmount(primal)),
                "Converted CV output changed for " + primal);
        }
        h.succeed();
    }
}
