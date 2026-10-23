package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;

public class TcItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelResourceLocation VALVE_BASE = ModelResourceLocation.standalone(Thaumcraft.id("item/essentia_valve_base"));

    public static final ModelResourceLocation BRAIN_JAR_BASE = ModelResourceLocation.standalone(Thaumcraft.id("item/brain_jar_base"));

    public TcItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet entityModelSet) {
        super(dispatcher, entityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        switch (BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()) {
            case "wand" -> WandSpecialRenderer.renderWithContext(stack, displayContext, poseStack, buffers, light);
            case "thaumometer" -> ThaumometerItemRenderer.render(LegacyItemView.of(displayContext), poseStack, buffers, light);
            case "aura_node" -> NodeItemRenderer.render(poseStack, buffers);
            case "node_jar" -> NodeJarItemRenderer.render(stack, poseStack, buffers, light);
            case "node_stabilizer" -> NodeDeviceSpecialRenderers.stabilizer(poseStack, buffers, light, false);
            case "advanced_node_stabilizer" -> NodeDeviceSpecialRenderers.stabilizer(poseStack, buffers, light, true);
            case "node_transducer" -> NodeDeviceSpecialRenderers.transducer(poseStack, buffers, light);
            case "vis_relay" -> NodeDeviceSpecialRenderers.relay(poseStack, buffers, light);
            case "vis_charge_relay" -> ArcaneSpecialRenderers.chargeRelay(poseStack, buffers, light);
            case "runic_matrix" -> ArcaneSpecialRenderers.runicMatrix(poseStack, buffers, light);
            case "alchemical_centrifuge" -> EssentiaSpecialRenderers.centrifuge(poseStack, buffers, light);
            case "essentia_crystallizer" -> EssentiaSpecialRenderers.crystallizer(poseStack, buffers, light);
            case "essentia_reservoir" -> EssentiaSpecialRenderers.reservoir(poseStack, buffers, light);
            case "filled_jar" -> EssentiaSpecialRenderers.filledJar(stack, poseStack, buffers, light, false);
            case "filled_void_jar" -> EssentiaSpecialRenderers.filledJar(stack, poseStack, buffers, light, true);
            case "alembic" -> AlchemySpecialRenderers.alembic(poseStack, buffers, light);
            case "flux_scrubber" -> AlchemySpecialRenderers.fluxScrubber(poseStack, buffers, light);
            case "arcane_bellows" -> AlchemySpecialRenderers.bellows(poseStack, buffers, light);
            case "air_crystal_cluster" -> thaumcraft.client.render.device.DeviceSpecialRenderers.crystalCluster(buffers, poseStack, light, 0);
            case "fire_crystal_cluster" -> thaumcraft.client.render.device.DeviceSpecialRenderers.crystalCluster(buffers, poseStack, light, 1);
            case "water_crystal_cluster" -> thaumcraft.client.render.device.DeviceSpecialRenderers.crystalCluster(buffers, poseStack, light, 2);
            case "earth_crystal_cluster" -> thaumcraft.client.render.device.DeviceSpecialRenderers.crystalCluster(buffers, poseStack, light, 3);
            case "order_crystal_cluster" -> thaumcraft.client.render.device.DeviceSpecialRenderers.crystalCluster(buffers, poseStack, light, 4);
            case "entropy_crystal_cluster" -> thaumcraft.client.render.device.DeviceSpecialRenderers.crystalCluster(buffers, poseStack, light, 5);
            case "mixed_crystal_cluster" -> thaumcraft.client.render.device.DeviceSpecialRenderers.crystalCluster(buffers, poseStack, light, 6);
            case "strange_crystals" -> thaumcraft.client.render.device.DeviceSpecialRenderers.crystalCluster(buffers, poseStack, light, 7);
            case "hungry_chest" -> thaumcraft.client.render.device.WardedSpecialRenderers.hungryChest(buffers, poseStack, light);
            case "banner" -> thaumcraft.client.render.device.WardedSpecialRenderers.banner(stack, buffers, poseStack, light);
            case "arcane_bore_base" -> thaumcraft.client.render.device.MachineSpecialRenderers.boreBase(buffers, poseStack, light);
            case "arcane_bore" -> thaumcraft.client.render.device.MachineSpecialRenderers.bore(buffers, poseStack, light);
            case "brain_jar" -> {
                BakedModel model = Minecraft.getInstance().getModelManager().getModel(BRAIN_JAR_BASE);
                Minecraft.getInstance().getItemRenderer().renderModelLists(model, stack, light, overlay, poseStack, buffers.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true)));
                thaumcraft.client.render.device.WardedSpecialRenderers.brainJar(buffers, poseStack, light);
            }
            case "essentia_valve" -> {
                BakedModel model = Minecraft.getInstance().getModelManager().getModel(VALVE_BASE);
                Minecraft.getInstance().getItemRenderer().renderModelLists(
                    model,
                    stack,
                    light,
                    overlay,
                    poseStack,
                    buffers.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true))
                );
                EssentiaSpecialRenderers.tubeValve(poseStack, buffers, light);
            }
            default -> {
            }
        }
    }
}
