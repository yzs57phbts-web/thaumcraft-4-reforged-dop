package thaumcraft.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.AdvancedAlchemicalFurnaceBlockEntity;
import thaumcraft.alchemy.AdvancedAlchemicalFurnaceNozzleBlockEntity;
import thaumcraft.alchemy.AlchemicalFurnaceBlockEntity;
import thaumcraft.alchemy.AlembicBlockEntity;
import thaumcraft.alchemy.BellowsBlockEntity;
import thaumcraft.alchemy.CrucibleBlockEntity;
import thaumcraft.alchemy.FluxScrubberBlockEntity;
import thaumcraft.alchemy.MnemonicMatrixBlockEntity;
import thaumcraft.alchemy.ThaumatoriumBlockEntity;
import thaumcraft.alchemy.ThaumatoriumTopBlockEntity;
import thaumcraft.arcane.ArcaneWorkbenchBlockEntity;
import thaumcraft.arcane.DeconstructionTableBlockEntity;
import thaumcraft.arcane.VisChargeRelayBlockEntity;
import thaumcraft.arcane.FocalManipulatorBlockEntity;
import thaumcraft.blockentity.HoleBlockEntity;
import thaumcraft.blockentity.EtherealBloomBlockEntity;
import thaumcraft.blockentity.ManaPodBlockEntity;
import thaumcraft.blockentity.NodeJarBlockEntity;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.blockentity.WandPedestalBlockEntity;
import thaumcraft.blockentity.WardedBlockEntity;
import thaumcraft.blockentity.WardingStoneBlockEntity;
import thaumcraft.device.BannerBlockEntity;
import thaumcraft.device.BrainJarBlockEntity;
import thaumcraft.device.HungryChestBlockEntity;
import thaumcraft.device.OwnedBlockEntity;
import thaumcraft.device.ArcaneBoreBaseBlockEntity;
import thaumcraft.device.ArcaneBoreBlockEntity;
import thaumcraft.device.ArcaneSpaBlockEntity;
import thaumcraft.device.InfernalFurnaceBlockEntity;
import thaumcraft.device.InfernalFurnaceNozzleBlockEntity;
import thaumcraft.essentia.CentrifugeBlockEntity;
import thaumcraft.essentia.EssentiaBufferBlockEntity;
import thaumcraft.essentia.EssentiaCrystallizerBlockEntity;
import thaumcraft.essentia.EssentiaReservoirBlockEntity;
import thaumcraft.essentia.FilterTubeBlockEntity;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.essentia.OnewayTubeBlockEntity;
import thaumcraft.essentia.RestrictTubeBlockEntity;
import thaumcraft.essentia.TubeBlockEntity;
import thaumcraft.essentia.ValveTubeBlockEntity;
import thaumcraft.essentia.VoidJarBlockEntity;
import thaumcraft.infusion.InfusionMatrixBlockEntity;
import thaumcraft.infusion.InfusionPillarBlockEntity;
import thaumcraft.infusion.PedestalBlockEntity;
import thaumcraft.node.EnergizedNodeBlockEntity;
import thaumcraft.node.NodeBlockEntity;
import thaumcraft.node.NodeStabilizerBlockEntity;
import thaumcraft.node.NodeTransducerBlockEntity;
import thaumcraft.vis.VisRelayBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Thaumcraft.MODID);

    public static final Supplier<BlockEntityType<ManaPodBlockEntity>> MANA_POD = BLOCK_ENTITIES.register(
        "mana_pod",
        () -> BlockEntityType.Builder.of(ManaPodBlockEntity::new, ModBlocks.MANA_POD.get()).build(null)
    );
    public static final Supplier<BlockEntityType<WardingStoneBlockEntity>> WARDING_STONE = BLOCK_ENTITIES.register(
        "warding_stone",
        () -> BlockEntityType.Builder.of(WardingStoneBlockEntity::new, ModBlocks.WARDING_PAVING_STONE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<NodeBlockEntity>> NODE = BLOCK_ENTITIES.register(
        "node",
        () -> BlockEntityType.Builder.of(NodeBlockEntity::new, ModBlocks.AURA_NODE.get(), ModBlocks.CHARGED_OBSIDIAN_TOTEM.get(), ModBlocks.SILVERWOOD_KNOT.get()).build(null)
    );
    public static final Supplier<BlockEntityType<EnergizedNodeBlockEntity>> ENERGIZED_NODE = BLOCK_ENTITIES.register(
        "energized_node",
        () -> BlockEntityType.Builder.of(EnergizedNodeBlockEntity::new, ModBlocks.ENERGIZED_AURA_NODE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<NodeStabilizerBlockEntity>> NODE_STABILIZER = BLOCK_ENTITIES.register(
        "node_stabilizer",
        () -> BlockEntityType.Builder.of(NodeStabilizerBlockEntity::new, ModBlocks.NODE_STABILIZER.get(), ModBlocks.ADVANCED_NODE_STABILIZER.get()).build(null)
    );
    public static final Supplier<BlockEntityType<NodeTransducerBlockEntity>> NODE_TRANSDUCER = BLOCK_ENTITIES.register(
        "node_transducer",
        () -> BlockEntityType.Builder.of(NodeTransducerBlockEntity::new, ModBlocks.NODE_TRANSDUCER.get()).build(null)
    );
    public static final Supplier<BlockEntityType<VisRelayBlockEntity>> VIS_RELAY = BLOCK_ENTITIES.register(
        "vis_relay",
        () -> BlockEntityType.Builder.of(VisRelayBlockEntity::new, ModBlocks.VIS_RELAY.get()).build(null)
    );
    public static final Supplier<BlockEntityType<WandPedestalBlockEntity>> WAND_PEDESTAL = BLOCK_ENTITIES.register(
        "wand_pedestal",
        () -> BlockEntityType.Builder.of(WandPedestalBlockEntity::new, ModBlocks.WAND_RECHARGE_PEDESTAL.get()).build(null)
    );
    public static final Supplier<BlockEntityType<NodeJarBlockEntity>> NODE_JAR = BLOCK_ENTITIES.register(
        "node_jar",
        () -> BlockEntityType.Builder.of(NodeJarBlockEntity::new, ModBlocks.NODE_JAR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE = BLOCK_ENTITIES.register(
        "research_table",
        () -> BlockEntityType.Builder.of(ResearchTableBlockEntity::new, ModBlocks.RESEARCH_TABLE.get()).build(null)
    );

    public static final Supplier<BlockEntityType<JarBlockEntity>> JAR = BLOCK_ENTITIES.register(
        "jar",
        () -> BlockEntityType.Builder.of(JarBlockEntity::new, ModBlocks.WARDED_JAR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<VoidJarBlockEntity>> VOID_JAR = BLOCK_ENTITIES.register(
        "void_jar",
        () -> BlockEntityType.Builder.of(VoidJarBlockEntity::new, ModBlocks.VOID_JAR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<TubeBlockEntity>> TUBE = BLOCK_ENTITIES.register(
        "essentia_tube",
        () -> BlockEntityType.Builder.of(TubeBlockEntity::new, ModBlocks.ESSENTIA_TUBE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ValveTubeBlockEntity>> VALVE_TUBE = BLOCK_ENTITIES.register(
        "essentia_valve",
        () -> BlockEntityType.Builder.of(ValveTubeBlockEntity::new, ModBlocks.ESSENTIA_VALVE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<FilterTubeBlockEntity>> FILTER_TUBE = BLOCK_ENTITIES.register(
        "filtered_essentia_tube",
        () -> BlockEntityType.Builder.of(FilterTubeBlockEntity::new, ModBlocks.FILTERED_ESSENTIA_TUBE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<RestrictTubeBlockEntity>> RESTRICT_TUBE = BLOCK_ENTITIES.register(
        "restricted_essentia_tube",
        () -> BlockEntityType.Builder.of(RestrictTubeBlockEntity::new, ModBlocks.RESTRICTED_ESSENTIA_TUBE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<OnewayTubeBlockEntity>> ONEWAY_TUBE = BLOCK_ENTITIES.register(
        "directional_essentia_tube",
        () -> BlockEntityType.Builder.of(OnewayTubeBlockEntity::new, ModBlocks.DIRECTIONAL_ESSENTIA_TUBE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<EssentiaBufferBlockEntity>> ESSENTIA_BUFFER = BLOCK_ENTITIES.register(
        "essentia_buffer",
        () -> BlockEntityType.Builder.of(EssentiaBufferBlockEntity::new, ModBlocks.ESSENTIA_BUFFER.get()).build(null)
    );
    public static final Supplier<BlockEntityType<CentrifugeBlockEntity>> CENTRIFUGE = BLOCK_ENTITIES.register(
        "alchemical_centrifuge",
        () -> BlockEntityType.Builder.of(CentrifugeBlockEntity::new, ModBlocks.ALCHEMICAL_CENTRIFUGE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<EssentiaCrystallizerBlockEntity>> ESSENTIA_CRYSTALLIZER = BLOCK_ENTITIES.register(
        "essentia_crystallizer",
        () -> BlockEntityType.Builder.of(EssentiaCrystallizerBlockEntity::new, ModBlocks.ESSENTIA_CRYSTALLIZER.get()).build(null)
    );
    public static final Supplier<BlockEntityType<EssentiaReservoirBlockEntity>> ESSENTIA_RESERVOIR = BLOCK_ENTITIES.register(
        "essentia_reservoir",
        () -> BlockEntityType.Builder.of(EssentiaReservoirBlockEntity::new, ModBlocks.ESSENTIA_RESERVOIR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE = BLOCK_ENTITIES.register(
        "crucible",
        () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, ModBlocks.CRUCIBLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<AlembicBlockEntity>> ALEMBIC = BLOCK_ENTITIES.register(
        "alembic",
        () -> BlockEntityType.Builder.of(AlembicBlockEntity::new, ModBlocks.ALEMBIC.get()).build(null)
    );
    public static final Supplier<BlockEntityType<AlchemicalFurnaceBlockEntity>> ALCHEMICAL_FURNACE = BLOCK_ENTITIES.register(
        "alchemical_furnace",
        () -> BlockEntityType.Builder.of(AlchemicalFurnaceBlockEntity::new, ModBlocks.ALCHEMICAL_FURNACE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<AdvancedAlchemicalFurnaceBlockEntity>> ADVANCED_ALCHEMICAL_FURNACE = BLOCK_ENTITIES.register(
        "advanced_alchemical_furnace",
        () -> BlockEntityType.Builder.of(AdvancedAlchemicalFurnaceBlockEntity::new, ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<AdvancedAlchemicalFurnaceNozzleBlockEntity>> ADVANCED_ALCHEMICAL_FURNACE_NOZZLE = BLOCK_ENTITIES.register(
        "advanced_alchemical_furnace_nozzle",
        () -> BlockEntityType.Builder.of(AdvancedAlchemicalFurnaceNozzleBlockEntity::new, ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ThaumatoriumBlockEntity>> THAUMATORIUM = BLOCK_ENTITIES.register(
        "thaumatorium",
        () -> BlockEntityType.Builder.of(ThaumatoriumBlockEntity::new, ModBlocks.THAUMATORIUM.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ThaumatoriumTopBlockEntity>> THAUMATORIUM_TOP = BLOCK_ENTITIES.register(
        "thaumatorium_top",
        () -> BlockEntityType.Builder.of(ThaumatoriumTopBlockEntity::new, ModBlocks.THAUMATORIUM_TOP.get()).build(null)
    );
    public static final Supplier<BlockEntityType<MnemonicMatrixBlockEntity>> MNEMONIC_MATRIX = BLOCK_ENTITIES.register(
        "mnemonic_matrix",
        () -> BlockEntityType.Builder.of(MnemonicMatrixBlockEntity::new, ModBlocks.MNEMONIC_MATRIX.get()).build(null)
    );
    public static final Supplier<BlockEntityType<FluxScrubberBlockEntity>> FLUX_SCRUBBER = BLOCK_ENTITIES.register(
        "flux_scrubber",
        () -> BlockEntityType.Builder.of(FluxScrubberBlockEntity::new, ModBlocks.FLUX_SCRUBBER.get()).build(null)
    );
    public static final Supplier<BlockEntityType<BellowsBlockEntity>> BELLOWS = BLOCK_ENTITIES.register(
        "arcane_bellows",
        () -> BlockEntityType.Builder.of(BellowsBlockEntity::new, ModBlocks.ARCANE_BELLOWS.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ArcaneWorkbenchBlockEntity>> ARCANE_WORKBENCH = BLOCK_ENTITIES.register(
        "arcane_workbench",
        () -> BlockEntityType.Builder.of(ArcaneWorkbenchBlockEntity::new, ModBlocks.ARCANE_WORKTABLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<VisChargeRelayBlockEntity>> VIS_CHARGE_RELAY = BLOCK_ENTITIES.register(
        "vis_charge_relay",
        () -> BlockEntityType.Builder.of(VisChargeRelayBlockEntity::new, ModBlocks.VIS_CHARGE_RELAY.get()).build(null)
    );
    public static final Supplier<BlockEntityType<DeconstructionTableBlockEntity>> DECONSTRUCTION_TABLE = BLOCK_ENTITIES.register(
        "deconstruction_table",
        () -> BlockEntityType.Builder.of(DeconstructionTableBlockEntity::new, ModBlocks.DECONSTRUCTION_TABLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<InfusionMatrixBlockEntity>> INFUSION_MATRIX = BLOCK_ENTITIES.register(
        "infusion_matrix",
        () -> BlockEntityType.Builder.of(InfusionMatrixBlockEntity::new, ModBlocks.RUNIC_MATRIX.get()).build(null)
    );
    public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL = BLOCK_ENTITIES.register(
        "arcane_pedestal",
        () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, ModBlocks.ARCANE_PEDESTAL.get()).build(null)
    );
    public static final Supplier<BlockEntityType<InfusionPillarBlockEntity>> INFUSION_PILLAR = BLOCK_ENTITIES.register(
        "infusion_pillar",
        () -> BlockEntityType.Builder.of(InfusionPillarBlockEntity::new, ModBlocks.INFUSION_PILLAR.get()).build(null)
    );

    public static final Supplier<BlockEntityType<FocalManipulatorBlockEntity>> FOCAL_MANIPULATOR = BLOCK_ENTITIES.register(
        "focal_manipulator",
        () -> BlockEntityType.Builder.of(FocalManipulatorBlockEntity::new, ModBlocks.FOCAL_MANIPULATOR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<HoleBlockEntity>> HOLE = BLOCK_ENTITIES.register(
        "hole",
        () -> BlockEntityType.Builder.of(HoleBlockEntity::new, ModBlocks.HOLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<WardedBlockEntity>> WARDED = BLOCK_ENTITIES.register(
        "warded_block",
        () -> BlockEntityType.Builder.of(WardedBlockEntity::new, ModBlocks.WARDED_BLOCK.get()).build(null)
    );

    public static final Supplier<BlockEntityType<thaumcraft.device.MirrorBlockEntity>> MAGIC_MIRROR = BLOCK_ENTITIES.register(
        "magic_mirror",
        () -> BlockEntityType.Builder.of(thaumcraft.device.MirrorBlockEntity::new, ModBlocks.MAGIC_MIRROR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.EssentiaMirrorBlockEntity>> ESSENTIA_MIRROR = BLOCK_ENTITIES.register(
        "essentia_mirror",
        () -> BlockEntityType.Builder.of(thaumcraft.device.EssentiaMirrorBlockEntity::new, ModBlocks.ESSENTIA_MIRROR.get()).build(null)
    );

    public static final Supplier<BlockEntityType<thaumcraft.device.NitorLightBlockEntity>> NITOR_LIGHT = BLOCK_ENTITIES.register(
        "nitor_light",
        () -> BlockEntityType.Builder.of(thaumcraft.device.NitorLightBlockEntity::new, ModBlocks.NITOR_LIGHT.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.WardingAuraBlockEntity>> WARDING_AURA = BLOCK_ENTITIES.register(
        "warding_aura",
        () -> BlockEntityType.Builder.of(thaumcraft.device.WardingAuraBlockEntity::new, ModBlocks.WARDING_AURA.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.ArcaneLampBlockEntity>> ARCANE_LAMP = BLOCK_ENTITIES.register(
        "arcane_lamp",
        () -> BlockEntityType.Builder.of(thaumcraft.device.ArcaneLampBlockEntity::new, ModBlocks.ARCANE_LAMP.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.LampOfGrowthBlockEntity>> LAMP_OF_GROWTH = BLOCK_ENTITIES.register(
        "lamp_of_growth",
        () -> BlockEntityType.Builder.of(thaumcraft.device.LampOfGrowthBlockEntity::new, ModBlocks.LAMP_OF_GROWTH.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.LampOfFertilityBlockEntity>> LAMP_OF_FERTILITY = BLOCK_ENTITIES.register(
        "lamp_of_fertility",
        () -> BlockEntityType.Builder.of(thaumcraft.device.LampOfFertilityBlockEntity::new, ModBlocks.LAMP_OF_FERTILITY.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.ItemGrateBlockEntity>> ITEM_GRATE = BLOCK_ENTITIES.register(
        "item_grate",
        () -> BlockEntityType.Builder.of(thaumcraft.device.ItemGrateBlockEntity::new, ModBlocks.ITEM_GRATE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.ArcaneEarBlockEntity>> ARCANE_EAR = BLOCK_ENTITIES.register(
        "arcane_ear",
        () -> BlockEntityType.Builder.of(thaumcraft.device.ArcaneEarBlockEntity::new, ModBlocks.ARCANE_EAR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.ArcaneLevitatorBlockEntity>> ARCANE_LEVITATOR = BLOCK_ENTITIES.register(
        "arcane_levitator",
        () -> BlockEntityType.Builder.of(thaumcraft.device.ArcaneLevitatorBlockEntity::new, ModBlocks.ARCANE_LEVITATOR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<thaumcraft.device.CrystalClusterBlockEntity>> CRYSTAL_CLUSTER = BLOCK_ENTITIES.register(
        "crystal_cluster",
        () -> BlockEntityType.Builder.of(thaumcraft.device.CrystalClusterBlockEntity::new, ModBlocks.AIR_CRYSTAL_CLUSTER.get(), ModBlocks.FIRE_CRYSTAL_CLUSTER.get(), ModBlocks.WATER_CRYSTAL_CLUSTER.get(), ModBlocks.EARTH_CRYSTAL_CLUSTER.get(), ModBlocks.ORDER_CRYSTAL_CLUSTER.get(), ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get(), ModBlocks.MIXED_CRYSTAL_CLUSTER.get(), ModBlocks.STRANGE_CRYSTALS.get()).build(null)
    );
    public static final Supplier<BlockEntityType<OwnedBlockEntity>> OWNED = BLOCK_ENTITIES.register(
        "owned",
        () -> BlockEntityType.Builder.of(OwnedBlockEntity::new, ModBlocks.WARDED_GLASS.get(), ModBlocks.ARCANE_DOOR.get(), ModBlocks.ARCANE_PRESSURE_PLATE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<HungryChestBlockEntity>> HUNGRY_CHEST = BLOCK_ENTITIES.register(
        "hungry_chest",
        () -> BlockEntityType.Builder.of(HungryChestBlockEntity::new, ModBlocks.HUNGRY_CHEST.get()).build(null)
    );
    public static final Supplier<BlockEntityType<BrainJarBlockEntity>> BRAIN_JAR = BLOCK_ENTITIES.register(
        "brain_jar",
        () -> BlockEntityType.Builder.of(BrainJarBlockEntity::new, ModBlocks.BRAIN_JAR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<BannerBlockEntity>> BANNER = BLOCK_ENTITIES.register(
        "banner",
        () -> BlockEntityType.Builder.of(BannerBlockEntity::new, ModBlocks.BANNER.get()).build(null)
    );
    public static final Supplier<BlockEntityType<InfernalFurnaceBlockEntity>> INFERNAL_FURNACE = BLOCK_ENTITIES.register(
        "infernal_furnace",
        () -> BlockEntityType.Builder.of(InfernalFurnaceBlockEntity::new, ModBlocks.INFERNAL_FURNACE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<InfernalFurnaceNozzleBlockEntity>> INFERNAL_FURNACE_NOZZLE = BLOCK_ENTITIES.register(
        "infernal_furnace_nozzle",
        () -> BlockEntityType.Builder.of(InfernalFurnaceNozzleBlockEntity::new, ModBlocks.INFERNAL_FURNACE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ArcaneBoreBaseBlockEntity>> ARCANE_BORE_BASE = BLOCK_ENTITIES.register(
        "arcane_bore_base",
        () -> BlockEntityType.Builder.of(ArcaneBoreBaseBlockEntity::new, ModBlocks.ARCANE_BORE_BASE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ArcaneBoreBlockEntity>> ARCANE_BORE = BLOCK_ENTITIES.register(
        "arcane_bore",
        () -> BlockEntityType.Builder.of(ArcaneBoreBlockEntity::new, ModBlocks.ARCANE_BORE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ArcaneSpaBlockEntity>> ARCANE_SPA = BLOCK_ENTITIES.register(
        "arcane_spa",
        () -> BlockEntityType.Builder.of(ArcaneSpaBlockEntity::new, ModBlocks.ARCANE_SPA.get()).build(null)
    );
    public static final Supplier<BlockEntityType<EtherealBloomBlockEntity>> ETHEREAL_BLOOM = BLOCK_ENTITIES.register(
        "ethereal_bloom",
        () -> BlockEntityType.Builder.of(EtherealBloomBlockEntity::new, ModBlocks.ETHEREAL_BLOOM.get()).build(null)
    );

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
