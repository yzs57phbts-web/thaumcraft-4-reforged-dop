package thaumcraft.client.render.device;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class DeviceRenderers {
    private DeviceRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_LAMP.get(), LampNozzleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAMP_OF_GROWTH.get(), LampNozzleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAMP_OF_FERTILITY.get(), LampNozzleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_CLUSTER.get(), CrystalClusterRenderer::new);
    }

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> tint == 0 ? 0xFF00A000 : 0xFFDD11FF, ModBlocks.ARCANE_LEVITATOR.get());
    }

}
