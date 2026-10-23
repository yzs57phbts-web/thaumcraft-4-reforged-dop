package thaumcraft.client.platform;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Mob;

public abstract class AbstractTranslucentMobRenderer<E extends Mob, M extends EntityModel<E>> extends MobRenderer<E, M> {
    protected AbstractTranslucentMobRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    protected RenderType getRenderType(E entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return TcRenderTypes.entityTranslucentNoDepth(this.getTextureLocation(entity));
    }
}
