package thaumcraft.client.render.entity.golem;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.screen.GolemScreen;
import thaumcraft.client.screen.TravelingTrunkScreen;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModMenus;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class GolemClientEvents {
    private GolemClientEvents() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GOLEM.get(), GolemRenderer::new);
        event.registerEntityRenderer(ModEntities.TRAVELING_TRUNK.get(), TravelingTrunkRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLEM_BOBBER.get(), GolemBobberRenderer::new);
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GolemModelLayers.GOLEM, GolemModelLayers::golem);
        event.registerLayerDefinition(GolemModelLayers.ACCESSORIES, GolemModelLayers::accessories);
        event.registerLayerDefinition(GolemModelLayers.MARKER, GolemModelLayers::marker);
        event.registerLayerDefinition(GolemModelLayers.TRUNK, GolemModelLayers::trunk);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GOLEM.get(), GolemScreen::new);
        event.register(ModMenus.TRAVELING_TRUNK.get(), TravelingTrunkScreen::new);
    }

    @SubscribeEvent
    static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var type : thaumcraft.entity.golem.GolemType.values()) {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(Thaumcraft.id(type.itemId()));
                net.minecraft.client.renderer.item.ItemProperties.register(item, Thaumcraft.id("golem_advanced"), (stack, level, entity, seed) -> thaumcraft.entity.golem.GolemKeys.snapshot(stack).advanced() ? 1 : 0);
                net.minecraft.client.renderer.item.ItemProperties.register(item, Thaumcraft.id("golem_has_core"), (stack, level, entity, seed) -> thaumcraft.entity.golem.GolemKeys.snapshot(stack).core() >= 0 ? 1 : 0);
            }
        });
    }
}
