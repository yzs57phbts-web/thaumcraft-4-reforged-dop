package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.block.ResearchTableBlock;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.client.geom.ExtrudedGeometry;
import thaumcraft.client.geom.ModelBoxGeometry;
import thaumcraft.client.platform.EntityQuadSink;
import thaumcraft.client.platform.ItemGeometry;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.VertexQuadSink;
import thaumcraft.item.ResearchNotesItem;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.research.ResearchNote;
import thaumcraft.research.ResearchNoteKeys;

public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity> {
    private static final ResourceLocation TABLE = Thaumcraft.id("textures/model/restable.png");
    private static final ResourceLocation SCROLL = Thaumcraft.id("textures/model/restable2.png");
    private static final ResourceLocation QUILL = Thaumcraft.id("textures/block/tablequill.png");
    private static final ResourceLocation PARCHMENT = Thaumcraft.id("textures/misc/parchment.png");

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ResearchTableBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (entity.getLevel() == null) {
            return;
        }
        Direction facing = entity.getBlockState().getValue(ResearchTableBlock.FACING);
        float rotation = 270.0F - facing.toYRot();
        ItemStack scribe = entity.getItem(ResearchTableBlockEntity.SCRIBE_SLOT);
        ItemStack note = entity.getItem(ResearchTableBlockEntity.NOTE_SLOT);
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        int light = packedLight;
        if (scribe.getItem() instanceof ScribingToolsItem) {
            ItemGeometry.submit(buffers, poseStack, TcRenderTypes.entityTranslucent(TABLE), (pose, buffer) ->
                ModelBoxGeometry.box(new EntityQuadSink(pose, buffer), -6, -1.98F, 3, 3, 2, 3, 0, 44, 128, 64, true, 0xFFFFFFFF, light));
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(-0.17F, 0.1F, -0.15F);
            poseStack.mulPose(Axis.YP.rotationDegrees(15.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            ItemGeometry.submit(buffers, poseStack, TcRenderTypes.entityCutoutNoCull(QUILL), (pose, buffer) ->
                ExtrudedGeometry.render(new EntityQuadSink(pose, buffer), 1, 0, 0, 1, 16, 16, 0.025F, 0xFFFFFFFF, light));
            poseStack.popPose();
        }
        for (int page = 0; page < 6; page++) {
            poseStack.pushPose();
            poseStack.translate(0.1F, -0.01F - page * 0.015F, 0.35F);
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(15 + page % 3 * 2));
            poseStack.scale(0.5F, 0.6F, 0.6F);
            ItemGeometry.submit(buffers, poseStack, TcRenderTypes.translucent(PARCHMENT), (pose, buffer) -> {
                VertexQuadSink sink = new VertexQuadSink(pose, buffer);
                sink.vertex(0, 1, 0, 0, 1, 0xFFFFFFFF, light);
                sink.vertex(1, 1, 0, 1, 1, 0xFFFFFFFF, light);
                sink.vertex(1, 0, 0, 1, 0, 0xFFFFFFFF, light);
                sink.vertex(0, 0, 0, 0, 0, 0xFFFFFFFF, light);
            });
            poseStack.popPose();
        }
        if (note.getItem() instanceof ResearchNotesItem) {
            poseStack.pushPose();
            poseStack.translate(-2.0F / 16, -2.0F / 16, 2.0F / 16);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(10.0)));
            ItemGeometry.submit(buffers, poseStack, TcRenderTypes.entityCutout(SCROLL), (pose, buffer) ->
                ModelBoxGeometry.box(new EntityQuadSink(pose, buffer), -21, -0.5F, -8, 8, 2, 2, 0, 0, 128, 64, true, 0xFFFFFFFF, light));
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.scale(1.2F, 1.2F, 1.2F);
            poseStack.translate(-2.0F / 16, -2.0F / 16, 2.0F / 16);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(10.0)));
            ResearchNote data = ResearchNoteKeys.NOTE.get(note);
            int color = 0xFF000000 | ((data == null ? 0x999999 : data.color()) & 0xFFFFFF);
            ItemGeometry.submit(buffers, poseStack, TcRenderTypes.entityCutout(SCROLL), (pose, buffer) ->
                ModelBoxGeometry.box(new EntityQuadSink(pose, buffer), -15.1F, -0.275F, -6.75F, 1, 2, 2, 0, 4, 128, 64, true, color, light));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(ResearchTableBlockEntity entity) {
        return new AABB(entity.getBlockPos()).inflate(1.0, 0.0, 1.0).expandTowards(0.0, 1.0, 0.0);
    }
}
