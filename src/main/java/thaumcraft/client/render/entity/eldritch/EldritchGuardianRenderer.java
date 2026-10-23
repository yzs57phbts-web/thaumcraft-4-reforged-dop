package thaumcraft.client.render.entity.eldritch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.AlphaMobRenderer;
import thaumcraft.entity.monster.eldritch.EldritchGuardian;
import thaumcraft.taint.TaintBiomes;

public final class EldritchGuardianRenderer extends AlphaMobRenderer<EldritchGuardian, EldritchGuardianModel<EldritchGuardian>> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/eldritch_guardian.png");

    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel<>(context.bakeLayer(EldritchGuardianModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchGuardian entity) {
        return TEXTURE;
    }

    @Override
    protected float getEntityAlpha(EldritchGuardian entity) {
        if (entity.level().getBiome(entity.blockPosition()).is(TaintBiomes.OUTER_LANDS)) {
            return 1.0F;
        }
        double endDistance = entity.level().getDifficulty() == Difficulty.HARD ? 576.0 : 1024.0;
        double distance = entity.distanceToSqr(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        return distance < 256.0 ? 0.6F : (float) (0.6F * Math.clamp((endDistance - distance) / (endDistance - 256.0), 0.0, 1.0));
    }
}
