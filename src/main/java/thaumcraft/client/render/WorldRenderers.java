package thaumcraft.client.render;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlockEntities;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class WorldRenderers {
    private WorldRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.NODE.get(), AuraNodeBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HOLE.get(), FocusBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WARDED.get(), FocusBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ENERGIZED_NODE.get(), EnergizedNodeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NODE_JAR.get(), JarNodeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NODE_STABILIZER.get(), NodeStabilizerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NODE_TRANSDUCER.get(), NodeTransducerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_RELAY.get(), VisRelayRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WAND_PEDESTAL.get(), WandPedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESEARCH_TABLE.get(), ResearchTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.JAR.get(), EssentiaJarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VOID_JAR.get(), EssentiaJarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VALVE_TUBE.get(), TubeValveRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ONEWAY_TUBE.get(), TubeOnewayRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ESSENTIA_BUFFER.get(), TubeBufferRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FILTER_TUBE.get(), TubeFilterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CENTRIFUGE.get(), CentrifugeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ESSENTIA_CRYSTALLIZER.get(), EssentiaCrystallizerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ESSENTIA_RESERVOIR.get(), EssentiaReservoirRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ALEMBIC.get(), AlembicRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(), AdvancedFurnaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.THAUMATORIUM.get(), ThaumatoriumRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BELLOWS.get(), BellowsRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUX_SCRUBBER.get(), FluxScrubberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_WORKBENCH.get(), ArcaneWorkbenchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOCAL_MANIPULATOR.get(), FocalManipulatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_CHARGE_RELAY.get(), ChargeRelayRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DECONSTRUCTION_TABLE.get(), DeconstructionTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL.get(), ArcanePedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INFUSION_PILLAR.get(), InfusionPillarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INFUSION_MATRIX.get(), RunicMatrixRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ETHEREAL_BLOOM.get(), thaumcraft.client.render.taint.EtherealBloomRenderer::new);
    }
}
