package thaumcraft.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFx;
import thaumcraft.client.platform.WarpPostEffects;
import thaumcraft.client.research.ResearchBrowserScreen;
import thaumcraft.client.research.ThaumometerClient;
import thaumcraft.client.screen.AlchemicalFurnaceScreen;
import thaumcraft.client.screen.ArcaneWorkbenchScreen;
import thaumcraft.client.screen.DeconstructionTableScreen;
import thaumcraft.client.screen.FocalManipulatorScreen;
import thaumcraft.client.screen.FocusPouchScreen;
import thaumcraft.client.screen.HoverHarnessScreen;
import thaumcraft.client.screen.ResearchTableScreen;
import thaumcraft.client.screen.ThaumatoriumScreen;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModMenus;
import thaumcraft.research.ResearchClientHooks;

@Mod(value = Thaumcraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ThaumcraftClient {
    public ThaumcraftClient(IEventBus modEventBus, ModContainer container) {
        ClientPayloadHandlers.register();
        Fx.set(new ClientFx());
        thaumcraft.device.MachineFx.set(new thaumcraft.client.fx.MachineClientFx());
        ResearchClientHooks.setBookOpener(ResearchBrowserScreen::open);
        ThaumometerClient.register();
        modEventBus.addListener(ThaumcraftClient::setup);
        modEventBus.addListener(ThaumcraftClient::registerReloadListeners);
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(WarpPostEffects.INSTANCE);
    }

    private static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.item.ItemProperties.register(thaumcraft.registry.ModItems.BONE_BOW.get(), net.minecraft.resources.ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0.0F : (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) * 0.05F);
            net.minecraft.client.renderer.item.ItemProperties.register(thaumcraft.registry.ModItems.BONE_BOW.get(), net.minecraft.resources.ResourceLocation.withDefaultNamespace("pulling"), (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
        event.register(ModMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(ModMenus.DECONSTRUCTION_TABLE.get(), DeconstructionTableScreen::new);
        event.register(ModMenus.ALCHEMICAL_FURNACE.get(), AlchemicalFurnaceScreen::new);
        event.register(ModMenus.THAUMATORIUM.get(), ThaumatoriumScreen::new);
        event.register(ModMenus.FOCAL_MANIPULATOR.get(), FocalManipulatorScreen::new);
        event.register(ModMenus.FOCUS_POUCH.get(), FocusPouchScreen::new);
        event.register(ModMenus.HOVER_HARNESS.get(), HoverHarnessScreen::new);
        event.register(ModMenus.ARCANE_SPA.get(), thaumcraft.client.screen.ArcaneSpaScreen::new);
        event.register(ModMenus.ARCANE_BORE.get(), thaumcraft.client.screen.ArcaneBoreScreen::new);
    }
}
