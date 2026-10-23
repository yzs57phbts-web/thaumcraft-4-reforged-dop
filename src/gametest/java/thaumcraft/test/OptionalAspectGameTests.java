package thaumcraft.test;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.*;
import thaumcraft.item.*;
import thaumcraft.node.*;
import thaumcraft.registry.*;
import thaumcraft.research.*;

@GameTestHolder(Thaumcraft.MODID) @PrefixGameTestTemplate(false)
public final class OptionalAspectGameTests {
    @GameTest(template="test/platform",batch="optional_aspects_mode")
    public static void realModPresenceControlsAllSeven(GameTestHelper h) {
        boolean loaded=ModList.get().isLoaded("forbiddenmagic");
        h.assertValueEqual(Aspect.getEnabledAspects().length,loaded?55:48,"active aspect count");
        h.assertValueEqual(Aspect.values().length,55,"stable saved aspect definitions removed");
        for(Aspect a:Aspect.values())h.assertTrue(a.isEnabled()==(a.ordinal()<=47||loaded),"wrong aspect owner "+a);
        h.assertTrue(Aspect.getCompoundAspects().stream().allMatch(Aspect::isEnabled),"inactive aspect entered compound list");
        System.out.println("OPTIONAL_ASPECT_MODE forbiddenmagic="+loaded+" active="+Aspect.getEnabledAspects().length+" stable_ids="+Aspect.values().length);h.succeed();
    }
    @GameTest(template="test/platform",batch="optional_aspects_creative")
    public static void creativeVariantsContainOnlyEnabledAspects(GameTestHelper h) {
        var tab=ModCreativeTabs.THAUMCRAFT.get();tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS,true,h.getLevel().registryAccess()));
        int essence=0;
        for(ItemStack stack:tab.getDisplayItems()){
            AspectList tags=AspectKeys.ASPECTS.get(stack);if(tags!=null)for(Aspect a:tags.getAspects())h.assertTrue(a.isEnabled(),"inactive creative item "+a);
            if(stack.is(ModItems.WISP_ESSENCE.get()))essence++;
        }
        h.assertValueEqual(essence,Aspect.getEnabledAspects().length,"creative variants not gated");h.succeed();
    }
    @GameTest(template="test/platform",batch="optional_aspects_generation")
    public static void generatedNodesBeansAndCrystalsUseOnlyEnabledAspects(GameTestHelper h) {
        var player=TestSupport.mockServerPlayer(h);BlockPos pos=h.absolutePos(new BlockPos(3,4,3));
        for(int i=0;i<96;i++){
            NodeGenerator.createRandomNodeAt(h.getLevel(),pos,new java.util.Random(741L+i),false,false,false);
            var node=(NodeBlockEntity)h.getLevel().getBlockEntity(pos);
            h.assertTrue(node!=null&&node.getAspects().getAspects().stream().allMatch(Aspect::isEnabled),"inactive aspect in random node");
            ItemStack bean=new ItemStack(ModBlocks.MANA_BEAN.get());bean.getItem().inventoryTick(bean,h.getLevel(),player,0,false);
            h.assertTrue(ManaBeanItem.getAspect(bean).isEnabled(),"inactive random mana bean");
            ItemStack crystal=new ItemStack(ModItems.CRYSTALLIZED_ESSENCE.get());crystal.getItem().inventoryTick(crystal,h.getLevel(),player,1,false);
            h.assertTrue(AspectKeys.ASPECTS.get(crystal).getAspects().stream().allMatch(Aspect::isEnabled),"inactive random crystal");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="optional_aspects_awards")
    public static void dormantAspectsCannotBeGrantedOrLearned(GameTestHelper h) {
        var player=TestSupport.mockServerPlayer(h);PlayerKnowledge k=Knowledge.get(player);
        for(Aspect a:List.of(Aspect.INFERNUS,Aspect.IRA,Aspect.INVIDIA,Aspect.GULA,Aspect.SUPERBIA,Aspect.DESIDIA,Aspect.LUXURIA)){
            int awarded=ResearchManager.awardAspect(player,a,1);
            h.assertTrue((awarded>0)==a.isEnabled(),"inactive aspect award/toast was accepted");
            h.assertTrue(k.hasDiscoveredAspect(a)==a.isEnabled(),"inactive aspect learned");
            h.assertTrue(k.visibleAspects().getAspects().contains(a)==a.isEnabled(),"inactive aspect listed in research UI");
        }h.succeed();
    }
}
