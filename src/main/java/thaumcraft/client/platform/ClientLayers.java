package thaumcraft.client.platform;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class ClientLayers {
    private ClientLayers() {
    }

    public static <E extends Entity, M extends EntityModel<E>> EyesLayer<E, M> fullbrightEyes(RenderLayerParent<E, M> parent, ResourceLocation texture) {
        return new EyesLayer<>(parent) {
            @Override
            public RenderType renderType() {
                return TcRenderTypes.eyes(texture);
            }
        };
    }
}
