package local.thaumonomiconui;

import java.util.*;
import local.thaumonomiconui.mixin.ContainerGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.*;
import thaumcraft.client.research.*;
import thaumcraft.client.screen.ArcaneWorkbenchScreen;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchManager;

/** Attach to the native container screen: never replace or close its live server menu. */
public final class BookPanels {
 private static State current;
 private static Screen rendering;
 public static boolean isRendering(Screen screen){return rendering==screen;}
 public static State get(Screen screen){return current!=null&&current.owner==screen?current:null;}
 private static boolean supported(Screen s){return s instanceof InventoryScreen||s instanceof CraftingScreen||s instanceof ArcaneWorkbenchScreen;}
 public static void register(IEventBus bus){
  bus.addListener(BookPanels::init);bus.addListener(BookPanels::before);bus.addListener(BookPanels::after);
  bus.addListener(BookPanels::opening);bus.addListener(BookPanels::tick);
  bus.addListener(BookPanels::pressed);bus.addListener(BookPanels::released);bus.addListener(BookPanels::dragged);bus.addListener(BookPanels::scrolled);bus.addListener(BookPanels::key);
 }
 private static void init(ScreenEvent.Init.Post e){
  if(!supported(e.getScreen()))return;
  var owner=(AbstractContainerScreen<?>)e.getScreen();
  State old=get(owner);if(current!=null&&old==null)current.close(false);
  current=old==null?new State(owner):old;
  current.initialize();e.addListener(current.button);
  if(current.open)current.layout();
 }
 private static void before(ScreenEvent.Render.Pre e){var s=get(e.getScreen());if(s!=null)s.refresh();}
 private static void after(ScreenEvent.Render.Post e){var s=get(e.getScreen());if(s!=null&&s.open)s.render(e.getGuiGraphics(),e.getMouseX(),e.getMouseY(),e.getPartialTick());}
 private static void opening(ScreenEvent.Opening e){
  var s=get(e.getCurrentScreen());if(s==null)return;
  if(s.open&&(e.getNewScreen() instanceof ResearchBrowserScreen||e.getNewScreen() instanceof ResearchPageScreen)){
   e.setCanceled(true);s.book.removed();s.book=e.getNewScreen();s.layout();s.initBook();
  }else if(e.getNewScreen()==null||e.getNewScreen() instanceof AbstractContainerScreen<?> other&&other!=s.owner){s.close(true);current=null;}
 }
 private static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(current==null)return;
  if(mc.player==null){current=null;return;}
  if(mc.screen==current.owner&&current.open){if(ThaumonomiconUI.findBook().isEmpty())current.close(true);else current.book.tick();}
 }
 private static void pressed(ScreenEvent.MouseButtonPressed.Pre e){
  var s=get(e.getScreen());if(s==null||!s.open)return;
  if(s.recipe!=null&&s.recipe.isMouseOver(e.getMouseX(),e.getMouseY())){s.close(true);s.recipe.onPress();e.setCanceled(true);return;}
  if(s.over(e.getMouseX(),e.getMouseY())){s.dragging=true;s.book.mouseClicked(s.x(e.getMouseX()),s.y(e.getMouseY()),e.getButton());e.setCanceled(true);}
 }
 private static void released(ScreenEvent.MouseButtonReleased.Pre e){var s=get(e.getScreen());if(s!=null&&s.open&&s.dragging){s.dragging=false;s.book.mouseReleased(s.x(e.getMouseX()),s.y(e.getMouseY()),e.getButton());e.setCanceled(true);}}
 private static void dragged(ScreenEvent.MouseDragged.Pre e){var s=get(e.getScreen());if(s!=null&&s.open&&s.dragging){s.book.mouseDragged(s.x(e.getMouseX()),s.y(e.getMouseY()),e.getMouseButton(),e.getDragX()/s.scale,e.getDragY()/s.scale);e.setCanceled(true);}}
 private static void scrolled(ScreenEvent.MouseScrolled.Pre e){var s=get(e.getScreen());if(s!=null&&s.open&&s.over(e.getMouseX(),e.getMouseY())){s.book.mouseScrolled(s.x(e.getMouseX()),s.y(e.getMouseY()),e.getScrollDeltaX(),e.getScrollDeltaY());e.setCanceled(true);}}
 private static void key(ScreenEvent.KeyPressed.Pre e){
  var s=get(e.getScreen());if(s==null||!s.open)return;
  if(e.getKeyCode()==256){if(s.book instanceof ResearchPageScreen)s.book.keyPressed(e.getKeyCode(),e.getScanCode(),e.getModifiers());else s.close(true);e.setCanceled(true);}
 }
 public static List<Rect2i> extraAreas(AbstractContainerScreen<?> owner){var s=get(owner);return s!=null&&s.open?List.of(s.area()):List.of();}

 public static final class State {
  public final AbstractContainerScreen<?> owner;
  public Screen book;
  public BookButton button;
  public boolean open;
  private ImageButton recipe;
  private int baseLeft,baseTop,bookHeight;
  private double origin,scale=1,contentLeft=24,contentRight=304;
  private int leftClearance;
  private boolean dragging;
  private final Map<AbstractWidget,int[]> anchors=new IdentityHashMap<>();
  State(AbstractContainerScreen<?> owner){this.owner=owner;}
  void initialize(){
   recipe=owner.children().stream().filter(ImageButton.class::isInstance).map(ImageButton.class::cast).filter(w->!(w instanceof BookButton)&&w.getWidth()==20&&w.getHeight()==18).findFirst().orElse(null);
   baseLeft=owner.getGuiLeft();baseTop=owner.getGuiTop();anchors.clear();capture();button=new BookButton(this);button.update();
   if(open)initBook();
  }
  private void capture(){
   for(var c:owner.children())if(c instanceof AbstractWidget w&&w!=button&&w!=recipe&&!anchors.containsKey(w))anchors.put(w,new int[]{w.getX()-owner.getGuiLeft(),w.getY()-owner.getGuiTop()});
  }
  public void toggle(){if(open){close(true);return;}if(ThaumonomiconUI.findBook().isEmpty())return;
   if(owner instanceof RecipeUpdateListener r&&r.getRecipeBookComponent().isVisible()&&recipe!=null)recipe.onPress();
   baseLeft=owner.getGuiLeft();baseTop=owner.getGuiTop();anchors.clear();capture();book=new ResearchBrowserScreen();open=true;layout();initBook();
  }
  public void close(boolean restore){
   if(!open)return;book.removed();open=false;dragging=false;
   if(restore){geometry(baseLeft,baseTop);positionWidgets();}button.update();
  }
  void refresh(){
   if(open&&owner instanceof RecipeUpdateListener r&&r.getRecipeBookComponent().isVisible())close(false);
   if(open){if(ThaumonomiconUI.findBook().isEmpty())close(true);else layout();}
   else{baseLeft=owner.getGuiLeft();baseTop=owner.getGuiTop();}
   button.update();
  }
  private void geometry(int left,int top){var access=(ContainerGeometry)owner;access.thaumonomiconui$left(left);access.thaumonomiconui$top(top);}
  private void positionWidgets(){for(var entry:anchors.entrySet()){var w=entry.getKey();var p=entry.getValue();w.setPosition(owner.getGuiLeft()+p[0],owner.getGuiTop()+p[1]);}button.update();}
  private void bounds(){
   // Native book background has transparent side margins; align its visible paper/cover edge.
   if(book instanceof ResearchPageScreen){contentLeft=15;contentRight=337;}
   else{long tabs=ResearchCategories.all().stream().filter(c->!c.key().equals("ELDRITCH")||ResearchManager.isComplete(Minecraft.getInstance().player,"ELDRITCHMINOR")).count();contentLeft=24;contentRight=tabs>9?328:304;}
  }
  public void layout(){
   capture();bounds();leftClearance=0;
   for(var entry:anchors.entrySet()){var w=entry.getKey();var p=entry.getValue();if(w.visible&&p[0]<0&&p[0]+w.getWidth()>-96&&p[1]<owner.getYSize()&&p[1]+w.getHeight()>0)leftClearance=Math.max(leftClearance,-p[0]+2);}
   double available=owner.width-(ModList.get().isLoaded("jei")?110:4)-4-owner.getXSize()-4-leftClearance;
   double oldScale=scale;scale=Math.min(1,Math.max(0.2,available/(contentRight-contentLeft)));
   double total=(contentRight-contentLeft)*scale+4+leftClearance+owner.getXSize();
   double groupLeft=ModList.get().isLoaded("jei")?4:Math.max(4,(owner.width-total)/2);
   origin=groupLeft-contentLeft*scale;
   geometry((int)Math.ceil(groupLeft+(contentRight-contentLeft)*scale+4+leftClearance),baseTop);
   positionWidgets();
   if(bookHeight!=(int)Math.round(owner.height/scale)||oldScale!=scale)initBook();
  }
  private void initBook(){if(book==null)return;bookHeight=(int)Math.round(owner.height/scale);book.init(Minecraft.getInstance(),352,bookHeight);}
  public double scale(){return scale;}
  public double origin(){return origin;}
  public int leftClearance(){return leftClearance;}
  public double visualRight(){return origin+contentRight*scale;}
  public double gap(){return owner.getGuiLeft()-visualRight();}
  public Rect2i area(){double h=(book instanceof ResearchPageScreen?236:230)*scale;return new Rect2i((int)Math.floor(origin+contentLeft*scale),(int)Math.floor((owner.height-h)/2),(int)Math.ceil((contentRight-contentLeft)*scale),(int)Math.ceil(h));}
  private boolean over(double x,double y){var a=area();return open&&x>=a.getX()&&x<a.getX()+a.getWidth()&&y>=a.getY()&&y<a.getY()+a.getHeight();}
  public double x(double screenX){return(screenX-origin)/scale;}
  public double y(double screenY){return screenY/scale;}
  void render(GuiGraphics graphics,int mouseX,int mouseY,float tick){
   graphics.flush();var mc=Minecraft.getInstance();GuiGraphics panel=new GuiGraphics(mc,mc.renderBuffers().bufferSource()){
    @Override public void enableScissor(int x1,int y1,int x2,int y2){super.enableScissor((int)Math.floor(origin+x1*scale),(int)Math.floor(y1*scale),(int)Math.ceil(origin+x2*scale),(int)Math.ceil(y2*scale));}
   };
   panel.pose().translate(origin,0,0);panel.pose().scale((float)scale,(float)scale,1);rendering=book;
   try{book.renderWithTooltip(panel,over(mouseX,mouseY)?(int)x(mouseX):-10000,over(mouseX,mouseY)?(int)y(mouseY):-10000,tick);panel.flush();}finally{rendering=null;}
  }
 }
 public static final class BookButton extends ImageButton {
  private final State state;
  BookButton(State s){super(0,0,20,18,sprites(s.owner instanceof ArcaneWorkbenchScreen),b->s.toggle(),Component.translatable("thaumonomiconui.open"));state=s;setTooltip(Tooltip.create(Component.translatable("thaumonomiconui.open")));}
  private static WidgetSprites sprites(boolean arcane){String name=arcane?"arcane_button":"book_button";return new WidgetSprites(ResourceLocation.fromNamespaceAndPath("thaumonomiconui",name),ResourceLocation.fromNamespaceAndPath("thaumonomiconui",name+"_highlighted"));}
  public void update(){
   var o=state.owner;active=visible=!ThaumonomiconUI.findBook().isEmpty();
   if(o instanceof InventoryScreen){if(state.recipe!=null)state.recipe.setPosition(o.getGuiLeft()+(visible?126:104),o.getGuiTop()+61);setPosition(o.getGuiLeft()+104,o.getGuiTop()+61);}
   else if(o instanceof CraftingScreen){if(state.recipe!=null)state.recipe.setPosition(o.getGuiLeft()+5,o.getGuiTop()+34);setPosition(o.getGuiLeft()+5,o.getGuiTop()+14);}
   else setPosition(o.getGuiLeft()+158,o.getGuiTop()+114);
  }
  @Override public void renderWidget(GuiGraphics graphics,int x,int y,float tick){super.renderWidget(graphics,x,y,tick);graphics.renderItem(ThaumonomiconUI.findBook(),getX()+2,getY()+1);}
 }
}
