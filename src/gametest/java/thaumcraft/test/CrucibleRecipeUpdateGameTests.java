package thaumcraft.test;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.alchemy.CrucibleBlock;
import thaumcraft.alchemy.CrucibleBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.CrucibleCrafting;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchPage;

@GameTestHolder("thaumcraft_recipe_tests")
@PrefixGameTestTemplate(false)
public final class CrucibleRecipeUpdateGameTests {
    private record Expected(String key, String research, Item input, Item output, int count, Map<Aspect, Integer> cost) {
        AspectList aspects() {
            var result = new AspectList();
            cost.forEach(result::add);
            return result;
        }
    }

    private static final List<Expected> RECIPES = List.of(
        new Expected("AltHoneyBottle", "ALCHEMICALDUPLICATION", Items.HONEY_BOTTLE, Items.HONEY_BOTTLE, 2, Map.of(Aspect.LIFE, 2, Aspect.WATER, 2)),
        new Expected("AltHoneycomb", "ALCHEMICALDUPLICATION", Items.HONEYCOMB, Items.HONEYCOMB, 2, Map.of(Aspect.LIFE, 2, Aspect.WATER, 2)),
        new Expected("AltGlowInk", "ALCHEMICALMANUFACTURE", Items.INK_SAC, Items.GLOW_INK_SAC, 1, Map.of(Aspect.SENSES, 2, Aspect.LIGHT, 2)),
        new Expected("AltPoisonousPotato", "ALCHEMICALMANUFACTURE", Items.POTATO, Items.POISONOUS_POTATO, 1, Map.of(Aspect.POISON, 2)),
        new Expected("AltString", "ENTROPICPROCESSING", Items.WHITE_WOOL, Items.STRING, 4, Map.of(Aspect.ENTROPY, 4)),
        new Expected("AltQuartz", "ENTROPICPROCESSING", Items.QUARTZ_BLOCK, Items.QUARTZ, 4, Map.of(Aspect.ENTROPY, 2))
    );

