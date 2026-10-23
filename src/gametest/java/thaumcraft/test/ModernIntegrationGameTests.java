package thaumcraft.test;

import java.util.*;
import java.lang.reflect.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.crafting.*;
import thaumcraft.infusion.*;
import thaumcraft.registry.*;
import thaumcraft.research.*;
import thaumcraft.modern.*;
import thaumcraft.wand.*;
import thaumcraft.essentia.*;

@GameTestHolder("thaumcraft")
@PrefixGameTestTemplate(false)
public final class ModernIntegrationGameTests {
    @GameTest(template="test/platform",batch="modern_jar_labels")
    public static void jarLabelBindingPreservesEssentiaAndClearingRemovesAspect(GameTestHelper h){
        for(var aspect:thaumcraft.aspect.Aspect.getEnabledAspects()){
            var phial=TcStack.withAspects("thaumcraft:essentia_phial",1,new thaumcraft.aspect.AspectList().add(aspect,8)).create();
            var input=CraftingInput.of(2,1,List.of(new ItemStack(ModItems.JAR_LABEL.get()),phial));
            var holder=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
            var result=holder.value().assemble(input,h.getLevel().registryAccess());var tags=thaumcraft.aspect.AspectKeys.ASPECTS.get(result);
            h.assertTrue(tags!=null&&tags.getAspects().getFirst()==aspect&&tags.visSize()==0,"wrong label aspect");
            h.assertTrue(ItemStack.matches(phial,holder.value().getRemainingItems(input).get(1)),"label craft consumed phial/essentia");
            var clear=CraftingInput.of(1,1,List.of(result));var clearRecipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,clear,h.getLevel()).orElseThrow();
            var blank=clearRecipe.value().assemble(clear,h.getLevel().registryAccess());
            h.assertTrue(blank.is(ModItems.JAR_LABEL.get())&&!thaumcraft.aspect.AspectKeys.ASPECTS.has(blank),"label did not clear");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="modern_recipe_display")
    public static void infusionDiagramsHaveOneVisibleSlotPerIngredient(GameTestHelper h){
        for(var r:TcRecipes.all(InfusionRecipe.class)){
            var points=InfusionDiagram.coordinates(r.key(),r.components().size());
            h.assertValueEqual(points.size(),r.components().size(),"diagram count "+r.key());
            for(int i=0;i<points.size();i++){
                var a=points.get(i);h.assertTrue(Math.abs(a[0])>=16||Math.abs(a[1])>=16,"ingredient covers central item "+r.key());
                for(int j=0;j<i;j++){var b=points.get(j);h.assertTrue(Math.abs(a[0]-b[0])>=16||Math.abs(a[1]-b[1])>=16,"overlapping slots "+r.key());}
            }
        }
        h.succeed();
    }
    @GameTest(template="test/platform",batch="modern_frost_consumption")
    public static void frostCraftConsumesBucketWithoutReturningEmptyBucket(GameTestHelper h){
        var p=TestSupport.mockServerPlayer(h);ResearchManager.complete(p,"FOCUSFROST");
        var recipe=(ShapedArcaneRecipe)TcRecipes.get("FocusFrost");var table=new SimpleContainer(10);
        for(int i=0;i<9;i++)table.setItem(i,recipe.grid().get(i).displayStacks().getFirst().copy());
        var wand=Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false);Wands.fill(wand);table.setItem(9,wand);
        var result=new SimpleContainer(1);result.setItem(0,recipe.result().create());
        new thaumcraft.menu.SlotCraftingArcaneWorkbench(p,table,result,0,0,0).onTake(p,result.getItem(0));
        for(int i=0;i<9;i++)h.assertTrue(table.getItem(i).isEmpty(),"frost left ingredient/container at "+i);
        h.assertTrue(!p.getInventory().contains(new ItemStack(Items.BUCKET)),"empty snow bucket returned");h.succeed();
    }
    @GameTest(template="test/platform",batch="modern_candle_crafting")
    public static void candleStringWorksInAllSixFreeSlotsAndRequiresWax(GameTestHelper h){
        for(int pos:new int[]{0,2,3,5,6,8}){
            var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));
            grid.set(1,new ItemStack(ModItems.MAGIC_TALLOW.get()));grid.set(4,new ItemStack(ModItems.MAGIC_TALLOW.get()));grid.set(7,new ItemStack(Items.HONEYCOMB));grid.set(pos,new ItemStack(Items.STRING));
            var input=CraftingInput.of(3,3,grid);var match=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
            h.assertTrue(match.isPresent(),"string slot "+pos+" rejected");var out=match.get().value().assemble(input,h.getLevel().registryAccess());
            h.assertTrue(out.is(ModBlocks.CANDLES.get("white").get().asItem())&&out.getCount()==3,"candle output");
            grid.set(7,ItemStack.EMPTY);h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,CraftingInput.of(3,3,grid),h.getLevel()).isEmpty(),"old wax-free candle craft still works");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="modern_wood_crafting")
    public static void bothWoodFamiliesCraftAndTrapdoorsWorkInHungryChest(GameTestHelper h){
        var p=TestSupport.mockServerPlayer(h);ResearchManager.complete(p,"HUNGRYCHEST");
        var hungry=(ShapedArcaneRecipe)TcRecipes.get("HungryChest");
        for(String wood:List.of("greatwood","silverwood"))for(String kind:List.of("door","trapdoor","fence","fence_gate","boat")){
            var id=ResourceLocation.parse("thaumcraft:"+wood+"_"+kind);var holder=h.getLevel().getRecipeManager().byKey(id).orElseThrow();
            h.assertTrue(holder.value() instanceof ShapedRecipe,"wood recipe missing "+id);var r=(ShapedRecipe)holder.value();
            var grid=r.getIngredients().stream().map(i->i.isEmpty()?ItemStack.EMPTY:i.getItems()[0].copy()).toList();var input=CraftingInput.of(r.getWidth(),r.getHeight(),grid);
            h.assertTrue(r.matches(input,h.getLevel())&&r.assemble(input,h.getLevel().registryAccess()).is(BuiltInRegistries.ITEM.get(id)),"wood recipe cannot craft "+id);
            if(kind.equals("trapdoor")){
                var item=BuiltInRegistries.ITEM.get(id);h.assertTrue(new ItemStack(item).is(ItemTags.WOODEN_TRAPDOORS),"new trapdoor missing tag");
                var chest=new ArrayList<ItemStack>();for(var i:hungry.grid())chest.add(i instanceof TcIngredient.Tag t&&t.tag().equals(ItemTags.WOODEN_TRAPDOORS)?new ItemStack(item):i.displayStacks().isEmpty()?ItemStack.EMPTY:i.displayStacks().getFirst().copy());
                h.assertTrue(ArcaneCrafting.findResult(chest,p).is(ModBlocks.HUNGRY_CHEST.get().asItem()),"hungry chest rejects "+id);
            }
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="modern_boat_save")
    public static void woodBoatPreservesVariantAndDropsThroughSave(GameTestHelper h){
        for(String wood:List.of("greatwood","silverwood")){
            var boat=ModernContent.BOAT.get().create(h.getLevel());boat.wood(wood);var tag=new net.minecraft.nbt.CompoundTag();boat.save(tag);
            var loaded=(WoodBoat)net.minecraft.world.entity.EntityType.loadEntityRecursive(tag,h.getLevel(),e->e);
            h.assertTrue(loaded!=null&&loaded.silver()==wood.equals("silverwood")&&loaded.getDropItem()==ModernContent.BOATS.get(wood).get(),"boat variant/item lost");
            h.assertTrue(loaded.canBoatInFluid(Blocks.WATER.defaultBlockState().getFluidState())&&!loaded.canBoatInFluid(Blocks.LAVA.defaultBlockState().getFluidState()),"wood boat lava behavior");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="modern_floating_candles")
    public static void coreOwnsAllFloatingCandleColorsResearchAndStabilization(GameTestHelper h){
        h.assertTrue(ResearchCategories.getResearch("FLOATCANDLE")==null&&ResearchCategories.getResearch("MAGICALWOODWORK")==null,"redundant research icons remain");
        for(String color:ModBlocks.CANDLE_COLORS){
            var b=ModernContent.CANDLES.get(color).get();h.setBlock(new BlockPos(2,4,2),b);
            h.assertTrue(h.getBlockEntity(new BlockPos(2,4,2)) instanceof FloatingCandleEntity,"floating animation data absent");
            h.assertTrue(b.canStabiliseInfusion(h.getLevel(),h.absolutePos(new BlockPos(2,4,2)))&&b.defaultBlockState().getLightEmission()==14,"light/stabilization changed");
            h.assertTrue(b==ModBlocks.CANDLES.get(color).get(),"two available candle families");
            h.assertTrue(TcRecipes.get("FloatingCandle_"+color)==null,"separate conversion still registered");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="modern_templates",timeoutTicks=200)
    public static void fortressTemplatesSurviveActualInfusionAndMatrixReload(GameTestHelper h)throws Exception{
        var r=(InfusionRecipe)TcRecipes.get("ThaumiumFortressLegs");var template=(TcIngredient.Reusable)r.components().getLast();
        for(var option:template.displayStacks())runTemplate(h,r,option);
        h.succeed();
    }
    private static void runTemplate(GameTestHelper h,InfusionRecipe r,ItemStack template)throws Exception{
        var center=new BlockPos(3,2,3);var matrixPos=new BlockPos(3,4,3);
        BlockPos[] ring={new BlockPos(1,2,3),new BlockPos(5,2,3),new BlockPos(3,2,1),new BlockPos(3,2,5),new BlockPos(1,2,1),new BlockPos(5,2,5)};
        h.setBlock(center,ModBlocks.ARCANE_PEDESTAL.get());h.setBlock(matrixPos,ModBlocks.RUNIC_MATRIX.get());
        for(int x:new int[]{2,4})for(int z:new int[]{2,4}){h.setBlock(new BlockPos(x,2,z),ModBlocks.ARCANE_STONE_BRICKS.get());h.setBlock(new BlockPos(x,3,z),ModBlocks.ARCANE_STONE.get());}
        InfusionTriggers.replace(h.getLevel(),h.absolutePos(new BlockPos(2,2,2)));
        var p=TestSupport.mockServerPlayer(h);ResearchManager.complete(p,r.research());h.<PedestalBlockEntity>getBlockEntity(center).setStack(r.central().displayStacks().getFirst());
        for(int i=0;i<r.components().size();i++){h.setBlock(ring[i],ModBlocks.ARCANE_PEDESTAL.get());h.<PedestalBlockEntity>getBlockEntity(ring[i]).setStack(i==r.components().size()-1?template.copy():r.components().get(i).displayStacks().getFirst().copy());}
        int n=0;for(var a:r.aspects().getAspects()){var pos=new BlockPos(n++*3,5,0);h.setBlock(pos,ModBlocks.WARDED_JAR.get());h.<JarBlockEntity>getBlockEntity(pos).addToContainer(a,64);}
        var matrix=h.<InfusionMatrixBlockEntity>getBlockEntity(matrixPos);matrix.recalculateSurroundings();matrix.craftingStart(p);h.assertTrue(matrix.isCrafting(),"template recipe does not start");
        var field=InfusionMatrixBlockEntity.class.getDeclaredField("instability");field.setAccessible(true);field.setInt(matrix,-1);EssentiaHandler.refreshSources(matrix);
        for(int tick=0;tick<4000&&matrix.isCrafting();tick++){
            if(tick==350){var loaded=new InfusionMatrixBlockEntity(matrix.getBlockPos(),matrix.getBlockState());loaded.loadWithComponents(matrix.saveWithoutMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());h.getLevel().setBlockEntity(loaded);matrix=loaded;EssentiaHandler.refreshSources(matrix);}
            InfusionMatrixBlockEntity.tick(h.getLevel(),matrix.getBlockPos(),matrix.getBlockState(),matrix);
        }
        h.assertTrue(!matrix.isCrafting()&&r.result().matches(h.<PedestalBlockEntity>getBlockEntity(center).getStack()),"actual infusion or resume failed");
        h.assertTrue(ItemStack.isSameItemSameComponents(template,h.<PedestalBlockEntity>getBlockEntity(ring[5]).getStack()),"template consumed or changed");
        for(int i=0;i<5;i++)h.assertTrue(h.<PedestalBlockEntity>getBlockEntity(ring[i]).isEmpty(),"consumable retained");
    }
}
