package thaumcraft.test;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.*;
import thaumcraft.aspect.*;
import thaumcraft.crafting.CrucibleCrafting;
import thaumcraft.entity.golem.*;
import thaumcraft.essentia.*;
import thaumcraft.node.*;
import thaumcraft.registry.*;
import thaumcraft.vis.*;

@GameTestHolder(Thaumcraft.MODID) @PrefixGameTestTemplate(false)
public final class AlchemyNetworkAuditGameTests {
    private static final BlockPos P = new BlockPos(3,2,3);
    private static EnergizedNodeBlockEntity source(GameTestHelper h, BlockPos pos) {
        h.setBlock(pos,ModBlocks.ENERGIZED_AURA_NODE.get());
        EnergizedNodeBlockEntity n=h.getBlockEntity(pos);
        n.setAspects(new AspectList().add(Aspect.FIRE,2500).add(Aspect.WATER,2500).add(Aspect.ENTROPY,2500));
        n.setupNode();
        EnergizedNodeBlockEntity.serverTick(h.getLevel(),n.getBlockPos(),n.getBlockState(),n);
        return n;
    }
    @GameTest(template="test/platform",timeoutTicks=600,batch="alchemy_audit_pipeline")
    public static void furnaceAlembicAndJarConserveEssentia(GameTestHelper h) {
        h.setBlock(P,ModBlocks.ALCHEMICAL_FURNACE.get());
        h.setBlock(P.above(),ModBlocks.ALEMBIC.get());
        h.setBlock(P.above().east(),ModBlocks.ESSENTIA_TUBE.get());
        h.setBlock(P.east(),ModBlocks.WARDED_JAR.get());
        AlchemicalFurnaceBlockEntity f=h.getBlockEntity(P); AlembicBlockEntity a=h.getBlockEntity(P.above()); JarBlockEntity j=h.getBlockEntity(P.east());
        ItemStack input=new ItemStack(Items.STONE); AspectList expected=CrucibleCrafting.dissolveTags(input);
        h.assertTrue(expected.size()==1,"single-aspect test ingredient changed: "+expected);
        f.setItem(0,input);f.setItem(1,new ItemStack(Items.COAL));
        h.succeedWhen(()->{
            h.assertTrue(f.getItem(0).isEmpty() && j.getAmount()==expected.visSize(),"furnace/alembic/tube/jar pipeline stalled");
            h.assertTrue(f.getVis()==0 && a.getAmount()==0,"essentia duplicated in processing chain");
            h.assertTrue(j.getAspect()==expected.getAspects().getFirst(),"aspect changed in processing chain");
        });
    }
    @GameTest(template="test/platform",batch="alchemy_audit_stack")
    public static void fiveAlembicsSeparateFiveAspects(GameTestHelper h) {
        h.setBlock(P,ModBlocks.ALCHEMICAL_FURNACE.get());AlchemicalFurnaceBlockEntity f=h.getBlockEntity(P);
        List<Aspect> aspects=List.of(Aspect.AIR,Aspect.FIRE,Aspect.EARTH,Aspect.WATER,Aspect.ORDER);
        for(int i=0;i<5;i++) { h.setBlock(P.above(i+1),ModBlocks.ALEMBIC.get()); AlembicBlockEntity a=h.getBlockEntity(P.above(i+1));a.setAspectFilter(aspects.get(i)); f.getAspects().add(aspects.get(i),2); }
        for(int i=0;i<240;i++)AlchemicalFurnaceBlockEntity.serverTick(h.getLevel(),f.getBlockPos(),f.getBlockState(),f);
        for(int i=0;i<5;i++){AlembicBlockEntity a=h.getBlockEntity(P.above(i+1));h.assertTrue(a.getAspect()==aspects.get(i)&&a.getAmount()==2,"stacked alembic lost or mixed filtered essentia");}
        h.assertTrue(f.getVis()==0,"furnace did not feed the whole stack");h.succeed();
    }
    @GameTest(template="test/platform",batch="alchemy_audit_jar_query")
    public static void jarReportsItsActualAspectAmount(GameTestHelper h) {
        h.setBlock(P,ModBlocks.WARDED_JAR.get());JarBlockEntity j=h.getBlockEntity(P);j.addToContainer(Aspect.FIRE,17);
        h.assertValueEqual(j.containerContains(Aspect.FIRE),17,"jar aspect query");h.assertValueEqual(j.containerContains(Aspect.WATER),0,"wrong aspect query");h.succeed();
    }
    @GameTest(template="test/platform",batch="alchemy_audit_jar_filter")
    public static void labelsRejectWrongAspectOnDirectInsertion(GameTestHelper h) {
        for(var block:List.of(ModBlocks.WARDED_JAR.get(),ModBlocks.VOID_JAR.get())){
            h.setBlock(P,block);JarBlockEntity j=h.getBlockEntity(P);j.setAspectFilter(Aspect.FIRE);
            h.assertValueEqual(j.addToContainer(Aspect.WATER,4),4,"labelled jar accepted wrong essentia");
            h.assertValueEqual(j.getAmount(),0,"wrong aspect polluted labelled jar");
            h.assertValueEqual(j.addToContainer(Aspect.FIRE,70),j.isVoid()?0:6,"capacity/void overflow changed");
            h.assertValueEqual(j.getAmount(),64,"jar capacity");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="alchemy_audit_jar_save")
    public static void jarDropPlacementAndPacketKeepContents(GameTestHelper h) {
        h.setBlock(P,ModBlocks.WARDED_JAR.get());JarBlockEntity j=h.getBlockEntity(P);j.setAspectFilter(Aspect.FIRE);j.addToContainer(Aspect.FIRE,37);
        ItemStack drop=((JarBlock)j.getBlockState().getBlock()).createDrop(j);
        h.assertValueEqual(AspectKeys.ASPECTS.get(drop).getAmount(Aspect.FIRE),37,"jar drop lost contents");
        h.assertTrue(EssentiaKeys.FILTER.get(drop)==Aspect.FIRE,"jar drop lost label");
        JarBlockEntity copy=new JarBlockEntity(j.getBlockPos(),j.getBlockState());copy.loadWithComponents(j.getUpdateTag(h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(copy.getAmount()==37&&copy.getAspectFilter()==Aspect.FIRE&&copy.getAspect()==Aspect.FIRE,"jar update/save lost contents");h.succeed();
    }
    @GameTest(template="test/platform",batch="alchemy_audit_reservoir")
    public static void reservoirPreservesMixedContentsAndCapacity(GameTestHelper h) {
        h.setBlock(P,ModBlocks.ESSENTIA_RESERVOIR.get());EssentiaReservoirBlockEntity r=h.getBlockEntity(P);
        h.assertValueEqual(r.addToContainer(Aspect.FIRE,150),0,"first reservoir add");h.assertValueEqual(r.addToContainer(Aspect.WATER,150),44,"reservoir overflow");
        h.assertValueEqual(r.getAspects().visSize(),256,"reservoir capacity");h.assertTrue(r.takeFromContainer(Aspect.WATER,100),"reservoir refused stored aspect");
        EssentiaReservoirBlockEntity copy=new EssentiaReservoirBlockEntity(r.getBlockPos(),r.getBlockState());copy.loadWithComponents(r.getUpdateTag(h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertValueEqual(copy.containerContains(Aspect.FIRE),150,"reservoir saved fire");h.assertValueEqual(copy.containerContains(Aspect.WATER),6,"reservoir saved water");h.succeed();
    }
    @GameTest(template="test/platform",timeoutTicks=160,batch="alchemy_audit_reservoir_input")
    public static void reservoirPullsThroughItsConfiguredFace(GameTestHelper h) {
        h.setBlock(P,ModBlocks.ALEMBIC.get());AlembicBlockEntity a=h.getBlockEntity(P);a.addToContainer(Aspect.FIRE,10);
        h.setBlock(P.east(),ModBlocks.ESSENTIA_RESERVOIR.get().defaultBlockState().setValue(EssentiaReservoirBlock.FACING,Direction.WEST));
        EssentiaReservoirBlockEntity r=h.getBlockEntity(P.east());
        h.succeedWhen(()->{h.assertValueEqual(r.containerContains(Aspect.FIRE),10,"reservoir did not pull from alembic");h.assertValueEqual(a.getAmount(),0,"reservoir duplicated source");});
    }
    @GameTest(template="test/platform",timeoutTicks=180,batch="alchemy_audit_reservoir_output")
    public static void reservoirExportsSelectedAspectIntoLabelledJar(GameTestHelper h) {
        h.setBlock(P,ModBlocks.ESSENTIA_RESERVOIR.get().defaultBlockState().setValue(EssentiaReservoirBlock.FACING,Direction.EAST));
        EssentiaReservoirBlockEntity r=h.getBlockEntity(P);r.addToContainer(Aspect.FIRE,10);r.addToContainer(Aspect.WATER,7);
        h.setBlock(P.east(),ModBlocks.ESSENTIA_TUBE.get());h.setBlock(P.east().below(),ModBlocks.WARDED_JAR.get());JarBlockEntity j=h.getBlockEntity(P.east().below());j.setAspectFilter(Aspect.FIRE);
        h.succeedWhen(()->{h.assertTrue(j.getAspect()==Aspect.FIRE&&j.getAmount()==10,"reservoir/tube/labelled jar output stalled");h.assertTrue(r.containerContains(Aspect.FIRE)==0&&r.containerContains(Aspect.WATER)==7,"reservoir mixed or lost remaining aspect");});
    }
    @GameTest(template="test/platform",batch="alchemy_audit_source_cache")
    public static void removedSourceDoesNotHideRemainingJars(GameTestHelper h) {
        h.setBlock(P,ModBlocks.ALEMBIC.get());AlembicBlockEntity consumer=h.getBlockEntity(P);
        h.setBlock(P.west(),ModBlocks.WARDED_JAR.get());h.setBlock(P.east(),ModBlocks.WARDED_JAR.get());
        JarBlockEntity first=h.getBlockEntity(P.west());JarBlockEntity second=h.getBlockEntity(P.east());first.addToContainer(Aspect.FIRE,1);second.addToContainer(Aspect.FIRE,2);
        h.assertTrue(EssentiaHandler.drainEssentia(consumer,Aspect.FIRE,null,3),"initial source search failed");
        h.setBlock(P.west(),Blocks.AIR);
        h.assertTrue(EssentiaHandler.drainEssentia(consumer,Aspect.FIRE,null,3),"removed cached jar hid remaining essentia");
        h.assertValueEqual(second.getAmount(),1,"remaining jar was not drained");h.succeed();
    }
    private static final class FurnaceProbe extends AdvancedAlchemicalFurnaceBlockEntity {
        int dirty; FurnaceProbe(BlockPos p,BlockState s){super(p,s);}
        @Override public void setChanged(){dirty++;super.setChanged();}
    }
    @GameTest(template="test/platform",batch="alchemy_audit_power_save")
    public static void advancedFurnacePowerChangesMarkSaveDirty(GameTestHelper h) {
        source(h,P.west(3));h.setBlock(P,ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get());
        FurnaceProbe f=new FurnaceProbe(h.absolutePos(P),h.getBlockState(P));h.getLevel().setBlockEntity(f);
        CompoundTag data=new CompoundTag();data.putInt("heat",500);f.loadWithComponents(data,h.getLevel().registryAccess());f.dirty=0;
        for(int i=0;i<5;i++)AdvancedAlchemicalFurnaceBlockEntity.serverTick(h.getLevel(),f.getBlockPos(),f.getBlockState(),f);
        h.assertTrue(f.getPower1()>0&&f.getPower2()>0,"advanced furnace did not receive CV");
        h.assertTrue(f.dirty>0,"power changed without marking chunk for saving");h.succeed();
    }
    @GameTest(template="test/platform",batch="alchemy_audit_advanced")
    public static void advancedFurnaceConsumesCvAndExportsThroughNozzle(GameTestHelper h) {
        EnergizedNodeBlockEntity n=source(h,P.west(3));h.setBlock(P,ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get());
        h.setBlock(P.east(),ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get().defaultBlockState().setValue(AdvancedAlchemicalFurnaceBlock.PART,AdvancedAlchemicalFurnaceBlock.Part.NOZZLE));
        AdvancedAlchemicalFurnaceBlockEntity f=h.getBlockEntity(P);AdvancedAlchemicalFurnaceNozzleBlockEntity nozzle=h.getBlockEntity(P.east());
        for(int i=0;i<50;i++){EnergizedNodeBlockEntity.serverTick(h.getLevel(),n.getBlockPos(),n.getBlockState(),n);AdvancedAlchemicalFurnaceBlockEntity.serverTick(h.getLevel(),f.getBlockPos(),f.getBlockState(),f);}
        ItemStack input=new ItemStack(Items.COBBLESTONE);AspectList expected=CrucibleCrafting.dissolveTags(input);int heat=f.getHeat(),p1=f.getPower1(),p2=f.getPower2();
        h.assertTrue(f.process(input),"powered advanced furnace refused item");h.assertTrue(f.getHeat()==heat-expected.visSize()*2&&f.getPower1()==p1-expected.visSize()&&f.getPower2()==p2-expected.visSize(),"advanced furnace CV cost changed");
        h.assertTrue(!f.process(input),"advanced furnace ignored processing delay");
        for(Aspect a:expected.getAspects()){int amount=expected.getAmount(a);h.assertValueEqual(nozzle.takeEssentia(a,amount,Direction.EAST),amount,"nozzle could not export");}
        h.assertValueEqual(f.getVis(),0,"nozzle duplicated or lost essentia");h.succeed();
    }
    private static BlockPos formAdvancedFurnace(GameTestHelper h) {
        BlockPos core=new BlockPos(10,2,10);h.setBlock(core,ModBlocks.ALCHEMICAL_FURNACE.get());
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0){
            h.setBlock(core.offset(x,0,z),ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
            h.setBlock(core.offset(x,1,z),x!=0&&z!=0?ModBlocks.ALEMBIC.get():ModBlocks.ALCHEMICAL_CONSTRUCT.get());
        }
        var player=TestSupport.mockServerPlayer(h);thaumcraft.research.Knowledge.get(player).addResearch("ADVALCHEMYFURNACE");
        ItemStack wand=thaumcraft.wand.Wands.create(thaumcraft.wand.WandRegistry.CAP_GOLD,thaumcraft.wand.WandRegistry.ROD_GREATWOOD,false);thaumcraft.wand.Wands.fill(wand);
        h.assertTrue(thaumcraft.wand.WandTriggers.perform(h.getLevel(),wand,player,h.absolutePos(core.east()),Direction.UP),"advanced furnace wand trigger failed");
        h.assertTrue(h.getLevel().getBlockEntity(h.absolutePos(core)) instanceof AdvancedAlchemicalFurnaceBlockEntity,"multiblock core not formed");
        for(Direction d:Direction.Plane.HORIZONTAL)h.assertTrue(h.getLevel().getBlockEntity(h.absolutePos(core.relative(d))) instanceof AdvancedAlchemicalFurnaceNozzleBlockEntity,"multiblock nozzle missing");
        return core;
    }
    @GameTest(template="test/eldritch_regression",timeoutTicks=200,batch="alchemy_audit_multiblock_collision")
    public static void formedAdvancedFurnaceProcessesAnActualDroppedItem(GameTestHelper h) {
        BlockPos core=formAdvancedFurnace(h);source(h,core.west(6));BlockPos absolute=h.absolutePos(core);
        var item=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),absolute.getX()+0.5,absolute.getY()+2.0,absolute.getZ()+0.5,new ItemStack(Items.STONE));
        item.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);h.getLevel().addFreshEntity(item);
        h.succeedWhen(()->{
            AdvancedAlchemicalFurnaceBlockEntity f=h.getBlockEntity(core);
            h.assertTrue(item.isRemoved(),"dropped item did not enter the powered formed furnace: item="+item.position()+" heat="+f.getHeat()+" entropy="+f.getPower1()+" water="+f.getPower2()+" vis="+f.getVis());
            h.assertTrue(f.getAspects().equals(CrucibleCrafting.dissolveTags(new ItemStack(Items.STONE))),"physical furnace input lost or duplicated aspects");
        });
    }
    @GameTest(template="test/eldritch_regression",batch="alchemy_audit_multiblock_collapse")
    public static void brokenNozzleRevertsAdvancedFurnaceToComponents(GameTestHelper h) {
        BlockPos core=formAdvancedFurnace(h);h.setBlock(core.east(),Blocks.AIR);
        h.assertBlockPresent(ModBlocks.ALCHEMICAL_FURNACE.get(),core);
        h.assertBlockPresent(ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get(),core.west());
        h.assertBlockPresent(ModBlocks.ALEMBIC.get(),core.offset(-1,1,-1));h.succeed();
    }
    @GameTest(template="test/eldritch_regression",timeoutTicks=180,batch="alchemy_audit_vis_reconnect")
    public static void relayDisconnectsAndReconnectsAroundObstruction(GameTestHelper h) {
        BlockPos s=new BlockPos(3,3,3),r=s.east(6);source(h,s);h.setBlock(r,ModBlocks.VIS_RELAY.get());VisRelayBlockEntity relay=h.getBlockEntity(r);
        h.runAtTickTime(50,()->{h.assertTrue(relay.getParentPos()!=null,"relay did not connect");h.assertTrue(VisNet.drainVis(h.getLevel(),h.absolutePos(r.east(5)),Aspect.FIRE,3)>0,"relay supplied no CV");h.setBlock(s.east(3),Blocks.STONE);});
        h.runAtTickTime(100,()->{h.assertTrue(relay.getParentPos()==null,"beam/network crossed solid obstruction");h.setBlock(s.east(3),Blocks.AIR);});
        h.runAtTickTime(160,()->{h.assertTrue(relay.getParentPos()!=null,"relay did not reconnect");h.succeed();});
    }
    @GameTest(template="test/eldritch_regression",batch="alchemy_audit_golem_essentia")
    public static void alchemyGolemTakesFromReservoirAndKeepsOverflow(GameTestHelper h) {
        BlockPos home=h.absolutePos(new BlockPos(8,2,8));h.getLevel().setBlockAndUpdate(home,ModBlocks.ESSENTIA_RESERVOIR.get().defaultBlockState());
        EssentiaReservoirBlockEntity r=(EssentiaReservoirBlockEntity)h.getLevel().getBlockEntity(home);r.addToContainer(Aspect.FIRE,16);
        GolemBase g=new GolemBase(ModEntities.GOLEM.get(),h.getLevel());g.setPos(home.getX()+0.5,home.getY()+1,home.getZ()+0.5);g.setHome(home.above(),Direction.UP);
        g.configure(GolemType.IRON,new GolemSnapshot(6,false,List.of(),"",List.of(),ItemContainerContents.EMPTY,List.of(),0));
        GoalSelector goals=new GoalSelector(h.getLevel()::getProfiler);LogisticsGoals.install(g,goals);goals.tick();
        h.assertTrue(g.getEssentia()==Aspect.FIRE&&g.getEssentiaAmount()>0,"alchemy golem did not gather from reservoir");
        h.assertValueEqual(r.containerContains(Aspect.FIRE)+g.getEssentiaAmount(),16,"golem gathering lost essentia");
        BlockPos jar=home.above().east();h.getLevel().setBlockAndUpdate(jar,ModBlocks.WARDED_JAR.get().defaultBlockState());JarBlockEntity j=(JarBlockEntity)h.getLevel().getBlockEntity(jar);j.setAspectFilter(Aspect.FIRE);j.addToContainer(Aspect.FIRE,63);
        g.setMarkers(List.of(new Marker(GlobalPos.of(h.getLevel().dimension(),jar),Direction.UP,-1)));g.getNavigation().stop();GoalSelector deposit=new GoalSelector(h.getLevel()::getProfiler);LogisticsGoals.install(g,deposit);int carried=g.getEssentiaAmount();deposit.tick();
        h.assertValueEqual(j.getAmount(),64,"golem did not fill marked jar");h.assertValueEqual(g.getEssentiaAmount(),carried-1,"golem lost overflow");h.succeed();
    }
}
