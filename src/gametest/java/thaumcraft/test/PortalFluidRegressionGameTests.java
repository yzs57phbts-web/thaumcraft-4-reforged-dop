package thaumcraft.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.block.LiquidDeathBlock;
import thaumcraft.registry.ModBlocks;

/** Selected existing regressions plus a real scheduled-flow conservation scenario. */
@GameTestHolder("thaumcraft_portal_fluid")
@PrefixGameTestTemplate(false)
public final class PortalFluidRegressionGameTests {
    private PortalFluidRegressionGameTests() {}

    @GameTest(template = "test/platform", batch = "portal_first_click", timeoutTicks = 1200)
    public static void portalOpensBeforeMazeIsReady(GameTestHelper helper) {
        EldritchGameTests.altarVisActivation(helper);
    }

    @GameTest(template = "test/platform", batch = "fluid_motion_purifying", timeoutTicks = 120)
    public static void purifyingMotion(GameTestHelper helper) {
        IssueRegressionGameTests.purifyingFluidAllowsTravel(helper);
    }

    @GameTest(template = "test/platform", batch = "fluid_motion_death", timeoutTicks = 120)
    public static void liquidDeathMotion(GameTestHelper helper) {
        IssueRegressionGameTests.liquidDeathAllowsTravel(helper);
    }

    @GameTest(template = "test/platform", batch = "fluid_motion_water", timeoutTicks = 120)
    public static void waterMotionControl(GameTestHelper helper) {
        IssueRegressionGameTests.vanillaWaterControlAllowsTravel(helper);
    }

    @GameTest(template = "test/eldritch_regression", batch = "fluid_drop_search", timeoutTicks = 160)
    public static void purifyingChoosesDropAndStaysBounded(GameTestHelper helper) {
        IssueRegressionGameTests.purifyingFluidFindsDropAndHasBoundedSpread(helper);
    }

    @GameTest(template = "test/platform", batch = "fluid_source_regeneration", timeoutTicks = 240)
    public static void purifyingCannotRegenerateConsumedSource(GameTestHelper helper) {
        FluidGameTests.purifyingFluidNoSourceRegeneration(helper);
    }

    @GameTest(template = "test/platform", batch = "fluid_death_horizontal", timeoutTicks = 240)
    public static void liquidDeathConservesHorizontalVolume(GameTestHelper helper) {
        FluidGameTests.liquidDeathHorizontalQuanta(helper);
    }

    @GameTest(template = "test/platform", batch = "fluid_death_vertical", timeoutTicks = 240)
    public static void liquidDeathConservesVerticalVolume(GameTestHelper helper) {
        FluidGameTests.liquidDeathVerticalQuanta(helper);
    }

    @GameTest(template = "test/eldritch_regression", batch = "fluid_death_mixed_basin", timeoutTicks = 240)
    public static void liquidDeathPreservesMixedVolumeAroundDrop(GameTestHelper helper) {
        BlockPos center = new BlockPos(16, 5, 16);
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                helper.setBlock(center.offset(x, -2, z), Blocks.STONE);
                helper.setBlock(center.offset(x, -1, z), Blocks.STONE);
                for (int y = 0; y <= 2; y++) {
                    helper.setBlock(center.offset(x, y, z), Math.abs(x) == 8 || Math.abs(z) == 8 ? Blocks.STONE : Blocks.AIR);
                }
            }
        }
        // A real one-block drop under a full source, plus full and partial neighbours.
        helper.setBlock(center.below(), Blocks.AIR);
        helper.setBlock(center, ModBlocks.LIQUID_DEATH.get());
        helper.setBlock(center.east(), ModBlocks.LIQUID_DEATH.get());
        helper.setBlock(center.north(), ModBlocks.LIQUID_DEATH.get().defaultBlockState().setValue(LiquidBlock.LEVEL, 2));
        helper.onEachTick(() -> {
            int volume = 0;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -1, -8), center.offset(8, 2, 8))) {
                volume += quanta(helper.getBlockState(pos));
            }
            helper.assertValueEqual(volume, 10, "scheduled flow duplicated or lost finite liquid death volume");
        });
        helper.runAfterDelay(10, () -> helper.assertTrue(quanta(helper.getBlockState(center.below())) > 0, "liquid death did not descend into the drop"));
        helper.runAfterDelay(180, helper::succeed);
    }

    private static int quanta(BlockState state) {
        if (!(state.getBlock() instanceof LiquidDeathBlock)) return 0;
        int level = state.getValue(LiquidBlock.LEVEL);
        return level == 0 ? LiquidDeathBlock.QUANTA_PER_BLOCK : LiquidDeathBlock.QUANTA_PER_BLOCK - level;
    }
}
