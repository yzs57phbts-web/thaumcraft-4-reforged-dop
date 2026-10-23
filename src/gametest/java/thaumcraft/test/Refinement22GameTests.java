package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;import net.minecraft.core.registries.*;import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.*;import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;import net.neoforged.neoforge.gametest.*;
import thaumcraft.registry.*;import thaumcraft.block.*;import thaumcraft.world.gen.OreGeneration;

@GameTestHolder("thaumcraft") @PrefixGameTestTemplate(false)
public final class Refinement22GameTests {
 static Block ore(String id){return BuiltInRegistries.BLOCK.get(ResourceLocation.parse("thaumcraft:"+id));}
 @GameTest(template="test/platform",batch="refinement22_height")
 public static void deepOresFollowDepthAndVanillaTransitionHosts(GameTestHelper h){
  for(String family:DeepOreVariants.FAMILIES)for(int y:new int[]{-50,-1,0,4,12,64})for(Block host:List.of(Blocks.STONE,Blocks.DEEPSLATE,Blocks.TUFF)){
   var state=OreGeneration.withHostRock(ore(family).defaultBlockState(),host.defaultBlockState(),y);boolean deep=y<0||host==Blocks.DEEPSLATE;
   h.assertTrue(state.is(ore((deep?"deepslate_":"")+family)),"wrong ore variant at "+y+" in "+host);
  }h.succeed();
 }
 @GameTest(template="test/platform",batch="refinement22_loot")
 public static void sevenDeepOresPreserveDropsAndSilkTouchIdentity(GameTestHelper h){
  var pos=h.absolutePos(new BlockPos(2,2,2));var plain=new ItemStack(Items.DIAMOND_PICKAXE);var silk=plain.copy();silk.enchant(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);
  for(String family:DeepOreVariants.FAMILIES){
   var block=ore("deepslate_"+family);var state=block.defaultBlockState();h.assertTrue(block!=Blocks.AIR&&state.getValue(HostRockOreBlock.DEEPSLATE),"deep ore not registered");h.getLevel().setBlockAndUpdate(pos,state);
   var drops=Block.getDrops(state,h.getLevel(),pos,null,null,plain);String expected=family.equals("cinnabar_ore")?"raw_cinnabar":family.replace("_infused_stone","_shard");
   h.assertTrue(drops.size()==1&&drops.getFirst().is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:"+expected))),"wrong raw/shard drop "+family);
   drops=Block.getDrops(state,h.getLevel(),pos,null,null,silk);h.assertTrue(drops.size()==1&&drops.getFirst().is(block.asItem()),"silk touch lost deep ore identity");
   var legacy=ore(family).defaultBlockState().setValue(HostRockOreBlock.DEEPSLATE,true);drops=Block.getDrops(legacy,h.getLevel(),pos,null,null,silk);
   h.assertTrue(drops.size()==1&&drops.getFirst().is(block.asItem()),"old saved deep state silk-touch compatibility lost");
   h.assertTrue(legacy.getBlock().getCloneItemStack(h.getLevel(),pos,legacy).is(block.asItem()),"pick block lost legacy deep identity");
  }h.succeed();
 }
 @GameTest(template="test/platform",batch="refinement22_aspects")
 public static void deepOresKeepOriginalAspectsAndCrystalColors(GameTestHelper h){
  for(String family:DeepOreVariants.FAMILIES){var base=ore(family);var deep=ore("deepslate_"+family);
   var a=thaumcraft.aspect.AspectHelper.getObjectTags(new ItemStack(base));var b=thaumcraft.aspect.AspectHelper.getObjectTags(new ItemStack(deep));
   h.assertTrue(a!=null&&b!=null&&a.toString().equals(b.toString()),"deep ore aspects changed "+family);
   if(base instanceof InfusedStoneBlock first)h.assertTrue(((InfusedStoneBlock)deep).getColor()==first.getColor(),"deep crystal color differs");
  }h.succeed();
 }
 @GameTest(template="test/platform",batch="refinement22_smelting")
 public static void deepCinnabarSmeltsToQuicksilver(GameTestHelper h){
  var input=new SingleRecipeInput(new ItemStack(ModBlocks.DEEPSLATE_CINNABAR_ORE.get()));var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,h.getLevel()).orElseThrow();
  h.assertTrue(recipe.value().assemble(input,h.getLevel().registryAccess()).is(ModItems.QUICKSILVER.get()),"deep cinnabar cannot smelt");h.succeed();
 }
}
