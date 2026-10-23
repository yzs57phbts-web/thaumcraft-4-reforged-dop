package thaumcraft.test;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import thaumcraft.Thaumcraft;
import thaumcraft.arcane.FocalManipulatorBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.device.ArcaneBoreBlock;
import thaumcraft.device.ArcaneBoreBlockEntity;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.network.BoreDigPayload;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;

@net.neoforged.neoforge.gametest.GameTestHolder(Thaumcraft.MODID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class DeviceRegressionGameTests {
    private DeviceRegressionGameTests() {
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 300, batch = "device_regression")
    public static void jarWandEmptying(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_GREATWOOD, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        for (var block : new Block[] {ModBlocks.WARDED_JAR.get(), ModBlocks.VOID_JAR.get()}) {
            for (boolean labelled : new boolean[] {false, true}) {
                level.setBlockAndUpdate(pos, block.defaultBlockState());
                JarBlockEntity jar = (JarBlockEntity) level.getBlockEntity(pos);
                jar.setAspect(Aspect.AIR);
                jar.setAmount(64);
                jar.setAspectFilter(labelled ? Aspect.AIR : null);
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), jar.getFacing(), pos, false);
                UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
                player.setShiftKeyDown(false);
                helper.assertTrue(wand.getItem().useOn(context) == InteractionResult.PASS && jar.getAmount() == 64,
                    "normal wand use emptied the jar");
                player.setShiftKeyDown(true);
                helper.assertTrue(wand.getItem().useOn(context) == InteractionResult.SUCCESS, "wand interaction was not consumed");
                helper.assertTrue(jar.getAmount() == 0 && jar.getComparatorSignal() == 0, "jar contents or comparator were not cleared");
                helper.assertTrue(jar.getAspectFilter() == (labelled ? Aspect.AIR : null), "wand removed the filter");
                helper.assertTrue(jar.getAspect() == (labelled ? Aspect.AIR : null), "empty jar has the wrong aspect");
                helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos)).isEmpty(), "wand dropped a label");
                helper.assertTrue(wand.getItem().useOn(context) == InteractionResult.SUCCESS, "empty jar did not consume wand use");
                level.removeBlock(pos, false);
            }
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 300, batch = "device_regression")
    public static void wardingFieldCollisions(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos stone = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(stone, ModBlocks.WARDING_PAVING_STONE.get().defaultBlockState());
        level.setBlockAndUpdate(stone.above(), ModBlocks.WARDING_AURA.get().defaultBlockState());
        level.setBlockAndUpdate(stone.above(2), ModBlocks.WARDING_AURA.get().defaultBlockState());
        var mob = EntityType.ZOMBIE.create(level);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(mob != null, "no test mob");
        for (int height = 1; height <= 2; height++) {
            BlockPos pos = stone.above(height);
            var state = level.getBlockState(pos);
            helper.assertTrue(!state.getCollisionShape(level, pos, CollisionContext.of(mob)).isEmpty(), "field did not block a mob");
            helper.assertTrue(state.getCollisionShape(level, pos, CollisionContext.of(player)).isEmpty(), "field blocked a player");
        }
        level.setBlockAndUpdate(stone.west(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        for (int height = 1; height <= 2; height++) {
            BlockPos pos = stone.above(height);
            helper.assertTrue(level.getBlockState(pos).getCollisionShape(level, pos, CollisionContext.of(mob)).isEmpty(), "powered field still blocks mobs");
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 300, batch = "device_regression")
    public static void transparentLighting(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        for (var block : new Block[] {ModBlocks.BRAIN_JAR.get(), ModBlocks.AIR_CRYSTAL_CLUSTER.get(), ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
            ModBlocks.WATER_CRYSTAL_CLUSTER.get(), ModBlocks.EARTH_CRYSTAL_CLUSTER.get(), ModBlocks.ORDER_CRYSTAL_CLUSTER.get(),
            ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get(), ModBlocks.MIXED_CRYSTAL_CLUSTER.get(), ModBlocks.STRANGE_CRYSTALS.get()}) {
            var state = block.defaultBlockState();
            helper.assertTrue(state.getShadeBrightness(level, pos) == 1.0F, "device casts ambient occlusion: " + block);
            helper.assertTrue(state.getLightBlock(level, pos) == 0, "device blocks light: " + block);
            helper.assertTrue(!state.isViewBlocking(level, pos), "device blocks the view: " + block);
            helper.assertTrue(!state.isSuffocating(level, pos), "device suffocates: " + block);
            helper.assertTrue(!state.getCollisionShape(level, pos).isEmpty(), "device lost collision: " + block);
            helper.assertTrue(state.getLightEmission() == (block == ModBlocks.BRAIN_JAR.get() ? 9 : 7), "device light emission changed");
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 300, batch = "device_regression")
    public static void boreTargetPayload(GameTestHelper helper) {
        for (boolean broken : new boolean[] {false, true}) {
            for (Direction direction : Direction.values()) {
                BlockPos bore = new BlockPos(-101234, -20, 98765);
                BlockPos target = bore.relative(direction, 65).offset(-2, 1, 2);
                BoreDigPayload expected = new BoreDigPayload(bore, target, 0x12345, broken);
                RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
                try {
                    BoreDigPayload.STREAM_CODEC.encode(buffer, expected);
                    helper.assertTrue(expected.equals(BoreDigPayload.STREAM_CODEC.decode(buffer)), "bore target or block state was truncated");
                    helper.assertTrue(buffer.readableBytes() == 0, "payload left unread bytes");
                } finally {
                    buffer.release();
                }
            }
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 300, batch = "device_regression")
    public static void boreDirectionalTargets(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 4, 3));
        for (Direction direction : Direction.values()) {
            for (Direction mounting : new Direction[] {Direction.UP, Direction.DOWN}) {
                if (direction == mounting.getOpposite()) {
                    continue;
                }
                for (BlockPos clear : BlockPos.betweenClosed(pos.offset(-3, -3, -3), pos.offset(3, 3, 3))) {
                    level.removeBlock(clear, false);
                }
                for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4))) {
                    drop.discard();
                }
                level.setBlockAndUpdate(pos.relative(mounting.getOpposite()), ModBlocks.ARCANE_BORE_BASE.get().defaultBlockState());
                level.setBlockAndUpdate(pos, ModBlocks.ARCANE_BORE.get().defaultBlockState().setValue(ArcaneBoreBlock.BASE, mounting));
                Direction powerSide = direction.getAxis() == Direction.Axis.X ? Direction.NORTH : Direction.WEST;
                level.setBlockAndUpdate(pos.relative(powerSide), Blocks.REDSTONE_BLOCK.defaultBlockState());
                BlockPos target = pos.relative(direction, 3);
                level.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
                ArcaneBoreBlockEntity bore = (ArcaneBoreBlockEntity) level.getBlockEntity(pos);
                bore.setOrientation(direction, true);
                bore.setItem(0, new ItemStack(ModItems.FOCUS_EXCAVATION.get()));
                bore.setItem(1, new ItemStack(Items.IRON_PICKAXE));
                for (int tick = 0; tick < 300 && !level.isEmptyBlock(target); tick++) {
                    ArcaneBoreBlockEntity.tick(level, pos, bore.getBlockState(), bore);
                }
                helper.assertTrue(level.isEmptyBlock(target), "bore missed target: " + direction + "/" + mounting);
                int drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4)).stream()
                    .filter(drop -> drop.getItem().is(Items.COBBLESTONE)).mapToInt(drop -> drop.getItem().getCount()).sum();
                helper.assertTrue(drops == 1 && bore.getItem(1).getDamageValue() == 1, "bore duplicated or lost drops: " + direction);
                level.removeBlock(pos.relative(powerSide), false);
                level.removeBlock(pos, false);
            }
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 300, batch = "device_regression")
    public static void focalFocusSync(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(pos, ModBlocks.FOCAL_MANIPULATOR.get().defaultBlockState());
        var table = (FocalManipulatorBlockEntity) level.getBlockEntity(pos);
        var copy = new FocalManipulatorBlockEntity(pos, table.getBlockState());
        copy.setLevel(level);
        table.setItem(0, new ItemStack(ModItems.FOCUS_EXCAVATION.get()));
        copy.loadWithComponents(table.getUpdateTag(level.registryAccess()), level.registryAccess());
        helper.assertTrue(copy.getItem(0).is(ModItems.FOCUS_EXCAVATION.get()), "focus did not synchronize");
        table.removeItem(0, 1);
        copy.loadWithComponents(table.getUpdateTag(level.registryAccess()), level.registryAccess());
        helper.assertTrue(copy.getItem(0).isEmpty(), "removed focus remained in render inventory");
        helper.succeed();
    }
}
