package thaumcraft.client.render.entity.eldritch;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.AlphaMobRenderer;
import thaumcraft.entity.boss.EldritchWarden;

public final class EldritchWardenRenderer extends AlphaMobRenderer<EldritchWarden, EldritchGuardianModel<EldritchWarden>> {
    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel<>(context.bakeLayer(EldritchGuardianModel.LAYER)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchWarden entity) {
        return Thaumcraft.id("textures/model/eldritch_warden.png");
    }

    @Override
    protected float getEntityAlpha(EldritchWarden entity) {
        return 1.0F;
    }
}
