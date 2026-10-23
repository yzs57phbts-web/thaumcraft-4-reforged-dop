package thaumcraft.client.render.entity;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModEntities;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ModEntityRenderers {
    private ModEntityRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FIREBAT.get(), FireBatRenderer::new);
        event.registerEntityRenderer(ModEntities.BRAINY_ZOMBIE.get(), BrainyZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.GIANT_BRAINY_ZOMBIE.get(), GiantBrainyZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.INHABITED_ZOMBIE.get(), InhabitedZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.PECH.get(), PechRenderer::new);
        event.registerEntityRenderer(ModEntities.WISP.get(), WispRenderer::new);
        event.registerEntityRenderer(ModEntities.THAUMIC_SLIME.get(), ThaumicSlimeRenderer::new);
        event.registerEntityRenderer(ModEntities.EMBER.get(), EmberRenderer::new);
        event.registerEntityRenderer(ModEntities.SHOCK_ORB.get(), ShockOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLEM_ORB.get(), GolemOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.ASPECT_ORB.get(), AspectOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.EXPLOSIVE_ORB.get(), ExplosiveOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.PRIMAL_ORB.get(), PrimalOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_SHARD.get(), FrostShardRenderer::new);
        event.registerEntityRenderer(ModEntities.PECH_BLAST.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.ALUMENTUM_PROJECTILE.get(), AlumentumRenderer::new);
        event.registerEntityRenderer(ModEntities.BOTTLE_TAINT.get(), BottleTaintRenderer::new);
        event.registerEntityRenderer(ModEntities.FALLING_TAINT.get(), FallingTaintRenderer::new);
        event.registerEntityRenderer(ModEntities.DART.get(), DartRenderer::new);
        event.registerEntityRenderer(ModEntities.PRIMAL_ARROW.get(), PrimalArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.MIND_SPIDER.get(), thaumcraft.client.render.entity.eldritch.MindSpiderRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDRITCH_GUARDIAN.get(), thaumcraft.client.render.entity.eldritch.EldritchGuardianRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDRITCH_CRAB.get(), thaumcraft.client.render.entity.eldritch.EldritchCrabRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST_KNIGHT.get(), CultistKnightRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST_CLERIC.get(), CultistClericRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDRITCH_ORB.get(), thaumcraft.client.render.entity.eldritch.EldritchOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINT_SPIDER.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.SPIDER, 0.3F));
        event.registerEntityRenderer(ModEntities.TAINTACLE.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.TENTACLE, 0.5F));
        event.registerEntityRenderer(ModEntities.TAINTACLE_TINY.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.TENTACLE_TINY, 0.2F));
        event.registerEntityRenderer(ModEntities.TAINT_SPORE.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.SPORE, 0.3F));
        event.registerEntityRenderer(ModEntities.TAINT_SPORE_SWARMER.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.SWARMER, 0.7F));
        event.registerEntityRenderer(ModEntities.TAINT_SWARM.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINTED_CHICKEN.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.CHICKEN, 0.3F));
        event.registerEntityRenderer(ModEntities.TAINTED_COW.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.COW, 0.7F));
        event.registerEntityRenderer(ModEntities.TAINTED_CREEPER.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.CREEPER, 0.5F));
        event.registerEntityRenderer(ModEntities.TAINTED_PIG.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.PIG, 0.7F));
        event.registerEntityRenderer(ModEntities.TAINTED_SHEEP.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.SHEEP, 0.7F));
        event.registerEntityRenderer(ModEntities.TAINTED_VILLAGER.get(), thaumcraft.client.render.entity.taint.TaintMobRenderer.provider(thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.VILLAGER, 0.5F));
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FireBatModel.LAYER, FireBatModel::createBodyLayer);
        event.registerLayerDefinition(PechModel.LAYER, PechModel::createBodyLayer);
        event.registerLayerDefinition(thaumcraft.client.render.entity.eldritch.EldritchGuardianModel.LAYER, thaumcraft.client.render.entity.eldritch.EldritchGuardianModel::createBodyLayer);
        event.registerLayerDefinition(thaumcraft.client.render.entity.eldritch.EldritchCrabModel.LAYER, thaumcraft.client.render.entity.eldritch.EldritchCrabModel::createBodyLayer);
        for (var kind : thaumcraft.client.render.entity.taint.TaintEntityModel.Kind.values()) {
            event.registerLayerDefinition(kind.layer(), () -> thaumcraft.client.render.entity.taint.TaintEntityModel.createLayer(kind));
        }
        event.registerLayerDefinition(thaumcraft.client.render.entity.taint.TaintSheepFurModel.LAYER, thaumcraft.client.render.entity.taint.TaintSheepFurModel::createLayer);
        event.registerLayerDefinition(thaumcraft.client.render.entity.taint.TaintSwarmerSporeModel.LAYER, thaumcraft.client.render.entity.taint.TaintSwarmerSporeModel::createLayer);
    }
}
