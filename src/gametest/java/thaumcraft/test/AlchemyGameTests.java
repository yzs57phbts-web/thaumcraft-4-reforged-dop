package thaumcraft.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.CrucibleBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.platform.Transfer;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.research.Knowledge;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandTriggers;
import thaumcraft.wand.Wands;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class AlchemyGameTests {
    private AlchemyGameTests() {
    }

    public static void register(IEventBus bus) {
    }

    private static final class CrucibleSyncProbe extends CrucibleBlockEntity {
        int updates;

        CrucibleSyncProbe(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public void sync() {
            updates++;
            super.sync();
        }
    }

    @GameTest(template = "test/platform", timeoutTicks = 40)
    public static void crucibleBucketUpdatesClientWater(GameTestHelper helper) {
        BlockPos offset = new BlockPos(2, 2, 2);
        helper.setBlock(offset, ModBlocks.CRUCIBLE.get());
        BlockPos pos = helper.absolutePos(offset);
        BlockState state = helper.getBlockState(offset);
        CrucibleSyncProbe crucible = new CrucibleSyncProbe(pos, state);
        helper.getLevel().setBlockEntity(crucible);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        state.useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "water bucket was not consumed");
        helper.assertValueEqual(crucible.getWaterAmount(), 1000, "server water after bucket fill");
        helper.assertTrue(crucible.updates > 0, "bucket fill did not request a client update");
        CrucibleBlockEntity clientCopy = new CrucibleBlockEntity(pos, state);
        clientCopy.loadWithComponents(crucible.getUpdateTag(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        helper.assertValueEqual(clientCopy.getWaterAmount(), 1000, "client water after update");
        helper.assertTrue(Math.abs(clientCopy.getFluidHeight() - 0.8F) < 0.0001F, "client water surface height");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 40)
    public static void directFluidTankChangesNotify(GameTestHelper helper) {
        int[] updates = {0};
        Transfer.FluidTank tank = new Transfer.FluidTank(1000, fluid -> fluid.is(Fluids.WATER), true, () -> updates[0]++);
        tank.setFluid(new FluidStack(Fluids.WATER, 1000));
        helper.assertValueEqual(updates[0], 1, "fill notification");
        tank.setFluid(new FluidStack(Fluids.WATER, 250));
        helper.assertValueEqual(updates[0], 2, "partial drain notification");
        helper.assertValueEqual(tank.getAmount(), 250, "partial drain amount");
        tank.setFluid(FluidStack.EMPTY);
        helper.assertValueEqual(updates[0], 3, "empty notification");
        helper.assertValueEqual(tank.getAmount(), 0, "empty tank amount");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void crucibleRecipeOutput(GameTestHelper helper) {
        BlockPos crucibleOffset = new BlockPos(2, 2, 2);
        helper.setBlock(crucibleOffset.below(), Blocks.LAVA);
        helper.setBlock(crucibleOffset, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible = helper.<CrucibleBlockEntity>getBlockEntity(crucibleOffset);
        crucible.fillWater(1000);
        crucible.setHeat(180);
        crucible.getAspects().add(Aspect.FIRE, 2).add(Aspect.WATER, 2).add(Aspect.EARTH, 2).add(Aspect.ORDER, 2).add(Aspect.ENTROPY, 2);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Knowledge.get(player).addResearch("CRUCIBLE");
        ItemEntity shard = helper.spawnItem(ModItems.AIR_SHARD.get(), crucibleOffset.above());
        shard.setThrower(player);
        helper.succeedWhen(() -> {
            helper.assertItemEntityPresent(ModItems.BALANCED_SHARD.get(), crucibleOffset, 2.0);
            helper.assertTrue(crucible.getAspects().isEmpty(), "aspects left in crucible");
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void cauldronToCrucible(GameTestHelper helper) {
        BlockPos offset = new BlockPos(2, 2, 2);
        helper.setBlock(offset, Blocks.CAULDRON);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_GREATWOOD, false);
        Wands.fill(wand);
        boolean done = WandTriggers.perform(helper.getLevel(), wand, player, helper.absolutePos(offset), Direction.UP);
        helper.assertTrue(done, "trigger did not fire");
        helper.assertBlockPresent(ModBlocks.CRUCIBLE.get(), offset);
        helper.succeed();
    }
}
