package thaumcraft.modern.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.minecraft.world.item.DyeColor;
import thaumcraft.Thaumcraft;
import thaumcraft.modern.ModernContent;
@EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ModernClient {
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){thaumcraft.client.wand.WandKeyHandler.registerKeys(e);}
    @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->thaumcraft.modern.ModernSigns.TYPES.values().forEach(net.minecraft.client.renderer.Sheets::addWoodType));}
    @SubscribeEvent public static void signRenderer(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(ModernContent.SIGN.get(),net.minecraft.client.renderer.blockentity.SignRenderer::new);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(ModernContent.BOAT.get(),ModernBoatRenderer::new);e.registerEntityRenderer(ModernContent.CHEST_BOAT.get(),c->new ModernBoatRenderer(c,true));e.registerBlockEntityRenderer(ModernContent.CANDLE.get(),CandleRenderer::new);}
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item e){ModernContent.CANDLES.forEach((name,b)->e.register((s,t)->t==0?0xff000000|DyeColor.byName(name,DyeColor.WHITE).getTextureDiffuseColor():-1,b.get().asItem()));}
    @SubscribeEvent public static void blockColors(RegisterColorHandlersEvent.Block e){ModernContent.CANDLES.forEach((name,b)->e.register((s,l,p,t)->0xff000000|DyeColor.byName(name,DyeColor.WHITE).getTextureDiffuseColor(),b.get()));}
}
