package thaumcraft.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.FluxBlock;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.essentia.EssentiaHandler;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.infusion.InfusionMatrixBlockEntity;
import thaumcraft.infusion.InfusionTriggers;
import thaumcraft.infusion.PedestalBlockEntity;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.ResearchManager;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class InfusionAuditGameTests {
    private static final BlockPos MATRIX = new BlockPos(16, 5, 16);
    private static final BlockPos CENTER = MATRIX.below(2);
    private static final BlockPos[] RING = {
        CENTER.offset(-2, 0, 0), CENTER.offset(2, 0, 0),
        CENTER.offset(0, 0, -2), CENTER.offset(0, 0, 2)
    };

    private static void field(InfusionMatrixBlockEntity matrix, String name, int value) {
        try {
            Field field = InfusionMatrixBlockEntity.class.getDeclaredField(name);
            field.setAccessible(true);
            field.setInt(matrix, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void call(InfusionMatrixBlockEntity matrix, String name, ServerLevel level, Integer type) {
        try {
            Method method = type == null
                ? InfusionMatrixBlockEntity.class.getDeclaredMethod(name, ServerLevel.class)
                : InfusionMatrixBlockEntity.class.getDeclaredMethod(name, ServerLevel.class, int.class);
            method.setAccessible(true);
            if (type == null) method.invoke(matrix, level); else method.invoke(matrix, level, type.intValue());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static InfusionMatrixBlockEntity frame(GameTestHelper helper) {
        helper.setBlock(CENTER, ModBlocks.ARCANE_PEDESTAL.get());
        helper.setBlock(MATRIX, ModBlocks.RUNIC_MATRIX.get());
        for (int x : new int[] {-1, 1}) for (int z : new int[] {-1, 1}) {
            helper.setBlock(CENTER.offset(x, 0, z), ModBlocks.ARCANE_STONE_BRICKS.get());
            helper.setBlock(CENTER.offset(x, 1, z), ModBlocks.ARCANE_STONE.get());
        }
        InfusionTriggers.replace(helper.getLevel(), helper.absolutePos(CENTER.offset(-1, 0, -1)));
        return helper.getBlockEntity(MATRIX);
    }

    private static void stabilisers(GameTestHelper helper, boolean mirrored, int mirroredHeight) {
        Block candle = ModBlocks.CANDLES.values().iterator().next().get();
        for (int x = 3; x <= 6; x++) for (int z = 3; z <= 5; z++) {
            BlockPos first = CENTER.offset(-x, 0, -z);
            helper.setBlock(first.below(), Blocks.STONE);
            helper.setBlock(first, candle);
            if (mirrored) {
                BlockPos second = CENTER.offset(x, mirroredHeight, z);
                helper.setBlock(second.below(), Blocks.STONE);
                helper.setBlock(second, candle);
            }
        }
    }

    private static InfusionRecipe recipe() {
        return (InfusionRecipe) TcRecipes.get("ElementalSword");
    }

    private static InfusionMatrixBlockEntity prepare(GameTestHelper helper, ServerPlayer player, boolean researched) {
        InfusionMatrixBlockEntity matrix = frame(helper);
        InfusionRecipe recipe = recipe();
        if (researched) ResearchManager.complete(player, recipe.research());
        helper.<PedestalBlockEntity>getBlockEntity(CENTER).setStack(recipe.central().displayStacks().getFirst());
        for (int index = 0; index < RING.length; index++) {
            helper.setBlock(RING[index], ModBlocks.ARCANE_PEDESTAL.get());
            helper.<PedestalBlockEntity>getBlockEntity(RING[index]).setStack(recipe.components().get(index).displayStacks().getFirst());
        }
        stabilisers(helper, true, 0);
        matrix.recalculateSurroundings();
        return matrix;
    }

    private static void jars(GameTestHelper helper) {
        List<Aspect> aspects = recipe().aspects().getAspects();
        for (int index = 0; index < aspects.size(); index++) {
            BlockPos pos = new BlockPos(12 + index, 3, 12);
            helper.setBlock(pos, ModBlocks.WARDED_JAR.get());
            JarBlockEntity jar = helper.getBlockEntity(pos);
            jar.addToContainer(aspects.get(index), 64);
        }
    }

    private static void ticks(GameTestHelper helper, InfusionMatrixBlockEntity matrix, int count) {
        for (int index = 0; index < count && matrix.isCrafting(); index++) {
            InfusionMatrixBlockEntity.tick(helper.getLevel(), helper.absolutePos(MATRIX), matrix.getBlockState(), matrix);
        }
    }

    private static void nonDestructiveBurst(GameTestHelper helper, Runnable trigger) {
        AtomicReference<Boolean> destructive = new AtomicReference<>();
        Consumer<ExplosionEvent.Start> listener = event -> {
            if (event.getLevel() == helper.getLevel()) destructive.set(event.getExplosion().interactsWithBlocks());
        };
        NeoForge.EVENT_BUS.addListener(listener);
        try {
            trigger.run();
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        helper.assertTrue(Boolean.FALSE.equals(destructive.get()), "instability explosion must preserve terrain as in TC4");
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_symmetry")
    public static void pairedStabilisers(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = frame(helper);
        stabilisers(helper, false, 0);
        matrix.recalculateSurroundings();
        helper.assertValueEqual(matrix.getSymmetry(), 1, "12 unpaired candles must add instability");
        stabilisers(helper, true, 0);
        matrix.recalculateSurroundings();
        helper.assertValueEqual(matrix.getSymmetry(), -2, "12 mirrored pairs must reduce instability");
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_height")
    public static void stabilisersNeedMatchingHeight(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = frame(helper);
        stabilisers(helper, true, 1);
        matrix.recalculateSurroundings();
        helper.assertValueEqual(matrix.getSymmetry(), 2, "candles at different heights must not form stable pairs");
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_pedestals")
    public static void pedestalScanBounds(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = frame(helper);
        BlockPos inside = MATRIX.offset(8, -1, 0);
        BlockPos hidden = inside.below();
        BlockPos outside = MATRIX.offset(9, -1, 0);
        BlockPos above = MATRIX.offset(0, 1, 4);
        for (BlockPos pos : List.of(inside, hidden, outside, above)) helper.setBlock(pos, ModBlocks.ARCANE_PEDESTAL.get());
        matrix.recalculateSurroundings();
        helper.assertTrue(matrix.getPedestals().equals(List.of(helper.absolutePos(inside))), "matrix selected a hidden, high or out-of-range pedestal: " + matrix.getPedestals());
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_matrix_burst")
    public static void matrixBurstPreservesBlocks(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        InfusionMatrixBlockEntity matrix = prepare(helper, player, true);
        matrix.craftingStart(player);
        field(matrix, "instability", 500);
        long seed = 0;
        for (;; seed++) {
            RandomSource probe = RandomSource.create(seed);
            probe.nextInt(500);
            if (probe.nextInt(21) == 9) break;
        }
        BlockPos fragile = MATRIX.above();
        helper.setBlock(fragile, Blocks.GLASS);
        helper.getLevel().getRandom().setSeed(seed);
        nonDestructiveBurst(helper, () -> call(matrix, "craftCycle", helper.getLevel(), null));
        helper.assertTrue(helper.getBlockState(fragile).is(Blocks.GLASS), "instability burst destroyed blocks unlike TC4");
        helper.assertTrue(helper.getBlockState(MATRIX).is(ModBlocks.RUNIC_MATRIX.get()), "instability burst destroyed its own matrix");
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_pedestal_burst")
    public static void pedestalBurstPreservesBlocks(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = frame(helper);
        helper.setBlock(RING[0], ModBlocks.ARCANE_PEDESTAL.get());
        helper.<PedestalBlockEntity>getBlockEntity(RING[0]).setStack(new ItemStack(Items.DIAMOND));
        matrix.recalculateSurroundings();
        BlockPos fragile = RING[0].offset(-1, 0, 0);
        helper.setBlock(fragile, Blocks.GLASS);
        nonDestructiveBurst(helper, () -> call(matrix, "inEvEjectItem", helper.getLevel(), 5));
        helper.assertTrue(helper.getBlockState(fragile).is(Blocks.GLASS), "pedestal burst destroyed nearby blocks unlike TC4");
        helper.assertTrue(helper.getBlockState(RING[0]).is(ModBlocks.ARCANE_PEDESTAL.get()), "pedestal burst destroyed its pedestal");
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_flux")
    public static void instabilityFluxMatchesLegacyAmount(GameTestHelper helper) {
        InfusionMatrixBlockEntity matrix = frame(helper);
        helper.setBlock(RING[0], ModBlocks.ARCANE_PEDESTAL.get());
        matrix.recalculateSurroundings();
        for (int type : new int[] {1, 2}) {
            helper.<PedestalBlockEntity>getBlockEntity(RING[0]).setStack(new ItemStack(Items.DIAMOND));
            call(matrix, "inEvEjectItem", helper.getLevel(), type);
            helper.assertValueEqual(helper.getBlockState(RING[0].above()).getValue(FluxBlock.LEVEL), 8, "infusion flux must match original metadata 7");
        }
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_research")
    public static void researchRequired(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        InfusionMatrixBlockEntity matrix = prepare(helper, player, false);
        matrix.craftingStart(player);
        helper.assertFalse(matrix.isCrafting(), "matrix bypassed research requirement");
        ResearchManager.complete(player, recipe().research());
        matrix.craftingStart(player);
        helper.assertTrue(matrix.isCrafting(), "matrix rejected researched recipe");
        helper.assertTrue(matrix.getInstability() < 0, "paired candles did not offset recipe instability");
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_reload", timeoutTicks = 200)
    public static void partialInfusionSurvivesReload(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        InfusionMatrixBlockEntity matrix = prepare(helper, player, true);
        jars(helper);
        matrix.craftingStart(player);
        EssentiaHandler.refreshSources(matrix);
        ticks(helper, matrix, 100);
        helper.assertTrue(matrix.getAspects().visSize() > 0 && matrix.getAspects().visSize() < recipe().aspects().visSize(), "infusion did not reach partial essentia phase");
        InfusionMatrixBlockEntity loaded = new InfusionMatrixBlockEntity(matrix.getBlockPos(), matrix.getBlockState());
        loaded.loadWithComponents(matrix.saveWithoutMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        helper.getLevel().setBlockEntity(loaded);
        ticks(helper, loaded, 1500);
        helper.assertFalse(loaded.isCrafting(), "infusion did not resume after reload");
        helper.assertTrue(recipe().result().matches(helper.<PedestalBlockEntity>getBlockEntity(CENTER).getStack()), "wrong output after reload");
        for (BlockPos pos : RING) helper.assertTrue(helper.<PedestalBlockEntity>getBlockEntity(pos).isEmpty(), "ingredient duplicated after reload");
        List<Aspect> aspects = recipe().aspects().getAspects();
        for (int index = 0; index < aspects.size(); index++) {
            JarBlockEntity jar = helper.getBlockEntity(new BlockPos(12 + index, 3, 12));
            helper.assertValueEqual(jar.getAmount(), 64 - recipe().aspects().getAmount(aspects.get(index)), "wrong essentia consumption after reload");
        }
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_consumed_reload", timeoutTicks = 200)
    public static void consumedIngredientsStayConsumedAfterReload(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        InfusionMatrixBlockEntity matrix = prepare(helper, player, true);
        jars(helper);
        matrix.craftingStart(player);
        EssentiaHandler.refreshSources(matrix);
        ticks(helper, matrix, 310);
        long remaining = List.of(RING).stream().filter(pos -> !helper.<PedestalBlockEntity>getBlockEntity(pos).isEmpty()).count();
        helper.assertTrue(remaining > 0 && remaining < RING.length && matrix.isCrafting(), "infusion did not reach partial ingredient phase");
        InfusionMatrixBlockEntity loaded = new InfusionMatrixBlockEntity(matrix.getBlockPos(), matrix.getBlockState());
        loaded.loadWithComponents(matrix.saveWithoutMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        helper.getLevel().setBlockEntity(loaded);
        ticks(helper, loaded, 1500);
        helper.assertFalse(loaded.isCrafting(), "consumed ingredients were requested again after reload");
        helper.assertTrue(recipe().result().matches(helper.<PedestalBlockEntity>getBlockEntity(CENTER).getStack()), "wrong output after ingredient phase reload");
        for (BlockPos pos : RING) helper.assertTrue(helper.<PedestalBlockEntity>getBlockEntity(pos).isEmpty(), "ingredient remained after resumed infusion");
        helper.succeed();
    }

    @GameTest(template = "test/eldritch_regression", batch = "infusion_audit_sanity")
    public static void sanityCheckerCanBeInfused(GameTestHelper helper) {
        BlockPos[] ring={RING[0],RING[1],RING[2],RING[3],CENTER.offset(-2,0,-2),CENTER.offset(2,0,2)};
        InfusionRecipe sanity = (InfusionRecipe) TcRecipes.get("SanityCheck");
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        ResearchManager.complete(player, sanity.research());
        InfusionMatrixBlockEntity matrix = frame(helper);
        helper.<PedestalBlockEntity>getBlockEntity(CENTER).setStack(sanity.central().displayStacks().getFirst());
        for (int index = 0; index < sanity.components().size(); index++) {
            helper.setBlock(ring[index], ModBlocks.ARCANE_PEDESTAL.get());
            helper.<PedestalBlockEntity>getBlockEntity(ring[index]).setStack(sanity.components().get(index).displayStacks().getFirst());
        }
        List<Aspect> aspects = sanity.aspects().getAspects();
        for (int index = 0; index < aspects.size(); index++) {
            BlockPos pos = new BlockPos(12 + index, 3, 12);
            helper.setBlock(pos, ModBlocks.WARDED_JAR.get());
            helper.<JarBlockEntity>getBlockEntity(pos).addToContainer(aspects.get(index), 64);
        }
        matrix.craftingStart(player);
        helper.assertTrue(matrix.isCrafting(), "sanity checker recipe cannot start");
        field(matrix, "instability", -1);
        EssentiaHandler.refreshSources(matrix);
        ticks(helper, matrix, 1500);
        ItemStack output = helper.<PedestalBlockEntity>getBlockEntity(CENTER).getStack();
        helper.assertFalse(matrix.isCrafting(), "sanity checker infusion did not finish");
        helper.assertTrue(output.is(thaumcraft.registry.ModItems.SANITY_CHECKER.get()) && output.getMaxStackSize() == 1, "sanity checker output missing or invalid");
        for (int index = 0; index < aspects.size(); index++) helper.assertValueEqual(helper.<JarBlockEntity>getBlockEntity(new BlockPos(12 + index, 3, 12)).getAmount(), 64 - sanity.aspects().getAmount(aspects.get(index)), "sanity checker essentia cost");
        helper.succeed();
    }
}
