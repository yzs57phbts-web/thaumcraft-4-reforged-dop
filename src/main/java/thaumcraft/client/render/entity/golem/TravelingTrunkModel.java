package thaumcraft.client.render.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import thaumcraft.client.platform.StateModel;
import thaumcraft.entity.golem.TravelingTrunk;
import net.minecraft.client.model.geom.ModelPart;
import thaumcraft.client.platform.TcRenderTypes;

public final class TravelingTrunkModel extends StateModel<TravelingTrunk, TravelingTrunkRenderState> {
    private final ModelPart root;
    private final ModelPart lid;
    private final ModelPart knob;

    public TravelingTrunkModel(ModelPart root) {
        super(root, TcRenderTypes::entityCutoutNoCull);
        this.root = root;
        lid = root.getChild("lid");
        knob = root.getChild("knob");
    }

    public void translateUpgrade(PoseStack poseStack) {
        root.translateAndRotate(poseStack);
        knob.translateAndRotate(poseStack);
        poseStack.translate(-0.075F, -0.115F, -0.94301F);
        poseStack.scale(0.15F, 0.15F, 0.15F);
    }

    @Override
    public void setupAnim(TravelingTrunkRenderState state) {
        super.setupAnim(state);
        lid.xRot = -state.lidOpen * ((float) Math.PI / 2.0F);
        knob.xRot = lid.xRot;
    }
}
