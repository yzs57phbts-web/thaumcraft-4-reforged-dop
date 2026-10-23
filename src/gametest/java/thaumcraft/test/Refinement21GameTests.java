package thaumcraft.test;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.*;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.crafting.*;
import thaumcraft.registry.*;

@GameTestHolder("thaumcraft") @PrefixGameTestTemplate(false)
public final class Refinement21GameTests {
 @GameTest(template="test/platform",batch="refinement21_gold")
 public static void netherGoldIsRejectedAndAbsentFromBookInputs(GameTestHelper h){
  var r=(CrucibleRecipe)TcRecipes.get("PureGold");
  for(Item item:java.util.List.of(Items.RAW_GOLD,Items.GOLD_ORE,Items.DEEPSLATE_GOLD_ORE))h.assertTrue(r.catalyst().test(new ItemStack(item)),"valid gold input rejected");
  h.assertFalse(r.catalyst().test(new ItemStack(Items.NETHER_GOLD_ORE)),"nether gold can produce a cluster");
  h.assertTrue(r.catalyst().displayStacks().stream().noneMatch(s->s.is(Items.NETHER_GOLD_ORE)),"nether gold icon remains");h.succeed();
 }
 @GameTest(template="test/platform",batch="refinement21_cinnabar")
 public static void baseCinnabarClustersComeFromMiningAndPechWithoutCrucibleRecipe(GameTestHelper h){
  h.assertTrue(TcRecipes.get("PureCinnabar")==null,"base cinnabar recipe remains");
  var research=thaumcraft.research.ResearchCategories.getResearch("PUREIRON");
  h.assertTrue(research.pages().stream().noneMatch(p->p instanceof thaumcraft.research.ResearchPage.Crucible c&&c.recipes().contains("PureCinnabar")),"base book still links cinnabar cluster");
  h.assertTrue(thaumcraft.modern.MetalIntegration.itemAvailable("native_cinnabar_cluster"),"cluster hidden without WG");
  var drops=thaumcraft.menu.PechTradeMenu.tradeOffers(thaumcraft.entity.monster.Pech.PechType.NORMAL,1,h.getLevel().registryAccess());
  h.assertTrue(drops.stream().anyMatch(s->s.is(ModItems.NATIVE_CINNABAR_CLUSTER.get())),"Pech cannot offer cinnabar cluster");
  for(var ore:java.util.List.of(ModBlocks.CINNABAR_ORE.get(),ModBlocks.DEEPSLATE_CINNABAR_ORE.get())){
   var raw=new ItemStack(ModItems.RAW_CINNABAR.get(),3);
   var mined=thaumcraft.item.tool.SpecialMining.find(ore.defaultBlockState(),raw,1,RandomSource.create(0));
   h.assertTrue(mined.is(ModItems.NATIVE_CINNABAR_CLUSTER.get())&&mined.getCount()==3,"special mining lost cluster or count");
   h.assertTrue(thaumcraft.item.tool.SpecialMining.find(ore.defaultBlockState(),raw,0,RandomSource.create(0))==raw,"zero-chance mining replaced normal drop");
  }
  var input=new SingleRecipeInput(new ItemStack(ModItems.NATIVE_CINNABAR_CLUSTER.get()));
  var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,h.getLevel()).orElseThrow();
  var result=recipe.value().assemble(input,h.getLevel().registryAccess());
  h.assertTrue(result.is(ModItems.QUICKSILVER.get())&&result.getCount()==2,"cluster must smelt into two quicksilver without WG");h.succeed();
 }
}