    private static void craft(GameTestHelper helper, Expected expected, Item inputItem) {
        var recipe = (CrucibleRecipe) TcRecipes.get(expected.key());
        helper.assertTrue(recipe != null && recipe.research().equals(expected.research()), "recipe or research registration wrong: " + expected.key());
        helper.assertTrue(recipe.result().create().is(expected.output()) && recipe.result().count() == expected.count(), "output definition wrong");
        for (Aspect aspect : Aspect.values()) {
            helper.assertValueEqual(recipe.aspects().getAmount(aspect), expected.cost().getOrDefault(aspect, 0), "wrong total aspect cost for " + expected.key() + "/" + aspect.getTag());
        }
        helper.assertTrue(recipe.catalyst().displayStacks().stream().allMatch(stack -> stack.getCount() == 1), "displayed catalyst amount is not one");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        Knowledge.get(player).addResearch(expected.research());
        ItemStack inputStack = new ItemStack(inputItem);
        helper.assertTrue(CrucibleCrafting.find(player, expected.aspects(), inputStack) == recipe, "crucible selects another recipe at the exact specified cost");
        for (Aspect aspect : expected.cost().keySet()) {
            AspectList shortfall = expected.aspects();
            shortfall.remove(aspect, 1);
            helper.assertTrue(!CrucibleCrafting.matches(recipe, shortfall, inputStack), "recipe accepts an underpaid aspect pool");
        }
        BlockPos local = new BlockPos(2, 2, 2);
        helper.setBlock(local, ModBlocks.CRUCIBLE.get());
        var crucible = (CrucibleBlockEntity) helper.getBlockEntity(local);
        for (Aspect aspect : List.copyOf(crucible.getAspects().getAspects())) {
            crucible.getAspects().remove(aspect, crucible.getAspects().getAmount(aspect));
        }
        expected.cost().forEach((aspect, amount) -> crucible.getAspects().add(aspect, amount));
        crucible.getAspects().add(Aspect.ORDER, 7);
        crucible.fillWater(1000);
        crucible.setHeat(180);
        BlockPos pos = helper.absolutePos(local);
        var entity = new ItemEntity(helper.getLevel(), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, inputStack);
        entity.setThrower(player);
        helper.getLevel().addFreshEntity(entity);
        crucible.getBlockState().entityInside(helper.getLevel(), pos, entity);
        helper.assertTrue(entity.isRemoved(), "one catalyst was not consumed by the real crucible block");
        helper.assertValueEqual(crucible.getWaterAmount(), 950, "expected exactly one crucible execution");
        helper.assertValueEqual(crucible.getAspects().getAmount(Aspect.ORDER), 7, "unrelated aspects were spent");
        helper.assertValueEqual(crucible.getAspects().visSize(), 7, "recipe cost was not spent exactly once");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2), item -> item.isAlive());
        helper.assertValueEqual(drops.size(), 1, "unexpected output or leftover catalyst/container");
        helper.assertTrue(drops.getFirst().getTags().contains(CrucibleBlock.SPECIAL_ITEM_TAG) && drops.getFirst().getItem().is(expected.output()), "wrong crucible output item");
        helper.assertValueEqual(drops.getFirst().getItem().getCount(), expected.count(), "wrong output count");
        drops.forEach(ItemEntity::discard);
    }

    @GameTest(template = "test/platform", batch = "crucible25")
    public static void honeyBottleExactCostAndOutput(GameTestHelper helper) { craft(helper, RECIPES.get(0), Items.HONEY_BOTTLE); helper.succeed(); }

    @GameTest(template = "test/platform", batch = "crucible25")
    public static void honeycombExactCostAndOutput(GameTestHelper helper) { craft(helper, RECIPES.get(1), Items.HONEYCOMB); helper.succeed(); }

    @GameTest(template = "test/platform", batch = "crucible25")
    public static void glowingInkExactCostAndOutput(GameTestHelper helper) { craft(helper, RECIPES.get(2), Items.INK_SAC); helper.succeed(); }

    @GameTest(template = "test/platform", batch = "crucible25")
    public static void poisonousPotatoExactCostAndOutput(GameTestHelper helper) { craft(helper, RECIPES.get(3), Items.POTATO); helper.succeed(); }

    @GameTest(template = "test/platform", batch = "crucible25")
    public static void woolExactCostAndOutput(GameTestHelper helper) { craft(helper, RECIPES.get(4), Items.WHITE_WOOL); helper.succeed(); }

    @GameTest(template = "test/platform", batch = "crucible25")
    public static void quartzExactCostAndOutput(GameTestHelper helper) { craft(helper, RECIPES.get(5), Items.QUARTZ_BLOCK); helper.succeed(); }

    @GameTest(template = "test/platform", batch = "crucible25_variants")
    public static void allWoolColorsWorkAndUnspecifiedVariantsDoNot(GameTestHelper helper) {
        for (DyeColor color : DyeColor.values()) {
            Item wool = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(color.getName() + "_wool"));
            craft(helper, RECIPES.get(4), wool);
        }
        var wool = (CrucibleRecipe) TcRecipes.get("AltString");
        helper.assertTrue(!wool.catalyst().test(new ItemStack(Items.WHITE_CARPET)), "carpet treated as a wool block");
        var quartz = (CrucibleRecipe) TcRecipes.get("AltQuartz");
        for (Item other : List.of(Items.QUARTZ_PILLAR, Items.CHISELED_QUARTZ_BLOCK, Items.SMOOTH_QUARTZ)) {
            helper.assertTrue(!quartz.catalyst().test(new ItemStack(other)), "unspecified quartz variant accepted");
        }
        helper.assertTrue(!((CrucibleRecipe) TcRecipes.get("AltHoneycomb")).catalyst().test(new ItemStack(Items.HONEYCOMB_BLOCK)), "honeycomb block accepted instead of one honeycomb item");
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "crucible25_pages")
    public static void existingResearchHasExactlyTheRequestedRecipePages(GameTestHelper helper) {
        var pages = Map.of(
            "ALCHEMICALDUPLICATION", List.of("AltGunpowder", "AltSlime", "AltClay", "AltGlowstone", "AltInk", "AltHoneyBottle", "AltHoneycomb"),
            "ALCHEMICALMANUFACTURE", List.of("AltWeb", "AltIce", "AltGlowInk", "AltPoisonousPotato"),
            "ENTROPICPROCESSING", List.of("AltBonemeal", "AltString", "AltQuartz")
        );
        pages.forEach((key, expected) -> {
            var research = ResearchCategories.getResearch(key);
            helper.assertTrue(research != null && research.pages().size() == expected.size() + 1, "unexpected research page count");
            helper.assertTrue(research.pages().getFirst() instanceof ResearchPage.Text text && text.text().equals("tc.research_page." + key + ".1"), "research description page was changed");
            var actual = research.pages().stream().filter(ResearchPage.Crucible.class::isInstance).map(ResearchPage.Crucible.class::cast).flatMap(page -> page.recipes().stream()).toList();
            helper.assertTrue(actual.equals(expected), "wrong recipe pages in " + key + ": " + actual);
            helper.assertTrue(research.isConcealed() && research.isSecondary() && research.complexity() == 1, "research flags/complexity changed");
        });
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "crucible25_removed")
    public static void removedRecipesAreAbsentButTheirItemsRemain(GameTestHelper helper) {
        for (String key : List.of("AltCrackedBrick", "AltMossyCobble")) {
            helper.assertTrue(TcRecipes.get(key) == null, "removed recipe remains registered: " + key);
            for (var research : ResearchCategories.allResearch()) {
                helper.assertTrue(research.pages().stream().noneMatch(page -> page instanceof ResearchPage.Crucible crucible && crucible.recipes().contains(key)), "removed recipe still shown in research");
            }
        }
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        Knowledge.get(player).addResearch("ALCHEMICALMANUFACTURE");
        Knowledge.get(player).addResearch("ENTROPICPROCESSING");
        helper.assertTrue(CrucibleCrafting.find(player, new AspectList().add(Aspect.PLANT, 2).add(Aspect.MAGIC, 1), new ItemStack(Items.COBBLESTONE)) == null, "mossy cobblestone conversion still works");
        helper.assertTrue(CrucibleCrafting.find(player, new AspectList().add(Aspect.ENTROPY, 2), new ItemStack(Items.STONE_BRICKS)) == null, "cracked brick conversion still works");
        helper.assertTrue(!new ItemStack(Items.MOSSY_COBBLESTONE).isEmpty() && !new ItemStack(Items.CRACKED_STONE_BRICKS).isEmpty(), "result items were removed");
        helper.succeed();
    }
}
