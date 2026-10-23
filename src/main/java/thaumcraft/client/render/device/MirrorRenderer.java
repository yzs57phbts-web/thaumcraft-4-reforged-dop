package thaumcraft.client.render.device;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import thaumcraft.client.platform.ItemGeometry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.ExtrudedGeometry;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.PortalQuads;
import thaumcraft.client.render.EssentiaRendering;
import thaumcraft.client.render.NodeVisibility;
import thaumcraft.device.EssentiaMirrorBlockEntity;
import thaumcraft.device.LinkedMirrorBlockEntity;
import thaumcraft.device.MirrorBlockEntity;

public class MirrorRenderer implements BlockEntityRenderer<LinkedMirrorBlockEntity> {
    private static final ResourceLocation PANE = Thaumcraft.id("textures/block/mirrorpane.png");
    private static final ResourceLocation PANE_LINKED = Thaumcraft.id("textures/block/mirrorpanetrans.png");
    private static final ResourceLocation FRAME = Thaumcraft.id("block/mirrorframe");
    private static final ResourceLocation FRAME_ESSENTIA = Thaumcraft.id("block/mirrorframe2");
    private static final float PORTAL_INSET = 0.1875F;
    private static final float PORTAL_DEPTH = -0.01F;
    private static final float FRAME_THICKNESS = 0.0625F;
    private static final int WHITE = 0xFFFFFFFF;

    public static class State {
        int lightCoords;
        net.minecraft.core.BlockPos blockPos;
        Direction facing = Direction.UP;
        boolean linked;
        boolean showPortal;
        boolean essentia;
        float offset;
    }

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    private State extract(LinkedMirrorBlockEntity entity, float partialTicks, int light) {
        State state = new State();
        state.lightCoords = light;
        state.blockPos = entity.getBlockPos();
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        BlockPos pos = entity.getBlockPos();
        state.facing = entity.getFacing();
        state.linked = entity.isLinked();
        state.essentia = entity instanceof EssentiaMirrorBlockEntity;
        state.showPortal = state.linked && viewer != null && NodeVisibility.isVisibleTo(1.5F, viewer, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        state.offset = 0.0F;
        if (entity instanceof MirrorBlockEntity mirror && mirror.getInstability() > 0 && entity.getLevel() != null) {
            state.offset = entity.getLevel().getRandom().nextFloat() * (mirror.getInstability() / 10000.0F);
        }
        return state;
    }

    @Override
    public void render(LinkedMirrorBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource collector, int light, int overlay) {
        State state = extract(entity, partialTicks, light);
        poseStack.pushPose();
        orient(poseStack, state.facing);
        if (state.showPortal) {
            PortalQuads.submit(collector, poseStack, PORTAL_INSET, 1.0F - PORTAL_INSET, PORTAL_DEPTH);
            pane(collector, poseStack, PANE_LINKED, 0.02F + state.offset, state.lightCoords);
        } else {
            pane(collector, poseStack, PANE, 0.02F + state.offset, state.lightCoords);
        }
        frame(collector, poseStack, state.essentia ? FRAME_ESSENTIA : FRAME, state.lightCoords);
        poseStack.popPose();
    }

    private static void pane(MultiBufferSource collector, PoseStack poseStack, ResourceLocation texture, float offset, int light) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, -offset);
        ItemGeometry.blended(collector, poseStack, texture, 771, sink -> quad(sink, light));
        poseStack.popPose();
    }

    private static void quad(QuadSink sink, int light) {
        float edge = 15.99F / 16.0F;
        sink.vertex(0.0F, 1.0F, 0.0F, edge, 0.0F, WHITE, light);
        sink.vertex(1.0F, 1.0F, 0.0F, 0.0F, 0.0F, WHITE, light);
        sink.vertex(1.0F, 0.0F, 0.0F, 0.0F, edge, WHITE, light);
        sink.vertex(0.0F, 0.0F, 0.0F, edge, edge, WHITE, light);
    }

    private static void frame(MultiBufferSource collector, PoseStack poseStack, ResourceLocation id, int light) {
        TextureAtlasSprite sprite = EssentiaRendering.blockSprite(id);
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float minU = sprite.getU0();
        float maxV = sprite.getV1();
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        ItemGeometry.solid(
            collector,
            poseStack,
            TextureAtlas.LOCATION_BLOCKS,
            false,
            sink -> ExtrudedGeometry.render(sink, maxU, minV, minU, maxV, width, height, FRAME_THICKNESS, WHITE, light)
        );
    }

    private static void orient(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> {
                poseStack.translate(0.0F, 1.0F, 1.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case NORTH -> poseStack.translate(0.0F, 0.0F, 1.0F);
            case SOUTH -> {
                poseStack.translate(1.0F, 0.0F, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case WEST -> {
                poseStack.translate(1.0F, 0.0F, 1.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        }
    }
}
