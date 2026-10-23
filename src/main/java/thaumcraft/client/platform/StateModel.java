package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class StateModel<E extends Entity, S extends LivingEntityRenderState> extends EntityModel<E> {
    protected final ModelPart root;
    public S state;
    public int tint = -1;

    public StateModel(ModelPart root) {
        this(root, TcRenderTypes::entityCutoutNoCull);
    }

    public StateModel(ModelPart root, Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
        this.root = root;
    }

    public void setupAnim(S state) {
        root.getAllParts().forEach(ModelPart::resetPose);
    }

    @Override
    public void setupAnim(E entity, float walk, float speed, float age, float yaw, float pitch) {
        setupAnim(state);
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(poses, buffer, light, overlay, net.minecraft.util.FastColor.ARGB32.multiply(color, tint));
    }
}
