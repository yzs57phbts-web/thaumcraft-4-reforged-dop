package thaumcraft.client.render.device;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlockEntities;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class MachineRenderers {
    private MachineRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_BORE.get(), ArcaneBoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_BORE_BASE.get(), ArcaneBoreBaseRenderer::new);
    }

}
