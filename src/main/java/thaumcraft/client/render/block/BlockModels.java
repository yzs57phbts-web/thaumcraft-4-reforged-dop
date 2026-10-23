package thaumcraft.client.render.block;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class BlockModels {
    private BlockModels() {
    }

    @SubscribeEvent
    static void registerModels(ModelEvent.RegisterGeometryLoaders event) {
        event.register(Thaumcraft.id("warded"), (net.neoforged.neoforge.client.model.geometry.IGeometryLoader<WardedBlockModel.Unbaked>) (json, context) -> new WardedBlockModel.Unbaked());
        event.register(Thaumcraft.id("taint_fibres"), new thaumcraft.client.render.taint.TaintFibresModel.Loader());
    }
}
