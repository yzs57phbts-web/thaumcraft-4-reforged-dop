package thaumcraft.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import thaumcraft.entity.projectile.AlumentumProjectile;

public final class AlumentumRenderer extends ThrownItemRenderer<AlumentumProjectile> {
    public AlumentumRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
