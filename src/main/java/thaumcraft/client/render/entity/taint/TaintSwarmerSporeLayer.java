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
import thaumcraft.entity.monster.taint.TaintSpore;

public final class TaintSwarmerSporeLayer<T extends Mob> extends RenderLayer<T, TaintEntityModel<T>> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/taint_spore.png");
    private final TaintSwarmerSporeModel model;

    public TaintSwarmerSporeLayer(RenderLayerParent<T, TaintEntityModel<T>> parent, TaintSwarmerSporeModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof TaintSpore spore) {
            model.setupAnim(spore, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), 0xF000F0,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 0xFFFFFFFF);
        }
    }
}
