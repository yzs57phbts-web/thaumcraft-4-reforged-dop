package thaumcraft.client.render.entity.taint;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.taint.TaintedSheep;

public final class TaintSheepFurLayer<T extends Mob> extends RenderLayer<T, TaintEntityModel<T>> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/sheep_fur.png");
    private final TaintSheepFurModel model;

    public TaintSheepFurLayer(RenderLayerParent<T, TaintEntityModel<T>> parent, TaintSheepFurModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof TaintedSheep sheep && !sheep.isSheared()) {
            model.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 0xFFFFFFFF);
        }
    }
}
