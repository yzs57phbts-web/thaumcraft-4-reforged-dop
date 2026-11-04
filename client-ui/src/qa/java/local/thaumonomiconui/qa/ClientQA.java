package local.thaumonomiconui.qa;
import java.util.*;
import java.nio.file.*;
import local.thaumonomiconui.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import thaumcraft.client.screen.ArcaneWorkbenchScreen;
import thaumcraft.registry.ModBlocks;
import thaumcraft.client.research.*;
import thaumcraft.research.*;
import net.neoforged.neoforge.common.NeoForge;

/** Focused UI-only check: normal -> hover -> normal for the three real screens. */
@EventBusSubscriber(modid="thaumonomiconui_qa",value=Dist.CLIENT)
public final class ClientQA {
 private static int mode,step,ticks;private static boolean requested,done,hover;
 private static volatile boolean ready;
 private static AbstractContainerScreen<?> owner;
 private static final List<String> checks=new ArrayList<>();
 private static final Map<String,Object> geometry=new LinkedHashMap<>();
 private static String name(){return new String[]{"inventory","crafting","arcane"}[mode];}
 private static void check(boolean ok,String label){if(!ok)throw new IllegalStateException(name()+": "+label);checks.add(name()+": "+label);}
 private static boolean expected(Screen s){return switch(mode){case 0->s instanceof InventoryScreen;case 1->s instanceof CraftingScreen;default->s instanceof ArcaneWorkbenchScreen;};}
 private static void request(Minecraft mc){ready=false;int which=mode;var id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
  var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.closeContainer();p.setGameMode(GameType.SURVIVAL);p.getAbilities().invulnerable=true;p.onUpdateAbilities();p.teleportTo(1.5,-60,5.5);
  p.getInventory().clearContent();p.getInventory().setItem(9,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:thaumonomicon"))));p.inventoryMenu.broadcastChanges();
  var pos=p.blockPosition().offset(2,0,0);
  if(which==1){p.serverLevel().setBlockAndUpdate(pos,Blocks.CRAFTING_TABLE.defaultBlockState());p.openMenu(new SimpleMenuProvider((n,inv,player)->new CraftingMenu(n,inv,ContainerLevelAccess.create(p.level(),pos)),Component.literal("Crafting")));}
  if(which==2){p.serverLevel().setBlockAndUpdate(pos,ModBlocks.ARCANE_WORKTABLE.get().defaultBlockState());thaumcraft.platform.Menus.open(p,(MenuProvider)p.level().getBlockEntity(pos),pos);}
  ready=true;
 });}
 @SubscribeEvent public static void render(ScreenEvent.Render.Pre e){
  if(done||owner==null||e.getScreen()!=owner)return;
  var state=BookPanels.get(owner);if(state==null)return;
  var button=state.button;int x=hover?button.getX()+10:-10000,y=hover?button.getY()+9:-10000;
  // Use the native render/tooltip path with deterministic hover coordinates.
  // ClientHooks still fires the one normal Render.Post after this cancelled Pre.
  e.setCanceled(true);owner.renderWithTooltip(e.getGuiGraphics(),x,y,e.getPartialTick());
 }
 private static void shot(String state){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"v121-"+name()+"-"+state+".png",mc.getMainRenderTarget(),m->{});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  if(done)return;var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;
  if(mc.screen instanceof DisconnectedScreen){finish(mc,new IllegalStateException("QA disconnected"));return;}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null)return;
  try{
   if(!requested){requested=true;request(mc);return;}if(!ready)return;
   if(step==0){if(mode==0&&!(mc.screen instanceof InventoryScreen)){if(mc.player.tickCount<60||mc.screen!=null)return;mc.setScreen(new InventoryScreen(mc.player));return;}if(!expected(mc.screen))return;owner=(AbstractContainerScreen<?>)mc.screen;hover=false;step=1;ticks=0;}
   if(++ticks<30)return;ticks=0;var s=BookPanels.get(owner);check(s!=null&&s.button.visible,"native book button present");
   if(step==1){geometry.put(name(),Map.of("x",s.button.getX(),"y",s.button.getY(),"width",20,"height",18,"scale",mc.getWindow().getGuiScale()));
    if(mode==1){var recipe=owner.children().stream().filter(ImageButton.class::isInstance).map(ImageButton.class::cast).filter(b->!(b instanceof BookPanels.BookButton)&&b.getWidth()==20&&b.getHeight()==18).findFirst().orElseThrow();check(s.button.getX()==recipe.getX()&&s.button.getY()+18<=recipe.getY()-2,"book above green recipes with clear gap");check(s.button.getY()+18<owner.getGuiTop()+72,"book clear of inventory label");}
    shot("normal");hover=true;step=2;
   }else if(step==2){check(s.button.isHovered(),"native hover active");shot("hover");hover=false;step=3;}
   else if(step==3){
    check(!s.button.isHovered(),"native hover clears");shot("restored");
    var press=new ScreenEvent.MouseButtonPressed.Pre(owner,s.button.getX()+10,s.button.getY()+9,0);NeoForge.EVENT_BUS.post(press);if(!press.isCanceled())owner.mouseClicked(press.getMouseX(),press.getMouseY(),0);
    var release=new ScreenEvent.MouseButtonReleased.Pre(owner,press.getMouseX(),press.getMouseY(),0);NeoForge.EVENT_BUS.post(release);if(!release.isCanceled())owner.mouseReleased(release.getMouseX(),release.getMouseY(),0);
    check(mc.screen==owner&&s.open&&s.book instanceof ResearchBrowserScreen,"bundled button opens side map");
    var z=(ZoomControl)s.book;double before=z.thaumonomiconui$getZoom();var scroll=new ScreenEvent.MouseScrolled.Pre(owner,s.origin()+176*s.scale(),owner.height/2.0,0,-1);NeoForge.EVENT_BUS.post(scroll);
    check(scroll.isCanceled()&&z.thaumonomiconui$getZoom()<before,"bundled map wheel zoom works");step=4;
   }else if(step==4){shot("bundled-map");ResearchPageScreen.open(ResearchCategories.getResearch("RESEARCH"));check(mc.screen==owner&&s.book instanceof ResearchPageScreen,"bundled native page opens beside container");step=5;}
   else{shot("bundled-page");var key=new ScreenEvent.KeyPressed.Pre(owner,256,0,0);NeoForge.EVENT_BUS.post(key);check(s.book instanceof ResearchBrowserScreen,"bundled page returns to side map");s.close(true);mode++;owner=null;step=0;requested=false;if(mode==3){mode=2;finish(mc,null);}}
  }catch(Throwable error){error.printStackTrace();finish(mc,error);}
 }
 private static void finish(Minecraft mc,Throwable error){done=true;try{var proof=new LinkedHashMap<String,Object>();proof.put("status",error==null?"PASS":"FAIL");proof.put("checks",checks);proof.put("button_geometry",geometry);proof.put("error",error==null?null:error.toString());Files.writeString(mc.gameDirectory.toPath().resolve("ui-proof-1.2.1.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));}catch(Exception e){e.printStackTrace();}mc.stop();}
}
