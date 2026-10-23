package thaumcraft.test;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.CrucibleBlockEntity;
import thaumcraft.alchemy.AlchemyHooks;
import thaumcraft.device.ArcaneEarBlock;
import thaumcraft.device.ArcaneEarBlockEntity;
import thaumcraft.device.ChargedLampBlock;
import thaumcraft.device.ItemGrateBlock;
import thaumcraft.device.LampBlock;
import thaumcraft.device.LampOfGrowthBlockEntity;
import thaumcraft.platform.Transfer;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class DeviceGameTests {
    private DeviceGameTests() {
    }





    private static int countItems(GameTestHelper helper, Item item, BlockPos around) {
        List<ItemEntity> entities = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(around)).inflate(3.0));
        int total = 0;
        for (ItemEntity entity : entities) {
            if (entity.getItem().is(item)) {
                total += entity.getItem().getCount();
            }
        }
        return total;
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void nitorHeatsCrucible(GameTestHelper helper) {
        BlockPos crucibleOffset = new BlockPos(2, 3, 2);
        helper.setBlock(crucibleOffset.below(), ModBlocks.NITOR_LIGHT.get());
        helper.setBlock(crucibleOffset, ModBlocks.CRUCIBLE.get());
        helper.assertTrue(
            AlchemyHooks.isNitor(helper.getLevel(), helper.absolutePos(crucibleOffset.below()), helper.getBlockState(crucibleOffset.below())),
            "nitor not recognised as heat source"
        );
        CrucibleBlockEntity crucible = helper.<CrucibleBlockEntity>getBlockEntity(crucibleOffset);
        crucible.fillWater(1000);
        helper.succeedWhen(() -> helper.assertTrue(crucible.getHeat() > 20, "crucible heat " + crucible.getHeat()));
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void lampOfGrowthAdvancesCrop(GameTestHelper helper) {
        BlockPos cactus = new BlockPos(2, 3, 2);
        helper.setBlock(cactus.below(), Blocks.SAND);
        helper.setBlock(cactus, Blocks.CACTUS);
        BlockPos lampPos = new BlockPos(2, 6, 2);
        helper.setBlock(lampPos.above(), Blocks.STONE);
        helper.setBlock(lampPos, ModBlocks.LAMP_OF_GROWTH.get().defaultBlockState().setValue(LampBlock.FACING, Direction.UP));
        LampOfGrowthBlockEntity lamp = helper.<LampOfGrowthBlockEntity>getBlockEntity(lampPos);
        lamp.setCharges(100);
        helper.assertBlockProperty(lampPos, ChargedLampBlock.LIT, true);
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(cactus).getValue(CactusBlock.AGE) > 0, "cactus did not advance");
            helper.assertTrue(lamp.getCharges() < 100, "lamp spent no charge");
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void levitatorLiftsItem(GameTestHelper helper) {
        BlockPos levitator = new BlockPos(2, 2, 2);
        helper.setBlock(levitator, ModBlocks.ARCANE_LEVITATOR.get());
        ItemEntity item = helper.spawnItem(Items.STICK, levitator.above());
        double start = item.getY();
        helper.succeedWhen(() -> helper.assertTrue(item.getY() > start + 2.0, "item did not rise: " + (item.getY() - start)));
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void itemGrateOpenAndClosed(GameTestHelper helper) {
        BlockPos grate = new BlockPos(2, 4, 2);
        helper.setBlock(grate, ModBlocks.ITEM_GRATE.get());
        BlockState open = helper.getBlockState(grate);
        helper.assertTrue(!open.getValue(ItemGrateBlock.CLOSED), "grate starts closed");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemEntity probe = helper.spawnItem(Items.STICK, grate.above());
        helper.assertTrue(
            open.getCollisionShape(helper.getLevel(), helper.absolutePos(grate), CollisionContext.of(probe)).isEmpty(),
            "open grate blocks items"
        );
        helper.assertTrue(
            !open.getCollisionShape(helper.getLevel(), helper.absolutePos(grate), CollisionContext.of(player)).isEmpty(),
            "open grate lets players through"
        );
        probe.discard();
        ItemStack inserted = Transfer.insert(helper.getLevel(), helper.absolutePos(grate), Direction.UP, new ItemStack(Items.STICK, 3), false);
        helper.assertTrue(inserted.isEmpty(), "grate refused item from above");
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(countItems(helper, Items.STICK, grate) == 3, "grate did not drop the inserted item");
            helper.setBlock(grate.east(), Blocks.REDSTONE_BLOCK);
        });
        helper.runAfterDelay(5, () -> {
            helper.assertBlockProperty(grate, ItemGrateBlock.CLOSED, true);
            helper.assertTrue(
                !helper.getBlockState(grate).getCollisionShape(helper.getLevel(), helper.absolutePos(grate), CollisionContext.of(helper.spawnItem(Items.STICK, grate.above().above()))).isEmpty(),
                "closed grate lets items through"
            );
            helper.assertTrue(
                Transfer.insert(helper.getLevel(), helper.absolutePos(grate), Direction.UP, new ItemStack(Items.STICK), true).getCount() == 1,
                "closed grate accepts items"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void crystalClusterDrops(GameTestHelper helper) {
        BlockPos first = new BlockPos(1, 2, 1);
        BlockPos second = new BlockPos(3, 2, 1);
        BlockPos third = new BlockPos(5, 2, 1);
        helper.setBlock(first.below(), Blocks.STONE);
        helper.setBlock(first, ModBlocks.FIRE_CRYSTAL_CLUSTER.get());
        helper.setBlock(second, ModBlocks.MIXED_CRYSTAL_CLUSTER.get());
        helper.setBlock(third, ModBlocks.STRANGE_CRYSTALS.get());
        helper.assertBlockPresent(ModBlocks.FIRE_CRYSTAL_CLUSTER.get(), first);
        helper.getLevel().destroyBlock(helper.absolutePos(first), true);
        helper.getLevel().destroyBlock(helper.absolutePos(second), true);
        helper.getLevel().destroyBlock(helper.absolutePos(third), true);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(countItems(helper, ModItems.FIRE_SHARD.get(), first) == 7, "fire shards: " + countItems(helper, ModItems.FIRE_SHARD.get(), first));
            for (Item shard : List.of(ModItems.AIR_SHARD.get(), ModItems.WATER_SHARD.get(), ModItems.EARTH_SHARD.get(), ModItems.ORDER_SHARD.get(), ModItems.ENTROPY_SHARD.get())) {
                helper.assertTrue(countItems(helper, shard, second) >= 1, "mixed cluster dropped no " + shard);
            }
            helper.assertTrue(countItems(helper, ModItems.BALANCED_SHARD.get(), third) == 1, "strange crystals dropped no balanced shard");
            helper.succeed();
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void wardingStonePlacesAura(GameTestHelper helper) {
        BlockPos stone = new BlockPos(2, 2, 2);
        helper.setBlock(stone, ModBlocks.WARDING_PAVING_STONE.get());
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(ModBlocks.WARDING_AURA.get(), stone.above());
            helper.assertBlockPresent(ModBlocks.WARDING_AURA.get(), stone.above(2));
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void arcaneEarHearsNote(GameTestHelper helper) {
        BlockPos ear = new BlockPos(4, 2, 2);
        BlockPos noteBlock = new BlockPos(1, 2, 2);
        helper.setBlock(ear.below(), Blocks.DIRT);
        helper.setBlock(noteBlock.below(), Blocks.DIRT);
        helper.setBlock(ear, ModBlocks.ARCANE_EAR.get());
        helper.setBlock(noteBlock, Blocks.NOTE_BLOCK);
        helper.runAfterDelay(2, () -> helper.setBlock(noteBlock.north(), Blocks.REDSTONE_BLOCK));
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(ear, ArcaneEarBlock.POWERED, true);
            helper.assertTrue(helper.<ArcaneEarBlockEntity>getBlockEntity(ear).getRedstoneSignal() > 0, "ear has no signal");
        });
    }
}
