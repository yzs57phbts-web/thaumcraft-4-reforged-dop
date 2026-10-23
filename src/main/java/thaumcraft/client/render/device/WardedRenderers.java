package thaumcraft.client.render.device;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlockEntities;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class WardedRenderers {
    private WardedRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.HUNGRY_CHEST.get(), HungryChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BRAIN_JAR.get(), BrainJarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BANNER.get(), BannerRenderer::new);
    }

    @SubscribeEvent
    static void registerModels(ModelEvent.RegisterGeometryLoaders event) {
        event.register(Thaumcraft.id("warded_glass"), (net.neoforged.neoforge.client.model.geometry.IGeometryLoader<WardedGlassModel.Unbaked>) (json, context) -> {
            java.util.List<net.minecraft.resources.ResourceLocation> models = new java.util.ArrayList<>();
            for (com.google.gson.JsonElement model : json.getAsJsonArray("models")) {
                models.add(net.minecraft.resources.ResourceLocation.parse(model.getAsString()));
            }
            return new WardedGlassModel.Unbaked(models);
        });
    }
}
