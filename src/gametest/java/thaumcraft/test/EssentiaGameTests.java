package thaumcraft.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.registry.ModBlocks;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class EssentiaGameTests {
    private EssentiaGameTests() {
    }

    public static void register(IEventBus bus) {
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void essentiaMovesThroughTubes(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos target = new BlockPos(4, 2, 1);
        helper.setBlock(source, ModBlocks.WARDED_JAR.get());
        helper.setBlock(target, ModBlocks.WARDED_JAR.get());
        for (int x = 1; x <= 4; x++) {
            helper.setBlock(new BlockPos(x, 3, 1), ModBlocks.ESSENTIA_TUBE.get());
        }
        JarBlockEntity sourceJar = helper.<JarBlockEntity>getBlockEntity(source);
        JarBlockEntity targetJar = helper.<JarBlockEntity>getBlockEntity(target);
        sourceJar.addToContainer(Aspect.AIR, 10);
        targetJar.setAspectFilter(Aspect.AIR);
        helper.succeedWhen(() -> {
            helper.assertTrue(targetJar.getAmount() >= 3, "essentia did not reach the second jar");
            helper.assertTrue(sourceJar.getAmount() + targetJar.getAmount() <= 10, "essentia was duplicated");
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void voidJarVoidsOverflow(GameTestHelper helper) {
        BlockPos position = new BlockPos(1, 2, 1);
        helper.setBlock(position, ModBlocks.VOID_JAR.get());
        JarBlockEntity voidJar = helper.<JarBlockEntity>getBlockEntity(position);
        helper.assertValueEqual(voidJar.addToContainer(Aspect.FIRE, 60), 0, "first add remainder");
        helper.assertValueEqual(voidJar.addToContainer(Aspect.FIRE, 20), 0, "overflow remainder");
        helper.assertValueEqual(voidJar.getAmount(), 64, "void jar amount");
        BlockPos plainPosition = new BlockPos(3, 2, 1);
        helper.setBlock(plainPosition, ModBlocks.WARDED_JAR.get());
        JarBlockEntity plainJar = helper.<JarBlockEntity>getBlockEntity(plainPosition);
        plainJar.addToContainer(Aspect.FIRE, 60);
        helper.assertValueEqual(plainJar.addToContainer(Aspect.FIRE, 20), 16, "plain jar remainder");
        helper.succeed();
    }
}
