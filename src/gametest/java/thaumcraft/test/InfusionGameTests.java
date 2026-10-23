package thaumcraft.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.InfusionRunicAugmentRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.essentia.EssentiaHandler;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.infusion.InfusionMatrixBlockEntity;
import thaumcraft.infusion.InfusionPillarBlockEntity;
import thaumcraft.infusion.InfusionTriggers;
import thaumcraft.infusion.PedestalBlockEntity;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.ResearchManager;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class InfusionGameTests {
    private static final BlockPos[] RING = {
        new BlockPos(1, 2, 3), new BlockPos(5, 2, 3), new BlockPos(3, 2, 1), new BlockPos(3, 2, 5),
        new BlockPos(1, 2, 1), new BlockPos(5, 2, 5), new BlockPos(1, 2, 5), new BlockPos(5, 2, 1)
    };
    private static final BlockPos[] JARS = {
        new BlockPos(0, 5, 0), new BlockPos(6, 5, 0), new BlockPos(0, 5, 6), new BlockPos(6, 5, 6),
        new BlockPos(0, 6, 3), new BlockPos(6, 6, 3)
    };

    private InfusionGameTests() {
    }

    public static void register(IEventBus bus) {
    }

    private static ServerPlayer mockServerPlayer(GameTestHelper helper) {
        return TestSupport.mockServerPlayer(helper);
    }

    private static void buildFrame(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 2, 3), ModBlocks.ARCANE_PEDESTAL.get());
        helper.setBlock(new BlockPos(3, 4, 3), ModBlocks.RUNIC_MATRIX.get());
        for (int x = 2; x <= 4; x += 2) {
            for (int z = 2; z <= 4; z += 2) {
                helper.setBlock(new BlockPos(x, 2, z), ModBlocks.ARCANE_STONE_BRICKS.get());
                helper.setBlock(new BlockPos(x, 3, z), ModBlocks.ARCANE_STONE.get());
            }
        }
    }

    private static void form(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 2));
        InfusionTriggers.replace(helper.getLevel(), origin);
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void altarForms(GameTestHelper helper) {
        buildFrame(helper);
        BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.assertTrue(InfusionTriggers.fits(helper.getLevel(), origin), "blueprint does not fit");
        form(helper);
        InfusionMatrixBlockEntity matrix = helper.<InfusionMatrixBlockEntity>getBlockEntity(new BlockPos(3, 4, 3));
        helper.assertTrue(matrix.isActive(), "matrix not active");
        helper.assertTrue(matrix.validLocation(), "structure invalid");
        helper.assertTrue(helper.getBlockState(new BlockPos(2, 3, 2)).is(ModBlocks.INFUSION_PILLAR_TOP.get()), "no pillar top");
        helper.assertTrue(helper.getBlockState(new BlockPos(4, 2, 4)).is(ModBlocks.INFUSION_PILLAR.get()), "no pillar");
        helper.assertValueEqual(helper.<InfusionPillarBlockEntity>getBlockEntity(new BlockPos(4, 2, 4)).getOrientation(), 5, "orientation");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void stabilitySymmetry(GameTestHelper helper) {
        buildFrame(helper);
        form(helper);
        InfusionMatrixBlockEntity matrix = helper.<InfusionMatrixBlockEntity>getBlockEntity(new BlockPos(3, 4, 3));
        matrix.recalculateSurroundings();
        helper.assertValueEqual(matrix.getSymmetry(), 0, "empty symmetry");
        helper.setBlock(RING[0], ModBlocks.ARCANE_PEDESTAL.get());
        helper.<PedestalBlockEntity>getBlockEntity(RING[0]).setStack(new ItemStack(Items.STICK));
        matrix.recalculateSurroundings();
        helper.assertValueEqual(matrix.getSymmetry(), 3, "single pedestal symmetry");
        helper.setBlock(RING[1], ModBlocks.ARCANE_PEDESTAL.get());
        helper.<PedestalBlockEntity>getBlockEntity(RING[1]).setStack(new ItemStack(Items.STICK));
        matrix.recalculateSurroundings();
        helper.assertValueEqual(matrix.getSymmetry(), 0, "mirrored pedestal symmetry");
        Block candle = ModBlocks.CANDLES.values().iterator().next().get();
        for (int index = 0; index < 20; index++) {
            helper.setBlock(new BlockPos(0, 2 + index / 7, index % 7), candle);
        }
        matrix.recalculateSurroundings();
        helper.assertValueEqual(matrix.getSymmetry(), 2, "candle symmetry");
        helper.succeed();
    }

    private static @Nullable InfusionRecipe usableRecipe() {
        for (InfusionRecipe recipe : TcRecipes.all(InfusionRecipe.class)) {
            if (InfusionRunicAugmentRecipe.isDisplayRecipe(recipe.key()) || recipe.research().isEmpty()
                || !recipe.central().exists() || recipe.components().size() > RING.length || recipe.central().displayStacks().isEmpty()) {
                continue;
            }
            boolean usable = true;
            for (TcIngredient ingredient : recipe.components()) {
                usable &= ingredient.exists() && !ingredient.displayStacks().isEmpty();
            }
            if (usable && recipe.result().exists() && recipe.aspects().size() <= JARS.length) {
                return recipe;
            }
        }
        return null;
    }

    private static InfusionRecipe prepare(GameTestHelper helper, ServerPlayer player) {
        InfusionRecipe chosen = usableRecipe();
        helper.assertTrue(chosen != null, "no usable infusion recipe");
        prepare(helper, player, chosen);
        return chosen;
    }

    private static void prepare(GameTestHelper helper, ServerPlayer player, InfusionRecipe chosen) {
        ResearchManager.complete(player, chosen.research());
        buildFrame(helper);
        form(helper);
        ItemStack central = chosen.central().displayStacks().get(0).copyWithCount(1);
        helper.<PedestalBlockEntity>getBlockEntity(new BlockPos(3, 2, 3)).setStack(central);
        for (int index = 0; index < chosen.components().size(); index++) {
            helper.setBlock(RING[index], ModBlocks.ARCANE_PEDESTAL.get());
            helper.<PedestalBlockEntity>getBlockEntity(RING[index]).setStack(chosen.components().get(index).displayStacks().get(0).copyWithCount(1));
        }
    }

    private static InfusionMatrixBlockEntity start(GameTestHelper helper, ServerPlayer player) {
        InfusionMatrixBlockEntity matrix = helper.<InfusionMatrixBlockEntity>getBlockEntity(new BlockPos(3, 4, 3));
        matrix.recalculateSurroundings();
        matrix.craftingStart(player);
        helper.assertTrue(matrix.isCrafting(), "crafting did not start");
        return matrix;
    }

    private static void run(GameTestHelper helper, InfusionMatrixBlockEntity matrix, int ticks) {
        BlockPos matrixPos = helper.absolutePos(new BlockPos(3, 4, 3));
        for (int tick = 0; tick < ticks && (tick == 0 || matrix.isCrafting()); tick++) {
            InfusionMatrixBlockEntity.tick(helper.getLevel(), matrixPos, matrix.getBlockState(), matrix);
        }
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void withoutEssentia(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        InfusionRecipe chosen = prepare(helper, player);
        ItemStack central = chosen.central().displayStacks().get(0);
        InfusionMatrixBlockEntity matrix = start(helper, player);
        run(helper, matrix, 600);
        PedestalBlockEntity center = helper.<PedestalBlockEntity>getBlockEntity(new BlockPos(3, 2, 3));
        helper.assertTrue(center.getStack().is(central.getItem()), "central item changed without essentia");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void withJars(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        InfusionRecipe chosen = prepare(helper, player);
        craftWithJars(helper, player, chosen, 5);
    }

    @GameTest(template = "test/platform", timeoutTicks = 200, batch = "infusion_floor_jars")
    public static void zephyrSwordWithFloorJars(GameTestHelper helper) {
        zephyrSwordWithJars(helper, 2);
    }

    @GameTest(template = "test/platform", timeoutTicks = 200, batch = "infusion_level_jars")
    public static void zephyrSwordWithLevelJars(GameTestHelper helper) {
        zephyrSwordWithJars(helper, 4);
    }

    @GameTest(template = "test/platform", timeoutTicks = 200, batch = "infusion_ceiling_jars")
    public static void zephyrSwordWithCeilingJars(GameTestHelper helper) {
        zephyrSwordWithJars(helper, 6);
    }

    private static void zephyrSwordWithJars(GameTestHelper helper, int jarHeight) {
        ServerPlayer player = mockServerPlayer(helper);
        InfusionRecipe chosen = (InfusionRecipe) TcRecipes.get("ElementalSword");
        helper.assertTrue(chosen != null, "Sword of the Zephyr recipe missing");
        prepare(helper, player, chosen);
        craftWithJars(helper, player, chosen, jarHeight);
    }

    private static BlockPos jarPos(int index, int height) {
        return new BlockPos(JARS[index].getX(), height, JARS[index].getZ());
    }

    private static void craftWithJars(GameTestHelper helper, ServerPlayer player, InfusionRecipe chosen, int jarHeight) {
        List<Aspect> aspects = chosen.aspects().getAspects();
        for (int index = 0; index < aspects.size(); index++) {
            BlockPos jarPos = jarPos(index, jarHeight);
            helper.setBlock(jarPos, ModBlocks.WARDED_JAR.get());
            JarBlockEntity jar = helper.<JarBlockEntity>getBlockEntity(jarPos);
            jar.setAspect(aspects.get(index));
            jar.setAmount(jar.getMaxAmount());
        }
        InfusionMatrixBlockEntity matrix = start(helper, player);
        EssentiaHandler.refreshSources(matrix);
        try {
            Field instability = InfusionMatrixBlockEntity.class.getDeclaredField("instability");
            instability.setAccessible(true);
            instability.setInt(matrix, 0);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        run(helper, matrix, 10);
        helper.assertValueEqual(matrix.getAspects().visSize(), chosen.aspects().visSize() - 1, "matrix did not drain essentia from jars");
        run(helper, matrix, 4000);
        helper.assertFalse(matrix.isCrafting(), "crafting did not finish");
        PedestalBlockEntity center = helper.<PedestalBlockEntity>getBlockEntity(new BlockPos(3, 2, 3));
        helper.assertTrue(chosen.result().matches(center.getStack()), "central item is not the result: " + center.getStack());
        for (int index = 0; index < chosen.components().size(); index++) {
            helper.assertTrue(helper.<PedestalBlockEntity>getBlockEntity(RING[index]).getStack().isEmpty(), "ingredient not consumed: " + index);
        }
        for (int index = 0; index < aspects.size(); index++) {
            JarBlockEntity jar = helper.<JarBlockEntity>getBlockEntity(jarPos(index, jarHeight));
            helper.assertValueEqual(jar.getAmount(), jar.getMaxAmount() - chosen.aspects().getAmount(aspects.get(index)), "jar " + aspects.get(index).getTag());
        }
        for (int index = 0; index < aspects.size(); index++) {
            helper.setBlock(jarPos(index, jarHeight), Blocks.AIR);
        }
        EssentiaHandler.refreshSources(matrix);
        helper.succeed();
    }
}
