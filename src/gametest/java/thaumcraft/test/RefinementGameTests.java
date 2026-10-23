package thaumcraft.test;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.crafting.*;
import thaumcraft.infusion.PedestalBlockEntity;
import thaumcraft.modern.*;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.ResearchCategories;

@GameTestHolder("thaumcraft")
@PrefixGameTestTemplate(false)
public final class RefinementGameTests {
    @GameTest(template="test/platform",batch="refinement_recipe_rings")
    public static void selectedRecipesUseCompleteRegularRings(GameTestHelper h){
        var keys=new ArrayList<>(List.of("ThaumiumFortressLegs","ThaumiumFortressChest","ThaumiumFortressHelm","MaskAngryGhost","MaskSippingFiend","VoidRobeChest","EldritchEye","SanityCheck","ArcaneBore","LampGrowth","LampFertility","JarBrain","RunicRing","HoverHarness"));
        for(int i=0;i<5;i++)keys.add("ConfigResearch.RunicAugment"+i);
        for(String key:keys){var r=(InfusionRecipe)TcRecipes.get(key);var coords=InfusionDiagram.coordinates(key,r.components().size());
            for(var point:coords)h.assertTrue(Math.abs(Math.hypot(point[0],point[1])-40)<.6,"ingredient inside ring: "+key);
            h.assertValueEqual(coords.stream().map(p->p[0]+","+p[1]).distinct().count(),(long)r.components().size(),"duplicated ring slot");
        }
        var brain=(InfusionRecipe)TcRecipes.get("JarBrain");var p=InfusionDiagram.coordinates(brain.key(),6);h.assertTrue(p.get(1)[0]==-p.get(3)[0]&&p.get(1)[1]==p.get(3)[1]&&p.get(4)[0]==-p.get(5)[0]&&p.get(4)[1]==p.get(5)[1],"eyes/honey are not paired horizontally");
        h.succeed();
    }
    @GameTest(template="test/platform",batch="refinement_materials")
    public static void revisedMaterialsAndRunicPairsMatchRequest(GameTestHelper h){
        var chest=(InfusionRecipe)TcRecipes.get("VoidRobeChest");h.assertValueEqual(chest.components().stream().filter(i->i.test(new ItemStack(Items.PHANTOM_MEMBRANE))).count(),1L,"extra membrane");
        h.assertValueEqual(chest.components().stream().filter(i->i instanceof TcIngredient.Reusable).count(),1L,"template lost");
        var harness=(InfusionRecipe)TcRecipes.get("HoverHarness");h.assertTrue(harness.components().stream().noneMatch(i->i.test(new ItemStack(ModBlocks.GREATWOOD_PLANKS.get()))),"harness still needs greatwood planks");h.assertValueEqual(harness.components().size(),8,"harness count");
        for(int level=0;level<5;level++){var r=(InfusionRecipe)TcRecipes.get("ConfigResearch.RunicAugment"+level);var p=InfusionDiagram.coordinates(r.key(),r.components().size());h.assertTrue(p.get(0)[0]==-p.get(1)[0]&&p.get(0)[1]==p.get(1)[1],"runic shields not opposed");}
        h.succeed();
    }
    @GameTest(template="test/platform",batch="refinement_candle_migration")
    public static void standingAndLegacyFloatingCandlesBecomeOneFamily(GameTestHelper h){
        var local=new BlockPos(2,3,2);var pos=h.absolutePos(local);var canonical=ModBlocks.CANDLES.get("blue").get();h.setBlock(local,canonical);
        var chunk=h.getLevel().getChunkAt(pos);chunk.removeBlockEntity(pos);UnifiedCandleMigration.repair(chunk);
        h.assertTrue(h.getBlockEntity(local) instanceof FloatingCandleEntity,"old standing candle has no floating renderer entity");
        h.assertTrue(canonical.defaultBlockState().canSurvive(h.getLevel(),pos),"candle requires support");
        h.setBlock(local,ModernContent.LEGACY_CANDLES.get("blue").get());UnifiedCandleMigration.repair(chunk);
        h.assertTrue(h.getLevel().getBlockState(pos).is(canonical)&&h.getBlockEntity(local) instanceof FloatingCandleEntity,"legacy floating block did not migrate");
        var player=TestSupport.mockServerPlayer(h);player.tickCount=20;
        var old=new ItemStack(ModernContent.LEGACY_CANDLES.get("blue").get(),12);old.set(DataComponents.CUSTOM_NAME,Component.literal("Saved candle"));player.getInventory().setItem(0,old);
        UnifiedCandleMigration.inventory(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));var migrated=player.getInventory().getItem(0);
        h.assertTrue(migrated.is(canonical.asItem())&&migrated.getCount()==12&&migrated.getHoverName().getString().equals("Saved candle"),"legacy item count/data changed");
        h.assertTrue(ResearchCategories.getResearch("FLOATCANDLE")==null&&ResearchCategories.getResearch("MAGICALWOODWORK")==null,"extra research icon remains");h.succeed();
    }
    @GameTest(template="test/platform",batch="refinement_chest_boat_save")
    public static void chestBoatKeepsAllSlotsVariantAndMenuOnReload(GameTestHelper h){
        for(String wood:List.of("greatwood","silverwood")){
            var boat=ModernContent.CHEST_BOAT.get().create(h.getLevel());boat.wood(wood);boat.setItem(0,new ItemStack(Items.DIAMOND,7));boat.setItem(26,new ItemStack(Items.BOOK,11));
            var tag=new net.minecraft.nbt.CompoundTag();boat.save(tag);var loaded=(WoodChestBoat)EntityType.loadEntityRecursive(tag,h.getLevel(),e->e);
            h.assertTrue(loaded!=null&&loaded.silver()==wood.equals("silverwood")&&loaded.getContainerSize()==27,"variant/size lost");h.assertTrue(loaded.getItem(0).is(Items.DIAMOND)&&loaded.getItem(0).getCount()==7&&loaded.getItem(26).getCount()==11,"stored items lost");
            h.assertTrue(loaded.getDropItem()==ModernContent.CHEST_BOATS.get(wood).get(),"wrong pick/drop item");
            var player=TestSupport.mockServerPlayer(h);var menu=loaded.createMenu(1,player.getInventory(),player);h.assertTrue(menu!=null&&menu.slots.size()==63&&menu.slots.get(0).getItem().getCount()==7,"vanilla chest menu not backed by boat");
            var inventory=loaded.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ENTITY,null);h.assertTrue(inventory!=null&&inventory.getSlots()==27,"container capability absent");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="refinement_chest_boat_craft")
    public static void bothChestBoatsCraftFromTheirBoatAndChest(GameTestHelper h){
        for(String wood:List.of("greatwood","silverwood")){
            var input=CraftingInput.of(2,1,List.of(new ItemStack(Items.CHEST),new ItemStack(ModernContent.BOATS.get(wood).get())));var match=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
            h.assertTrue(match.value().assemble(input,h.getLevel().registryAccess()).is(ModernContent.CHEST_BOATS.get(wood).get()),"wrong chest boat craft");
        }h.succeed();
    }
    @GameTest(template="test/platform",batch="refinement_chest_boat_drops")
    public static void breakingChestBoatDropsInventoryAndCorrectBoatExactlyOnce(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(3,3,3));var boat=ModernContent.CHEST_BOAT.get().create(h.getLevel());boat.wood("silverwood");boat.moveTo(pos.getCenter());boat.setItem(0,new ItemStack(Items.DIAMOND,3));h.getLevel().addFreshEntity(boat);
        boat.hurt(h.getLevel().damageSources().playerAttack(h.makeMockPlayer(GameType.SURVIVAL)),5);
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3));h.assertTrue(boat.isRemoved(),"boat did not break");
        h.assertValueEqual(drops.stream().filter(e->e.getItem().is(Items.DIAMOND)).mapToInt(e->e.getItem().getCount()).sum(),3,"inventory lost/duplicated");
        h.assertValueEqual(drops.stream().filter(e->e.getItem().is(ModernContent.CHEST_BOATS.get("silverwood").get())).mapToInt(e->e.getItem().getCount()).sum(),1,"wrong/double boat drop");h.succeed();
    }
}
