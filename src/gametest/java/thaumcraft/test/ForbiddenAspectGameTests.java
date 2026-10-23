package thaumcraft.test;

import java.util.List;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.*;
import thaumcraft.essentia.*;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.*;

@GameTestHolder(Thaumcraft.MODID) @PrefixGameTestTemplate(false)
public final class ForbiddenAspectGameTests {
    private static final List<Aspect> DARK=List.of(Aspect.INFERNUS,Aspect.IRA,Aspect.INVIDIA,Aspect.GULA,Aspect.SUPERBIA,Aspect.DESIDIA,Aspect.LUXURIA);
    @GameTest(template="test/platform",batch="forbidden_aspect_definitions")
    public static void originalFormulasColorsAndLegacyIds(GameTestHelper h) {
        Aspect[][] components={{Aspect.FIRE,Aspect.MAGIC},{Aspect.WEAPON,Aspect.FIRE},{Aspect.SENSES,Aspect.HUNGER},{Aspect.HUNGER,Aspect.VOID},{Aspect.FLIGHT,Aspect.VOID},{Aspect.TRAP,Aspect.SOUL},{Aspect.FLESH,Aspect.HUNGER}};
        int[] colors={0xFF0000,0x870404,0x00BA00,0xD59C46,0x9639FF,0x6E6E6E,0xFFC1CE};
        int[] blends={771,771,1,1,1,771,1};
        h.assertValueEqual(Aspect.TRAP.ordinal(),47,"old aspect ordinals changed");h.assertValueEqual(Aspect.getPrimalAspects().size(),6,"new aspect became primal");
        for(int i=0;i<DARK.size();i++){
            Aspect a=DARK.get(i);h.assertTrue(Aspect.getAspect(a.getTag())==a,"tag lookup failed");
            h.assertTrue(!a.isPrimal()&&a.getColor()==colors[i]&&a.getBlend()==blends[i],"legacy definition mismatch "+a);
            Aspect expected=a.isEnabled()?a:null;h.assertTrue(Aspect.combine(components[i][0],components[i][1])==expected&&Aspect.combine(components[i][1],components[i][0])==expected,"research combination mismatch "+a);
            h.assertTrue(AspectHelper.reduceToPrimals(new AspectList().add(a,1)).size()>0,"cannot reduce to primals "+a);
            h.assertTrue(ForbiddenAspectGameTests.class.getResource("/assets/"+a.getImage().getNamespace()+"/"+a.getImage().getPath())!=null,"missing original aspect texture "+a);
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="forbidden_aspect_codec")
    public static void aspectListsRoundTripThroughSaveJsonAndNetwork(GameTestHelper h) {
        AspectList all=new AspectList();for(int i=0;i<DARK.size();i++)all.add(DARK.get(i),i+1);all.add(Aspect.FIRE,19);
        var nbt=AspectList.CODEC.encodeStart(NbtOps.INSTANCE,all).getOrThrow();h.assertTrue(AspectList.CODEC.parse(NbtOps.INSTANCE,nbt).getOrThrow().equals(all),"NBT lost new aspects");
        var json=AspectList.CODEC.encodeStart(JsonOps.INSTANCE,all).getOrThrow();h.assertTrue(AspectList.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(all),"JSON recipe aspects lost");
        var buffer=Unpooled.buffer();try{AspectList.STREAM_CODEC.encode(buffer,all);h.assertTrue(AspectList.STREAM_CODEC.decode(buffer).equals(all),"network lost new aspects");}finally{buffer.release();}
        h.succeed();
    }
    @GameTest(template="test/platform",batch="forbidden_aspect_knowledge")
    public static void researchDiscoveryAndPoolsSurviveSaveAndSync(GameTestHelper h) {
        AspectList legacy=new AspectList();for(Aspect a:DARK)legacy.add(a,9);
        var tag=new net.minecraft.nbt.CompoundTag();tag.put("aspects",AspectList.CODEC.encodeStart(NbtOps.INSTANCE,legacy).getOrThrow());
        PlayerKnowledge knowledge=PlayerKnowledge.CODEC.parse(NbtOps.INSTANCE,tag).getOrThrow();
        var saved=PlayerKnowledge.CODEC.encodeStart(NbtOps.INSTANCE,knowledge).getOrThrow();PlayerKnowledge restored=PlayerKnowledge.CODEC.parse(NbtOps.INSTANCE,saved).getOrThrow();
        var buffer=Unpooled.buffer();try{
            PlayerKnowledge.STREAM_CODEC.encode(buffer,restored);PlayerKnowledge client=PlayerKnowledge.STREAM_CODEC.decode(buffer);
            for(Aspect a:DARK){
                h.assertValueEqual(client.aspects().getAmount(a),9,"dormant saved knowledge was erased "+a);
                h.assertTrue(client.hasDiscoveredAspect(a)==a.isEnabled(),"inactive knowledge became visible "+a);
                h.assertValueEqual(client.getAspectPool(a),a.isEnabled()?9:0,"inactive knowledge became spendable "+a);
            }
        }finally{buffer.release();}h.succeed();
    }
    @GameTest(template="test/platform",batch="forbidden_aspect_research_table")
    public static void researchTableCombinesAllSevenUsingRealHandler(GameTestHelper h) {
        BlockPos pos=new BlockPos(3,2,3);h.setBlock(pos,ModBlocks.RESEARCH_TABLE.get());
        var player=TestSupport.mockServerPlayer(h);var absolute=h.absolutePos(pos);player.setPos(absolute.getX()+0.5,absolute.getY()+1,absolute.getZ()+0.5);
        player.containerMenu=new thaumcraft.menu.ResearchTableMenu(1,player.getInventory(),absolute);
        PlayerKnowledge knowledge=Knowledge.get(player);
        for(Aspect a:DARK){
            Aspect[] parts=a.getComponents();for(Aspect part:parts){knowledge.discoverAspect(part);knowledge.setAspectPool(part,2);}
            thaumcraft.blockentity.ResearchTableBlockEntity.handleCombine(new thaumcraft.network.AspectCombinePayload(absolute,parts[0],parts[1],false,false),player);
            h.assertTrue(knowledge.hasDiscoveredAspect(a)==a.isEnabled()&&knowledge.getAspectPool(a)==(a.isEnabled()?3:0),"research table activation mismatch "+a);
            for(Aspect part:parts)h.assertValueEqual(knowledge.getAspectPool(part),a.isEnabled()?1:2,"research combination spent wrong component amount");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="forbidden_aspect_items")
    public static void filledJarsKeepEveryNewAspectThroughItemPackets(GameTestHelper h) {
        BlockPos p=new BlockPos(3,2,3);h.setBlock(p,ModBlocks.WARDED_JAR.get());JarBlockEntity jar=h.getBlockEntity(p);
        for(Aspect a:DARK){
            jar.addToContainer(a,32);ItemStack drop=((JarBlock)jar.getBlockState().getBlock()).createDrop(jar);
            var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());try{
                ItemStack.STREAM_CODEC.encode(buffer,drop);ItemStack received=ItemStack.STREAM_CODEC.decode(buffer);
                h.assertValueEqual(AspectKeys.ASPECTS.get(received).getAmount(a),32,"jar item component lost "+a);
            }finally{buffer.release();}
            h.assertTrue(jar.takeFromContainer(a,32),"jar could not export "+a);
        }h.succeed();
    }
}
