package thaumcraft.test;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.modern.ModernContent;

@GameTestHolder("thaumcraft") @PrefixGameTestTemplate(false)
public final class Refinement20GameTests {
 @GameTest(template="test/platform",batch="refinement20_hanging_signs")
 public static void hangingSignsCraftAttachAndKeepTextOnReload(GameTestHelper h){
  for(String wood:List.of("greatwood","silverwood")){
   var log=BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:"+wood+"_log"));
   var input=CraftingInput.of(3,3,List.of(new ItemStack(Items.CHAIN),ItemStack.EMPTY,new ItemStack(Items.CHAIN),new ItemStack(log),new ItemStack(log),new ItemStack(log),new ItemStack(log),new ItemStack(log),new ItemStack(log)));
   var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();var result=recipe.value().assemble(input,h.getLevel().registryAccess());
   h.assertTrue(result.getCount()==6&&BuiltInRegistries.ITEM.getKey(result.getItem()).getPath().equals(wood+"_hanging_sign"),"wrong hanging sign craft");
   for(boolean wall:List.of(false,true)){
    var local=new BlockPos(wall?5:2,3,2);h.setBlock(wall?local.east():local.above(),Blocks.OAK_PLANKS);
    var block=ModernContent.WOOD.get(wood+(wall?"_wall_hanging_sign":"_hanging_sign")).get();var state=block.defaultBlockState();
    if(wall)state=state.setValue(WallHangingSignBlock.FACING,Direction.SOUTH);
    h.assertTrue(state.canSurvive(h.getLevel(),h.absolutePos(local)),"hanging sign cannot attach");h.setBlock(local,state);
    var sign=(HangingSignBlockEntity)h.getBlockEntity(local);h.assertTrue(sign.getType()==BlockEntityType.HANGING_SIGN&&sign.getType().isValid(state),"vanilla hanging sign type not registered for wood");
    sign.setText(new SignText().setMessage(0,Component.literal("Front")).setHasGlowingText(true).setColor(DyeColor.YELLOW),true);sign.setText(new SignText().setMessage(0,Component.literal("Back")),false);sign.setWaxed(true);
    var tag=sign.saveWithFullMetadata(h.getLevel().registryAccess());var loaded=(HangingSignBlockEntity)BlockEntity.loadStatic(h.absolutePos(local),state,tag,h.getLevel().registryAccess());
    h.assertTrue(loaded!=null&&loaded.getFrontText().getMessage(0,false).getString().equals("Front")&&loaded.getBackText().getMessage(0,false).getString().equals("Back")&&loaded.getFrontText().hasGlowingText()&&loaded.isWaxed(),"hanging sign text/wax lost on reload");
   }
  }h.succeed();
 }
}
