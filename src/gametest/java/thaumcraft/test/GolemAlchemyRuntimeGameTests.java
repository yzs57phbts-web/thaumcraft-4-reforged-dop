package thaumcraft.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.AlembicBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemKeys;
import thaumcraft.entity.golem.GolemSnapshot;
import thaumcraft.entity.golem.GolemType;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.registry.ModBlocks;

@GameTestHolder("thaumcraft_golem_runtime")
@PrefixGameTestTemplate(false)
public final class GolemAlchemyRuntimeGameTests {
    private static long pacedTick = Long.MIN_VALUE;
    private static long pacedAt;

    private static void pace(long gameTime) {
        if (gameTime == pacedTick) return;
        long wait = 50_000_000L - (System.nanoTime() - pacedAt);
        if (wait > 0) java.util.concurrent.locks.LockSupport.parkNanos(wait);
        pacedAt = System.nanoTime();
        pacedTick = gameTime;
    }

    private static BlockHitResult hit(BlockPos pos, Direction side) {
        return new BlockHitResult(Vec3.atCenterOf(pos).add(side.getStepX() * 0.5, side.getStepY() * 0.5, side.getStepZ() * 0.5), side, pos, false);
    }

    private static void transfers(GameTestHelper helper, boolean onAlembic, int jarDistance, Direction attachment) {
        var level = helper.getLevel();
        helper.onEachTick(() -> pace(level.getGameTime()));
        for (BlockPos pos : BlockPos.betweenClosed(0, 1, 0, 15, 1, 15)) helper.setBlock(pos, Blocks.STONE);
        BlockPos furnace = helper.absolutePos(new BlockPos(4, 2, 5));
        BlockPos jarPos = furnace.east(jarDistance);
        level.setBlockAndUpdate(furnace, ModBlocks.ALCHEMICAL_FURNACE.get().defaultBlockState());
        level.setBlockAndUpdate(furnace.above(), ModBlocks.ALEMBIC.get().defaultBlockState());
        level.setBlockAndUpdate(jarPos, ModBlocks.WARDED_JAR.get().defaultBlockState());
        var alembic = (AlembicBlockEntity) level.getBlockEntity(furnace.above());
        alembic.addToContainer(Aspect.EARTH, 32);
        var jar = (JarBlockEntity) level.getBlockEntity(jarPos);
        var player = TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.CREATIVE);
        player.setPos(furnace.getX(), furnace.getY(), furnace.getZ() - 2);
        ItemStack packed = new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id(GolemType.FLESH.itemId())));
        GolemKeys.STATE.set(packed, new GolemSnapshot(6, false, List.of(), "", List.of(), ItemContainerContents.EMPTY, List.of(), 0));
        player.setItemInHand(InteractionHand.MAIN_HAND, packed);
        BlockPos sourceBlock = onAlembic ? furnace.above() : furnace;
        Direction face = onAlembic ? Direction.UP : attachment;
        helper.assertTrue(player.gameMode.useItemOn(player, level, packed, InteractionHand.MAIN_HAND, hit(sourceBlock, face)).consumesAction(), "actual golem placement failed");
        var placed = level.getEntitiesOfClass(GolemBase.class, new AABB(furnace).inflate(4));
        helper.assertValueEqual(placed.size(), 1, "unexpected golem count");
        GolemBase golem = placed.getFirst();
        helper.assertTrue(golem.getRestrictCenter().equals(sourceBlock.relative(face)) && golem.getHomeFacing() == face, "placement attachment lost");
        ItemStack bell = new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id("golem_bell")));
        player.setItemInHand(InteractionHand.MAIN_HAND, bell);
        player.interactOn(golem, InteractionHand.MAIN_HAND);
        helper.assertTrue(GolemKeys.BELL.get(player.getMainHandItem()).homeBlock().equals(sourceBlock), "bell points to air instead of the attached source block");
        player.gameMode.useItemOn(player, level, bell, InteractionHand.MAIN_HAND, hit(jarPos, Direction.NORTH));
        helper.assertValueEqual(golem.getMarkers().size(), 1, "jar binding failed");
        player.setPos(furnace.getX() - 4, furnace.getY(), furnace.getZ() - 4);
        helper.runAtTickTime(590, () -> System.out.println("GOLEM_RUNTIME_STATE direct=" + onAlembic + " jarDistance=" + jarDistance + " face=" + face + " pos=" + golem.position() + " home=" + golem.getRestrictCenter() + " source=" + alembic.getAmount() + " jar=" + jar.getAmount() + " carried=" + golem.getEssentiaAmount() + " navDone=" + golem.getNavigation().isDone()));
        helper.succeedWhen(() -> {
            helper.assertValueEqual(alembic.getAmount() + jar.getAmount() + golem.getEssentiaAmount(), 32, "essentia lost or duplicated in normal entity AI");
            helper.assertValueEqual(jar.getAmount(), 32, "normal AI failed to finish repeated delivery: source=" + alembic.getAmount() + " carried=" + golem.getEssentiaAmount() + " pos=" + golem.position());
        });
    }

    @GameTest(template = "test/large", timeoutTicks = 1200, batch = "golem_runtime")
    public static void furnaceSideNearJar(GameTestHelper helper) { transfers(helper, false, 3, Direction.EAST); }

    @GameTest(template = "test/large", timeoutTicks = 1200, batch = "golem_runtime")
    public static void furnaceSideFarJar(GameTestHelper helper) { transfers(helper, false, 9, Direction.EAST); }

    @GameTest(template = "test/large", timeoutTicks = 1200, batch = "golem_runtime")
    public static void furnaceNorthFarJar(GameTestHelper helper) { transfers(helper, false, 9, Direction.NORTH); }

    @GameTest(template = "test/large", timeoutTicks = 1200, batch = "golem_runtime")
    public static void furnaceSouthFarJar(GameTestHelper helper) { transfers(helper, false, 9, Direction.SOUTH); }

    @GameTest(template = "test/large", timeoutTicks = 1200, batch = "golem_runtime")
    public static void furnaceWestFarJar(GameTestHelper helper) { transfers(helper, false, 9, Direction.WEST); }

    @GameTest(template = "test/large", timeoutTicks = 1200, batch = "golem_runtime")
    public static void directAlembicNearJar(GameTestHelper helper) { transfers(helper, true, 3, Direction.UP); }

    @GameTest(template = "test/large", timeoutTicks = 1200, batch = "golem_runtime")
    public static void directAlembicFarJar(GameTestHelper helper) { transfers(helper, true, 9, Direction.UP); }

    @GameTest(template = "test/large", batch = "golem_runtime_codec")
    public static void bellSelectionRetainsSourceFaceAndAcceptsOldSaves(GameTestHelper helper) {
        var home = net.minecraft.core.GlobalPos.of(helper.getLevel().dimension(), new BlockPos(8, 3, 8));
        var selection = new GolemKeys.BellSelection(java.util.UUID.randomUUID(), home, List.of(), Direction.WEST);
        var encoded = GolemKeys.BELL_CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, selection).getOrThrow();
        var restored = GolemKeys.BELL_CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, encoded).getOrThrow();
        helper.assertTrue(restored.homeFacing() == Direction.WEST && restored.homeBlock().equals(home.pos().east()), "source face did not survive bell serialization");
        encoded.getAsJsonObject().remove("home_facing");
        var legacy = GolemKeys.BELL_CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, encoded).getOrThrow();
        helper.assertTrue(legacy.homeFacing() == Direction.UP && legacy.entity().equals(selection.entity()), "old bell component could not be loaded");
        helper.succeed();
    }

    @GameTest(template = "test/large", batch = "golem_runtime_rebind")
    public static void bellRebindsGroundGolemToFurnaceSide(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos furnace = helper.absolutePos(new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.STONE);
        level.setBlockAndUpdate(furnace, ModBlocks.ALCHEMICAL_FURNACE.get().defaultBlockState());
        var golem = new GolemBase(thaumcraft.registry.ModEntities.GOLEM.get(), level);
        golem.configure(GolemType.FLESH, new GolemSnapshot(6, false, List.of(), "", List.of(), ItemContainerContents.EMPTY, List.of(), 0));
        golem.setHome(furnace.south(2), Direction.UP);
        golem.setPos(furnace.getX() + 0.5, furnace.getY(), furnace.getZ() + 2.5);
        level.addFreshEntity(golem);
        var player = TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.CREATIVE);
        player.setPos(golem.position());
        var bell = new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id("golem_bell")));
        player.setItemInHand(InteractionHand.MAIN_HAND, bell);
        player.interactOn(golem, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(true);
        player.gameMode.useItemOn(player, level, bell, InteractionHand.MAIN_HAND, hit(furnace, Direction.SOUTH));
        helper.assertTrue(golem.getRestrictCenter().equals(furnace.south()) && golem.getHomeFacing() == Direction.SOUTH, "ground attachment was not replaced by the furnace side");
        helper.assertTrue(GolemKeys.BELL.get(player.getMainHandItem()).homeBlock().equals(furnace), "rebound bell still shows the old ground point");
        helper.succeed();
    }
}
