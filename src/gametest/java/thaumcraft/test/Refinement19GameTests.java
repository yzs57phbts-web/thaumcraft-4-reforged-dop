package thaumcraft.test;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.registry.*;
import thaumcraft.modern.*;
import thaumcraft.crafting.*;
import thaumcraft.alchemy.CrucibleBlockEntity;
import thaumcraft.aspect.*;
import thaumcraft.wand.*;

@GameTestHolder("thaumcraft") @PrefixGameTestTemplate(false)
public final class Refinement19GameTests {
 @GameTest(template="test/platform",batch="refinement19_ores")
 public static void clustersAcceptRawOreAndOreBlocks(GameTestHelper h){
  for(String metal:List.of("Iron","Gold","Copper")){
   var recipe=(CrucibleRecipe)TcRecipes.get("Pure"+metal);
   var raw=BuiltInRegistries.ITEM.get(ResourceLocation.parse(metal.equals("Cinnabar")?"thaumcraft:raw_cinnabar":"minecraft:raw_"+metal.toLowerCase()));
   var ore=BuiltInRegistries.ITEM.get(ResourceLocation.parse((metal.equals("Cinnabar")?"thaumcraft:":"minecraft:")+metal.toLowerCase()+"_ore"));
   h.assertTrue(recipe.catalyst().test(new ItemStack(raw)),"raw input rejected: "+metal);
   h.assertTrue(recipe.catalyst().test(new ItemStack(ore)),"ore block rejected: "+metal);
   h.assertFalse(recipe.catalyst().test(new ItemStack(Items.COBBLESTONE)),"unrelated stone accepted: "+metal);
  }
  h.assertTrue(thaumcraft.research.ResearchCategories.getResearch("PURETIN")==null&&thaumcraft.research.ResearchCategories.getResearch("PURELEAD")==null&&thaumcraft.research.ResearchCategories.getResearch("PURESILVER")==null,"empty integration research visible");
  h.assertFalse(MetalIntegration.itemAvailable("native_tin_cluster"),"placeholder cluster visible");
  h.assertTrue(thaumcraft.research.ResearchCategories.getResearch("CAP_silver")==null,"silver cap research visible without silver");h.succeed();
 }
 @GameTest(template="test/platform",batch="refinement19_cinnabar")
 public static void cinnabarHasRawLootAndSilkTouchStillPreservesOre(GameTestHelper h){
  var pos=h.absolutePos(new BlockPos(2,2,2));var state=ModBlocks.CINNABAR_ORE.get().defaultBlockState();h.getLevel().setBlockAndUpdate(pos,state);
  var tool=new ItemStack(Items.IRON_PICKAXE);var drops=Block.getDrops(state,h.getLevel(),pos,null,null,tool);
  h.assertTrue(drops.size()==1&&drops.getFirst().is(ModItems.RAW_CINNABAR.get()),"normal mining did not drop raw cinnabar");
  tool.enchant(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);
  drops=Block.getDrops(state,h.getLevel(),pos,null,null,tool);h.assertTrue(drops.size()==1&&drops.getFirst().is(ModBlocks.CINNABAR_ORE.get().asItem()),"silk touch lost ore block");
  var input=new SingleRecipeInput(new ItemStack(ModItems.RAW_CINNABAR.get()));var smelt=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,h.getLevel()).orElseThrow();
  h.assertTrue(smelt.value().assemble(input,h.getLevel().registryAccess()).is(ModItems.QUICKSILVER.get()),"raw cinnabar cannot smelt");h.succeed();
 }
 @GameTest(template="test/platform",batch="refinement19_heat")
 public static void onlyLitCampfiresHeatCrucible(GameTestHelper h){
  for(int i=0;i<4;i++){
   var local=new BlockPos(1+i*2,3,2);h.setBlock(local,ModBlocks.CRUCIBLE.get());
   var pot=(CrucibleBlockEntity)h.getBlockEntity(local);pot.fillWater(1000);
   h.setBlock(local.below(),(i<2?Blocks.CAMPFIRE:Blocks.SOUL_CAMPFIRE).defaultBlockState().setValue(CampfireBlock.LIT,i%2==0));
   CrucibleBlockEntity.serverTick(h.getLevel(),h.absolutePos(local),pot);
   h.assertTrue((pot.getHeat()>0)==(i%2==0),"wrong campfire heat: "+i);
  }h.succeed();
 }
 @GameTest(template="test/platform",batch="refinement19_signs")
 public static void signsCraftStoreBothTextSidesWaxAndCorrectBlockEntityType(GameTestHelper h){
  for(String wood:List.of("greatwood","silverwood")){
   var plank=BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:"+wood+"_planks"));
   var input=CraftingInput.of(3,3,List.of(new ItemStack(plank),new ItemStack(plank),new ItemStack(plank),new ItemStack(plank),new ItemStack(plank),new ItemStack(plank),ItemStack.EMPTY,new ItemStack(Items.STICK),ItemStack.EMPTY));
   var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();var result=recipe.value().assemble(input,h.getLevel().registryAccess());
   h.assertTrue(result.getCount()==3&&BuiltInRegistries.ITEM.getKey(result.getItem()).getPath().equals(wood+"_sign"),"sign craft failed");
   for(String kind:List.of("sign","wall_sign")){
    var state=ModernContent.WOOD.get(wood+"_"+kind).get().defaultBlockState();var pos=h.absolutePos(new BlockPos(2,3,2));
    var sign=(SignBlockEntity)((EntityBlock)state.getBlock()).newBlockEntity(pos,state);
    sign.setLevel(h.getLevel());
    sign.setText(new SignText().setMessage(0,Component.literal("Front")).setColor(DyeColor.BLUE).setHasGlowingText(true),true);
    sign.setText(new SignText().setMessage(0,Component.literal("Back")),false);sign.setWaxed(true);
    var tag=sign.saveWithFullMetadata(h.getLevel().registryAccess());var loaded=(SignBlockEntity)BlockEntity.loadStatic(pos,state,tag,h.getLevel().registryAccess());
    h.assertTrue(loaded!=null&&loaded.getType()==ModernContent.SIGN.get()&&loaded.getFrontText().getMessage(0,false).getString().equals("Front")&&loaded.getBackText().getMessage(0,false).getString().equals("Back")&&loaded.getFrontText().hasGlowingText()&&loaded.isWaxed(),"sign data/type lost");
   }
  }h.succeed();
 }
 @GameTest(template="test/platform",batch="refinement19_wand")
 public static void chargeUpdatesKeepEquippedWandAndUseAndOffhandFocusWorks(GameTestHelper h){
  var p=TestSupport.mockServerPlayer(h);var before=Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false);Wands.fill(before);var after=before.copy();Wands.storeVis(after,Aspect.AIR,20);WandKeys.TARGET.set(after,new BlockPos(1,2,3));
  var item=(WandItem)before.getItem();h.assertFalse(item.shouldCauseReequipAnimation(before,after,false),"vis/target update lowers hand");h.assertTrue(item.canContinueUsing(before,after),"vis update interrupts use");h.assertTrue(item.shouldCauseReequipAnimation(before,after,true),"slot switch should animate");
  var other=Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_SILVERWOOD,false);h.assertTrue(item.shouldCauseReequipAnimation(before,other,false),"different wand ignored");h.assertFalse(item.canContinueUsing(before,other),"different wand continues use");
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));p.setItemInHand(InteractionHand.OFF_HAND,after);h.assertTrue(Wands.heldWand(p)==after&&Wands.focusWand(p)==after,"offhand resolver failed");
  h.succeed();
 }
}
