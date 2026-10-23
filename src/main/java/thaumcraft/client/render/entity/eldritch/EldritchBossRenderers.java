package thaumcraft.client.render.entity.eldritch;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.entity.CultistLeaderRenderer;
import thaumcraft.client.render.entity.taint.TaintacleGiantRenderer;
import thaumcraft.registry.EldritchEntities;
import thaumcraft.registry.EldritchContent;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class EldritchBossRenderers {
    private EldritchBossRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EldritchEntities.ELDRITCH_GOLEM.get(), EldritchGolemRenderer::new);
        event.registerEntityRenderer(EldritchEntities.ELDRITCH_WARDEN.get(), EldritchWardenRenderer::new);
        event.registerEntityRenderer(EldritchEntities.CULTIST_LEADER.get(), CultistLeaderRenderer::new);
        event.registerEntityRenderer(EldritchEntities.TAINTACLE_GIANT.get(), TaintacleGiantRenderer::new);
        event.registerEntityRenderer(EldritchEntities.CULTIST_PORTAL.get(), CultistPortalRenderer::new);
        event.registerBlockEntityRenderer(EldritchContent.ELDRITCH_BLOCK_ENTITY.get(), EldritchBlockRenderer::new);
        event.registerBlockEntityRenderer(EldritchContent.ELDRITCH_PORTAL_ENTITY.get(), EldritchPortalBlockRenderer::new);
        event.registerBlockEntityRenderer(EldritchContent.ELDRITCH_NOTHING_ENTITY.get(), EldritchNothingRenderer::new);
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EldritchGolemModel.LAYER, EldritchGolemModel::createBodyLayer);
    }
}
