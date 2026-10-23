package thaumcraft.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.AlembicBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.eldritch.EldritchBlock;
import thaumcraft.eldritch.EldritchBlockEntity;
import thaumcraft.eldritch.EldritchPartBlockEntity;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemKeys;
import thaumcraft.entity.golem.GolemSnapshot;
import thaumcraft.entity.golem.GolemType;
import thaumcraft.entity.golem.LogisticsGoals;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.platform.GameplayConfig;
import thaumcraft.registry.EldritchContent;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchConfig;
import thaumcraft.research.ResearchManager;
import thaumcraft.world.WorldGenConfig;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class AuditFixGameTests {
    @GameTest(template = "test/platform", batch = "audit23_bell")
    public static void bellSelectsMarksAndClearsInBothModesAndHands(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = TestSupport.mockServerPlayer(helper);
        var golem = new GolemBase(ModEntities.GOLEM.get(), level);
        BlockPos home = helper.absolutePos(new BlockPos(4, 2, 4));
        golem.configure(GolemType.IRON, new GolemSnapshot(6, false, List.of(), "", List.of(), ItemContainerContents.EMPTY, List.of(), 0));
        golem.setHome(home, Direction.UP);
        golem.setPos(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        level.addFreshEntity(golem);
        player.setPos(golem.position());
        BlockPos jar = home.south();
        level.setBlockAndUpdate(jar, ModBlocks.WARDED_JAR.get().defaultBlockState());
        for (GameType mode : List.of(GameType.CREATIVE, GameType.SURVIVAL)) {
            player.setGameMode(mode);
            for (InteractionHand hand : InteractionHand.values()) {
                golem.setMarkers(List.of());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                ItemStack bell = new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id("golem_bell")));
                player.setItemInHand(hand, bell);
                helper.assertTrue(player.interactOn(golem, hand).consumesAction(), "bell interaction failed");
                var selection = GolemKeys.BELL.get(player.getItemInHand(hand));
                helper.assertTrue(selection != null && selection.entity().equals(golem.getUUID()), "selection lost in " + mode + "/" + hand);
                player.gameMode.useItemOn(player, level, player.getItemInHand(hand), hand, new BlockHitResult(Vec3.atCenterOf(jar), Direction.UP, jar, false));
                helper.assertValueEqual(golem.getMarkers().size(), 1, "marked jar missing");
                helper.assertValueEqual(GolemKeys.BELL.get(player.getItemInHand(hand)).markers().size(), 1, "bell marker missing");
                player.setShiftKeyDown(true);
                player.interactOn(golem, hand);
                helper.assertTrue(GolemKeys.BELL.get(player.getItemInHand(hand)) == null, "selection was not cleared");
                helper.assertValueEqual(golem.getMarkers().size(), 1, "deselecting changes golem configuration");
                helper.assertValueEqual(player.getItemInHand(hand).getCount(), 1, "bell consumed");
                player.setShiftKeyDown(false);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "audit23_essentia")
    public static void alchemyGolemStillMovesEssentiaAfterCreativeBinding(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos furnace = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlockAndUpdate(furnace, ModBlocks.ALCHEMICAL_FURNACE.get().defaultBlockState());
        level.setBlockAndUpdate(furnace.above(), ModBlocks.ALEMBIC.get().defaultBlockState());
        var alembic = (AlembicBlockEntity) level.getBlockEntity(furnace.above());
        alembic.addToContainer(Aspect.FIRE, 8);
        var golem = new GolemBase(ModEntities.GOLEM.get(), level);
        BlockPos home = furnace.east();
        golem.configure(GolemType.IRON, new GolemSnapshot(6, false, List.of(), "", List.of(), ItemContainerContents.EMPTY, List.of(), 0));
        golem.setHome(home, Direction.EAST);
        golem.setPos(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        level.addFreshEntity(golem);
        var player = TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.CREATIVE);
        player.setPos(golem.position());
        ItemStack bell = new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id("golem_bell")));
        player.setItemInHand(InteractionHand.MAIN_HAND, bell);
        player.interactOn(golem, InteractionHand.MAIN_HAND);
        BlockPos jarPos = home.south();
        level.setBlockAndUpdate(jarPos, ModBlocks.WARDED_JAR.get().defaultBlockState());
        player.gameMode.useItemOn(player, level, bell, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(jarPos), Direction.UP, jarPos, false));
        var gather = new GoalSelector(level::getProfiler);
        LogisticsGoals.install(golem, gather);
        gather.tick();
        helper.assertTrue(golem.getEssentiaAmount() > 0, "gathering failed");
        var deposit = new GoalSelector(level::getProfiler);
        LogisticsGoals.install(golem, deposit);
        deposit.tick();
        var jar = (JarBlockEntity) level.getBlockEntity(jarPos);
        helper.assertTrue(jar.getAmount() > 0, "delivery failed");
        helper.assertValueEqual(alembic.getAmount() + jar.getAmount() + golem.getEssentiaAmount(), 8, "essentia lost or duplicated");
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "audit23_obelisk")
    public static void obeliskPartsPersistWithoutTickingOrDuplicateRenderers(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(4, 2, 4));
        var lower = EldritchContent.ELDRITCH_OBELISK.get().defaultBlockState();
        var upper = lower.setValue(EldritchBlock.PART, DoubleBlockHalf.UPPER);
        level.setBlockAndUpdate(base, lower);
        level.setBlockAndUpdate(base.above(), upper);
        helper.assertTrue(level.getBlockEntity(base) instanceof EldritchBlockEntity, "lower logic missing");
        BlockEntity part = level.getBlockEntity(base.above());
        helper.assertTrue(part instanceof EldritchPartBlockEntity, "upper contract missing");
        helper.assertTrue(upper.getTicker(level, part.getType()) == null, "upper part is ticking");
        var saved = part.saveWithFullMetadata(level.registryAccess());
        helper.assertTrue(BlockEntity.loadStatic(base.above(), upper, saved, level.registryAccess()) instanceof EldritchPartBlockEntity, "part save/reload failed");
        for (var block : List.of(EldritchContent.GLOWING_CRUSTED_STONE, EldritchContent.GLYPHED_STONE, EldritchContent.ANCIENT_DOORWAY)) {
            helper.assertTrue(!block.get().defaultBlockState().hasBlockEntity(), "ordinary stone requests a block entity");
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "audit23_config")
    public static void serverConfigControlsResearchAndWorldgenAndRestoresDefaults(GameTestHelper helper) {
        ModConfigSpec.IntValue difficulty = GameplayConfig.SPEC.getValues().get("research.difficulty");
        ModConfigSpec.IntValue rarity = GameplayConfig.SPEC.getValues().get("worldgen.nodeRarity");
        int oldDifficulty = difficulty.get();
        int oldRarity = rarity.get();
        var primary = ResearchCategories.allResearch().stream().filter(item -> !item.isSecondary() && item.tags().size() > 0).findFirst().orElseThrow();
        try {
            difficulty.set(-1);
            rarity.set(73);
            GameplayConfig.apply();
            helper.assertTrue(ResearchManager.isBuyable(primary), "server difficulty not applied");
            helper.assertValueEqual(WorldGenConfig.nodeRarity, 73, "worldgen configuration not applied");
            GameplayConfig.reset();
            helper.assertValueEqual(ResearchConfig.RESEARCH_DIFFICULTY, 0, "default research difficulty changed");
            helper.assertValueEqual(WorldGenConfig.nodeRarity, 36, "default node rarity changed");
        } finally {
            difficulty.set(oldDifficulty);
            rarity.set(oldRarity);
            GameplayConfig.apply();
        }
        helper.succeed();
    }
}
