package thaumcraft.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.Pech;

public final class PechRenderer extends MobRenderer<Pech, PechModel> {
    private static final ResourceLocation[] TEXTURES = {
        Thaumcraft.id("textures/model/pech_forage.png"),
        Thaumcraft.id("textures/model/pech_thaum.png"),
        Thaumcraft.id("textures/model/pech_stalker.png")
    };

    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(PechModel.LAYER)), 0.35F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Pech entity) {
        int type = entity.getPechType().ordinal();
        return TEXTURES[Math.max(0, Math.min(TEXTURES.length - 1, type))];
    }
}
