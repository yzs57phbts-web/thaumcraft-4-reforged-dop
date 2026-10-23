package thaumcraft.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import thaumcraft.entity.projectile.BottleTaint;

public final class BottleTaintRenderer extends ThrownItemRenderer<BottleTaint> {
    public BottleTaintRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
