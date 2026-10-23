package thaumcraft.test;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.*;
import thaumcraft.block.WandPedestalBlock;
import thaumcraft.blockentity.WandPedestalBlockEntity;
import thaumcraft.node.*;
import thaumcraft.registry.*;
import thaumcraft.vis.VisNet;
import thaumcraft.wand.*;

@GameTestHolder(Thaumcraft.MODID) @PrefixGameTestTemplate(false)
public final class NodeDeviceGameTests {
    private static final BlockPos NODE=new BlockPos(3,3,3);
    private static NodeBlockEntity node(GameTestHelper helper, AspectList aspects) {
        var pos=helper.absolutePos(NODE);
        NodeGenerator.createNodeAt(helper.getLevel(),pos,NodeType.NORMAL,null,aspects);
        return (NodeBlockEntity)helper.getLevel().getBlockEntity(pos);
    }
    private static void device(GameTestHelper helper, boolean stabilizer) {
        if(stabilizer)helper.setBlock(NODE.below(),ModBlocks.NODE_STABILIZER.get());
        node(helper,new AspectList().add(Aspect.AIR,100).add(Aspect.FIRE,64));
        helper.setBlock(NODE.above(),ModBlocks.NODE_TRANSDUCER.get());
        helper.setBlock(NODE.above(2),Blocks.REDSTONE_BLOCK);
    }
    private static NodeTransducerBlockEntity transducer(GameTestHelper helper) {
        return (NodeTransducerBlockEntity)helper.getLevel().getBlockEntity(helper.absolutePos(NODE.above()));
    }
    @GameTest(template="test/platform",batch="node_device_late_stabilizer",timeoutTicks=100)
    public static void transducerStartsWhenStabilizerIsInstalledLast(GameTestHelper helper) {
        device(helper,false);
        helper.runAtTickTime(5,()->{
            helper.assertTrue(transducer(helper).getStatus()==0,"Incomplete structure started conversion");
            helper.setBlock(NODE.below(),ModBlocks.NODE_STABILIZER.get());
        });
        helper.runAtTickTime(30,()->{
            helper.assertTrue(transducer(helper).getStatus()==1 && transducer(helper).getCount()>0,"Transducer ignored the late stabilizer");
            helper.succeed();
        });
    }
    @GameTest(template="test/platform",batch="node_device_removed_stabilizer",timeoutTicks=100)
    public static void removingStabilizerStopsWarmupWithoutRedstoneToggle(GameTestHelper helper) {
        device(helper,true);
        int[] progress={0};
        helper.runAtTickTime(10,()->{progress[0]=transducer(helper).getCount();helper.setBlock(NODE.below(),Blocks.AIR);});
        helper.runAtTickTime(25,()->{
            helper.assertTrue(transducer(helper).getStatus()==0 && transducer(helper).getCount()<progress[0],"Unsupported transducer continued converting");
            helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(NODE)) instanceof NodeBlockEntity,"Interrupted ordinary node disappeared");
            helper.succeed();
        });
    }
    @GameTest(template="test/platform",batch="node_device_stabilizer_modes",timeoutTicks=220)
    public static void bothStabilizersLockNodesAndRedstoneDisablesThem(GameTestHelper helper) {
        var node=node(helper,new AspectList().add(Aspect.FIRE,100));
        helper.setBlock(NODE.below(),ModBlocks.NODE_STABILIZER.get());
        helper.runAtTickTime(55,()->{helper.assertTrue(node.getLock()==1,"Basic stabilizer did not lock node");helper.setBlock(NODE.below(),ModBlocks.ADVANCED_NODE_STABILIZER.get());});
        helper.runAtTickTime(110,()->{helper.assertTrue(node.getLock()==2,"Advanced stabilizer did not lock node");helper.setBlock(NODE.below().west(),Blocks.REDSTONE_BLOCK);});
        helper.runAtTickTime(165,()->{helper.assertTrue(node.getLock()==0,"Powered stabilizer stayed enabled");helper.setBlock(NODE.below().west(),Blocks.AIR);});
        helper.runAtTickTime(215,()->{helper.assertTrue(node.getLock()==2,"Stabilizer failed to re-enable");helper.succeed();});
    }
    @GameTest(template="test/platform",batch="node_device_full_conversion",timeoutTicks=1200)
    public static void realTickersConvertAndSupplyCv(GameTestHelper helper) {
        device(helper,true);
        var id=((NodeBlockEntity)helper.getLevel().getBlockEntity(helper.absolutePos(NODE))).getId();
        helper.runAtTickTime(1040,()->{
            var entity=helper.getLevel().getBlockEntity(helper.absolutePos(NODE));
            helper.assertTrue(entity instanceof EnergizedNodeBlockEntity,"Actual block ticker did not finish conversion after 1000 ticks");
            var energized=(EnergizedNodeBlockEntity)entity;
            helper.assertTrue(energized.getId().equals(id),"Conversion changed node identity");
            helper.assertTrue(energized.getAspects().getAmount(Aspect.AIR)==10 && energized.getAspects().getAmount(Aspect.FIRE)==8,"Incorrect CV output");
            int cv=VisNet.drainVis(helper.getLevel(),helper.absolutePos(NODE.east(3)),Aspect.AIR,3);
            helper.assertTrue(cv==3,"Converted node did not supply the real CV network");
            helper.succeed();
        });
    }
    public static final class ObservedPedestal extends WandPedestalBlockEntity {
        public final List<CompoundTag> sent=new ArrayList<>();
        public ObservedPedestal(BlockPos pos,BlockState state){super(pos,state);}
        @Override public void sync(){super.sync();if(getLevel()!=null)sent.add(getUpdateTag(getLevel().registryAccess()).copy());}
    }
    private static ObservedPedestal observed(GameTestHelper helper) {
        var pos=helper.absolutePos(new BlockPos(1,2,1));
        helper.getLevel().setBlockAndUpdate(pos,ModBlocks.WAND_RECHARGE_PEDESTAL.get().defaultBlockState());
        var pedestal=new ObservedPedestal(pos,helper.getLevel().getBlockState(pos));
        helper.getLevel().setBlockEntity(pedestal);
        node(helper,new AspectList().add(Aspect.FIRE,100));
        return pedestal;
    }
    @GameTest(template="test/platform",batch="node_device_charge_sync",timeoutTicks=100)
    public static void partialWandChargeSendsProgressToClient(GameTestHelper helper) {
        var pedestal=observed(helper);
        pedestal.setStack(Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false));
        helper.runAtTickTime(45,()->{
            var client=new WandPedestalBlockEntity(pedestal.getBlockPos(),pedestal.getBlockState());client.setLevel(helper.getLevel());
            client.loadWithComponents(pedestal.sent.getLast(),helper.getLevel().registryAccess());
            helper.assertTrue(VisStorage.getVis(pedestal.getStack(),Aspect.FIRE)>100,"Actual pedestal did not charge");
            helper.assertTrue(VisStorage.getVis(client.getStack(),Aspect.FIRE)>100,"Observing client is stuck at the first charge update");
            helper.succeed();
        });
    }
    @GameTest(template="test/platform",batch="node_device_blocked_pedestal")
    public static void blockedPedestalDoesNotSwallowWand(GameTestHelper helper) {
        var pos=helper.absolutePos(new BlockPos(1,2,1));
        helper.getLevel().setBlockAndUpdate(pos,ModBlocks.WAND_RECHARGE_PEDESTAL.get().defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(),Blocks.STONE.defaultBlockState());
        var player=TestSupport.mockServerPlayer(helper);
        var wand=Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false);
        player.setItemInHand(InteractionHand.MAIN_HAND,wand);
        WandPedestalBlock.interact(helper.getLevel(),pos,player,InteractionHand.MAIN_HAND,wand);
        helper.assertTrue(!wand.isEmpty() && ((WandPedestalBlockEntity)helper.getLevel().getBlockEntity(pos)).getStack().isEmpty(),"Wand inserted into a blocked/invisible pedestal slot");
        helper.succeed();
    }
    @GameTest(template="test/platform",batch="node_device_stale_beam",timeoutTicks=100)
    public static void removingWandClearsChargingState(GameTestHelper helper) {
        var pedestal=observed(helper);
        pedestal.setStack(Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false));
        helper.runAtTickTime(15,()->{
            helper.assertTrue(pedestal.isDraining(),"Charge never began");
            pedestal.removeStack();
            helper.assertTrue(!pedestal.isDraining(),"Empty pedestal retained active charging state");
            helper.succeed();
        });
    }
    @GameTest(template="test/platform",batch="node_device_compound",timeoutTicks=100)
    public static void compoundFocusUnlocksCompoundAspects(GameTestHelper helper) {
        var pos=helper.absolutePos(new BlockPos(1,2,1));
        helper.getLevel().setBlockAndUpdate(pos,ModBlocks.WAND_RECHARGE_PEDESTAL.get().defaultBlockState());
        var source=node(helper,new AspectList().add(Aspect.LIGHT,40));
        var pedestal=(WandPedestalBlockEntity)helper.getLevel().getBlockEntity(pos);
        pedestal.setStack(Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false));
        helper.runAtTickTime(20,()->{helper.assertTrue(VisStorage.getAllVis(pedestal.getStack()).visSize()==0,"Compound vis drained without focus");helper.getLevel().setBlockAndUpdate(pos.above(),ModBlocks.COMPOUND_RECHARGE_FOCUS.get().defaultBlockState());});
        helper.runAtTickTime(45,()->{helper.assertTrue(VisStorage.getAllVis(pedestal.getStack()).visSize()>0 && source.getAspects().getAmount(Aspect.LIGHT)<40,"Compound focus did not convert aspects into primal vis");helper.succeed();});
    }
    @GameTest(template="test/platform",batch="node_device_legacy_beam",timeoutTicks=60)
    public static void loadingLegacyStarvedPedestalClearsOldBeam(GameTestHelper helper) {
        var pedestal=observed(helper);
        pedestal.setStack(Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false));
        var saved=pedestal.getUpdateTag(helper.getLevel().registryAccess());saved.putBoolean("draining",true);
        helper.setBlock(NODE,Blocks.AIR);
        pedestal.loadWithComponents(saved,helper.getLevel().registryAccess());
        helper.runAtTickTime(5,()->{helper.assertTrue(!pedestal.isDraining(),"Reloaded empty network retained old beam");helper.succeed();});
    }
}
