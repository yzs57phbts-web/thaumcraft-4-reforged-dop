package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.geom.BoxGeometry;
import thaumcraft.client.platform.ItemGeometry;
import thaumcraft.item.NodeJarItem;

public final class NodeJarItemRenderer {
    private NodeJarItemRenderer() {
    }

    public static void render(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int light) {
        AspectList aspects = NodeJarItem.getAspects(stack);
        if (aspects != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.4F, 0.5F);
            NodeRendering.submitCross(buffers, poseStack, aspects, NodeJarItem.getData(stack).type(), NodeJarItem.getData(stack).modifier());
            poseStack.popPose();
        }
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        BoxGeometry.UvRect bottom = rect(atlas.getSprite(Thaumcraft.id("block/jar_bottom")));
        BoxGeometry.UvRect top = rect(atlas.getSprite(Thaumcraft.id("block/jar_top")));
        BoxGeometry.UvRect side = rect(atlas.getSprite(Thaumcraft.id("block/jar_side")));
        ItemGeometry.solid(buffers, poseStack, TextureAtlas.LOCATION_BLOCKS, true, sink -> {
            BoxGeometry.box(sink, 0.1875F, 0.0F, 0.1875F, 0.8125F, 0.75F, 0.8125F, bottom, top, side, 0xFFFFFFFF, light);
            BoxGeometry.box(sink, 0.3125F, 0.75F, 0.3125F, 0.6875F, 0.875F, 0.6875F, bottom, top, side, 0xFFFFFFFF, light);
        });
    }

    private static BoxGeometry.UvRect rect(TextureAtlasSprite sprite) {
        return new BoxGeometry.UvRect(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }
}
