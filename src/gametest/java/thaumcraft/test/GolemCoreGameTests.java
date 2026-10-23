package thaumcraft.test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemBobber;
import thaumcraft.entity.golem.GolemSnapshot;
import thaumcraft.entity.golem.GolemType;
import thaumcraft.entity.golem.LogisticsGoals;
import thaumcraft.entity.golem.goal.work.FishingGoal;
import thaumcraft.registry.ModEntities;

@net.neoforged.neoforge.gametest.GameTestHolder(Thaumcraft.MODID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class GolemCoreGameTests {
    private GolemCoreGameTests() { }

    private static GolemBase setup(GameTestHelper helper, int core) {
        var level = helper.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(1, 1, 1, 23, 1, 23)) level.setBlockAndUpdate(helper.absolutePos(pos), Blocks.STONE.defaultBlockState());
        GolemBase golem = new GolemBase(ModEntities.GOLEM.get(), level);
        BlockPos home = helper.absolutePos(new BlockPos(10, 2, 10));
        golem.setPos(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        golem.setHome(home, Direction.WEST);
        golem.configure(GolemType.IRON, new GolemSnapshot(core, false, List.of(), "", List.of(), ItemContainerContents.EMPTY, List.of(), 0));
        golem.setHealth(golem.getMaxHealth());
        golem.getRandom().setSeed(1729);
        return golem;
    }

    private static ItemEntity item(GameTestHelper helper, BlockPos relative, int count) {
        BlockPos pos = helper.absolutePos(relative);
        ItemEntity entity = new ItemEntity(helper.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(Items.WHEAT, count));
        entity.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void pickupUnreachable(GameTestHelper helper) {
        GolemBase golem = setup(helper, 2);
        BlockPos sealed = new BlockPos(12, 2, 10);
        for (BlockPos pos : BlockPos.betweenClosed(sealed.offset(-1, 0, -1), sealed.offset(1, 3, 1))) {
            if (!pos.equals(sealed) && !pos.equals(sealed.above())) helper.getLevel().setBlockAndUpdate(helper.absolutePos(pos), Blocks.BEDROCK.defaultBlockState());
        }
        ItemEntity blocked = item(helper, sealed, 1);
        ItemEntity reachable = item(helper, new BlockPos(10, 2, 15), 5);
        helper.getLevel().addFreshEntity(golem);
        helper.succeedWhen(() -> {
            helper.assertTrue(reachable.isRemoved() && golem.getCarried().getCount() == 5, "enclosed item prevents collecting a reachable item");
            helper.assertTrue(blocked.isAlive() && blocked.getItem().getCount() == 1, "item collected through wall");
        });
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void pickupThroughFloor(GameTestHelper helper) {
        GolemBase golem = setup(helper, 2);
        golem.setNoAi(true);
        ItemEntity blocked = item(helper, new BlockPos(10, 1, 10), 1);
        blocked.setNoGravity(true);
        GoalSelector goals = new GoalSelector(() -> helper.getLevel().getProfiler());
        LogisticsGoals.install(golem, goals);
        for (int tick = 0; tick < 220; tick++) {
            golem.tickCount = tick;
            goals.tick();
        }
        helper.assertTrue(blocked.isAlive() && golem.getCarried().isEmpty(), "golem targets or collects an item through the floor");
        helper.assertTrue(goals.getAvailableGoals().stream().noneMatch(goal -> goal.isRunning()), "unreachable item keeps pickup goal active");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void pickupThroughWall(GameTestHelper helper) {
        GolemBase golem = setup(helper, 2);
        golem.setNoAi(true);
        BlockPos wall = helper.absolutePos(new BlockPos(11, 2, 10));
        helper.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(wall.above(), Blocks.STONE.defaultBlockState());
        ItemEntity blocked = item(helper, new BlockPos(11, 2, 10), 1);
        GoalSelector goals = new GoalSelector(() -> helper.getLevel().getProfiler());
        LogisticsGoals.install(golem, goals);
        for (int tick = 0; tick < 220; tick++) {
            golem.tickCount = tick;
            goals.tick();
        }
        helper.assertTrue(blocked.isAlive() && golem.getCarried().isEmpty(), "golem collected an item through adjacent solid wall");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void pickupMoved(GameTestHelper helper) {
        GolemBase golem = setup(helper, 2);
        ItemEntity moving = item(helper, new BlockPos(14, 2, 10), 5);
        helper.getLevel().addFreshEntity(golem);
        helper.runAfterDelay(5, () -> {
            BlockPos destination = helper.absolutePos(new BlockPos(10, 2, 16));
            moving.setPos(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);
            moving.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        });
        helper.succeedWhen(() -> helper.assertTrue(moving.isRemoved() && golem.getCarried().getCount() == 5, "golem lost a moving pickup target"));
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void chestOpen(GameTestHelper helper) { chestOpen(helper, false); }
    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void doubleChestOpen(GameTestHelper helper) { chestOpen(helper, true); }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void chestShared(GameTestHelper helper) {
        GolemBase first = setup(helper, 2);
        GolemBase second = setup(helper, 2);
        var level = helper.getLevel();
        BlockPos pos = first.getRestrictCenter().east();
        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        level.addFreshEntity(first);
        level.addFreshEntity(second);
        first.containers().open(pos);
        second.containers().open(pos);
        helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, pos), 2, "two golems share one opener count");
        first.containers().close();
        helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, pos), 1, "one golem closes chest for the other");
        var player = TestSupport.mockServerPlayer(helper);
        var chest = (ChestBlockEntity) level.getBlockEntity(pos);
        chest.startOpen(player);
        second.containers().close();
        helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, pos), 1, "golem closes chest for player");
        chest.stopOpen(player);
        helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, pos), 0, "shared chest leaks opener count");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void chestFailedDeposit(GameTestHelper helper) {
        GolemBase golem = setup(helper, 2);
        var level = helper.getLevel();
        BlockPos pos = golem.getRestrictCenter().east();
        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        var chest = (ChestBlockEntity) level.getBlockEntity(pos);
        for (int slot = 0; slot < chest.getContainerSize(); slot++) chest.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        golem.setCarried(new ItemStack(Items.WHEAT, 7));
        GoalSelector goals = new GoalSelector(() -> helper.getLevel().getProfiler());
        LogisticsGoals.install(golem, goals);
        goals.tick();
        helper.assertValueEqual(golem.getCarried().getCount(), 7, "full chest loses carried items");
        helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, pos), 0, "failed deposit opens chest");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void chestRemovedGolem(GameTestHelper helper) {
        GolemBase golem = setup(helper, 2);
        var level = helper.getLevel();
        BlockPos pos = golem.getRestrictCenter().east();
        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        level.addFreshEntity(golem);
        golem.containers().open(pos);
        golem.discard();
        helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, pos), 0, "removed golem leaves chest open");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 100, batch = "golem_core")
    public static void chestRecheck(GameTestHelper helper) {
        GolemBase golem = setup(helper, 2);
        var level = helper.getLevel();
        BlockPos pos = golem.getRestrictCenter().east();
        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        level.addFreshEntity(golem);
        golem.containers().open(pos);
        helper.runAfterDelay(3, () -> golem.containers().open(pos));
        helper.runAfterDelay(6, () -> helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, pos), 1, "vanilla recheck lost golem opener"));
        helper.runAfterDelay(12, () -> {
            helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, pos), 0, "recheck caused negative or leaked opener count");
            helper.succeed();
        });
    }

    private static void chestOpen(GameTestHelper helper, boolean doubled) {
        GolemBase golem = setup(helper, 2);
        BlockPos chest = golem.getRestrictCenter().east();
        var level = helper.getLevel();
        var state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
        level.setBlockAndUpdate(chest, state);
        if (doubled) {
            level.setBlockAndUpdate(chest.east(), state.setValue(ChestBlock.TYPE, ChestType.RIGHT));
            level.setBlockAndUpdate(chest, state.setValue(ChestBlock.TYPE, ChestType.LEFT));
        }
        golem.setCarried(new ItemStack(Items.WHEAT, 7));
        level.addFreshEntity(golem);
        GoalSelector goals = new GoalSelector(() -> helper.getLevel().getProfiler());
        LogisticsGoals.install(golem, goals);
        golem.tickCount = 0;
        goals.tick();
        helper.assertTrue(golem.getCarried().isEmpty(), "gatherer failed to deposit");
        helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, chest), 1, "deposit did not open chest");
        if (doubled) helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, chest.east()), 1, "second chest half stayed closed");
        helper.runAfterDelay(12, () -> {
            helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, chest), 0, "chest stayed open after deposit");
            if (doubled) helper.assertValueEqual(ChestBlockEntity.getOpenCount(level, chest.east()), 0, "second chest half stayed open");
            helper.succeed();
        });
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void fishingCycles(GameTestHelper helper) {
        GolemBase golem = setup(helper, 11);
        golem.setNoAi(true);
        helper.getLevel().addFreshEntity(golem);
        for (BlockPos pos : BlockPos.betweenClosed(12, 1, 3, 19, 1, 18)) helper.getLevel().setBlockAndUpdate(helper.absolutePos(pos), Blocks.WATER.defaultBlockState());
        FishingGoal goal = new FishingGoal(golem);
        boolean running = false;
        int casts = 0;
        int longest = 0;
        int age = 0;
        AABB box = new AABB(golem.getRestrictCenter()).inflate(20);
        for (int tick = 0; tick < 20000; tick++) {
            golem.tickCount = tick;
            if (running && !goal.canContinueToUse()) { goal.stop(); running = false; }
            if (!running && goal.canUse()) { goal.start(); running = true; casts++; age = 0; }
            if (running) { goal.tick(); longest = Math.max(longest, ++age); }
            for (GolemBobber bobber : helper.getLevel().getEntitiesOfClass(GolemBobber.class, box, entity -> entity.getFisher() == golem)) if (bobber.isAlive()) bobber.tick();
        }
        goal.stop();
        int catches = helper.getLevel().getEntitiesOfClass(ItemEntity.class, box, entity -> entity.getItem().is(ItemTags.FISHES)).size();
        helper.assertTrue(casts > 10 && catches > 3 && longest > 100, "repeated fishing failed: casts=" + casts + ", catches=" + catches + ", longest=" + longest);
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 800, batch = "golem_core")
    public static void fishingWoodChest(GameTestHelper helper) {
        GolemBase golem = setup(helper, 11);
        golem.configure(GolemType.WOOD, new GolemSnapshot(11, false, List.of(), "", List.of(), ItemContainerContents.EMPTY, List.of(), 0));
        BlockPos chest = golem.getRestrictCenter();
        helper.getLevel().setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());
        golem.setHome(chest.above(), Direction.UP);
        golem.setPos(chest.getX() + 0.5, chest.getY() + 1, chest.getZ() + 0.5);
        helper.getLevel().addFreshEntity(golem);
        for (BlockPos pos : BlockPos.betweenClosed(11, 0, 2, 22, 1, 22)) helper.getLevel().setBlockAndUpdate(helper.absolutePos(pos), Blocks.WATER.defaultBlockState());
        AABB box = new AABB(golem.getRestrictCenter()).inflate(20);
        int casts = 0;
        int longest = 0;
        int age = 0;
        GolemBobber previous = null;
        for (int tick = 0; tick < 30000; tick++) {
            golem.tick();
            var bobbers = helper.getLevel().getEntitiesOfClass(GolemBobber.class, box, entity -> entity.getFisher() == golem);
            if (!bobbers.isEmpty()) {
                GolemBobber bobber = bobbers.getFirst();
                if (bobber != previous) { previous = bobber; casts++; age = 0; }
                if (bobber.isAlive()) { bobber.tick(); longest = Math.max(longest, ++age); }
            }
        }
        int catches = helper.getLevel().getEntitiesOfClass(ItemEntity.class, box, entity -> entity.getItem().is(ItemTags.FISHES)).size();
        Thaumcraft.LOGGER.info("Wooden golem fishing on chest: {} casts, {} fish in 30000 ticks, longest cast {}", casts, catches, longest);
        helper.assertTrue(casts > 10 && catches > 3 && longest > 100, "wood golem on chest stops fishing: casts=" + casts + ", catches=" + catches + ", longest=" + longest);
        helper.succeed();
    }
}
