package thaumcraft.client.render.entity.golem;

import thaumcraft.client.platform.StateModel;
import thaumcraft.entity.golem.GolemBase;
import net.minecraft.client.model.geom.ModelPart;
import thaumcraft.client.platform.TcRenderTypes;

public final class GolemModel extends StateModel<GolemBase, GolemRenderState> {
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GolemModel(ModelPart root) {
        super(root, TcRenderTypes::entityCutoutNoCull);
        ModelPart body = root.getChild("golem");
        body.xScale = body.yScale = body.zScale = 0.4F;
        head = body.getChild("head");
        rightArm = body.getChild("right_arm");
        leftArm = body.getChild("left_arm");
        rightLeg = body.getChild("right_leg");
        leftLeg = body.getChild("left_leg");
    }

    public static float triangle(float value, float period) {
        return (Math.abs(value % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }

    public static float armAngle(GolemRenderState state, boolean left, float current) {
        if (state.core < 0 && !state.awakeWithoutCore || state.bootup < 0.0F) {
            return 0.0F;
        }
        if (state.actionActive) {
            return -2.0F + 1.5F * triangle(state.actionTimer, 5.0F);
        }
        if (state.leftArmActive || state.rightArmActive) {
            boolean active = left ? state.leftArmActive : state.rightArmActive;
            float timer = left ? state.leftArm : state.rightArm;
            return active ? -2.0F + 1.5F * triangle(timer, 20.0F) : current;
        }
        if (state.carrying || state.core == 5) {
            return -1.0F;
        }
        float swing = 1.5F * triangle(state.walkAnimationPos, 13.0F);
        return (left ? -0.2F - swing : -0.2F + swing) * state.walkAnimationSpeed;
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        super.setupAnim(state);
        ModelPart body = root.getChild("golem");
        body.xScale = body.yScale = body.zScale = 0.4F;
        boolean sleeping = state.core < 0 && !state.awakeWithoutCore || state.bootup < 0.0F;
        if (sleeping || state.inactive) {
            head.yRot = 0.0F;
            head.xRot = 0.57595867F;
        } else if (state.bootup > 0.0F) {
            head.yRot = 0.0F;
            head.xRot = state.bootup / (180.0F / (float) Math.PI);
        } else {
            head.yRot = state.yRot * ((float) Math.PI / 180.0F);
            head.xRot = state.xRot * ((float) Math.PI / 180.0F);
        }
        float legSwing = sleeping ? 0.0F : 1.5F * triangle(state.walkAnimationPos, 13.0F) * state.walkAnimationSpeed;
        rightLeg.xRot = -legSwing;
        leftLeg.xRot = legSwing;
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
        rightArm.xRot = armAngle(state, false, rightArm.xRot);
        leftArm.xRot = armAngle(state, true, leftArm.xRot);
        rightArm.zRot = 0.0F;
        leftArm.zRot = 0.0F;
        if (!sleeping && state.core == 6) {
            float angle = (1.0F - (0.5F + Math.min(64, state.carryLimit) / 128.0F)) * 25.0F * ((float) Math.PI / 180.0F);
            leftArm.zRot = angle;
            rightArm.zRot = -angle;
        }
    }
}
