package thaumcraft.client.render.entity.golem;

import thaumcraft.client.platform.StateModel;
import thaumcraft.entity.golem.GolemBase;
import net.minecraft.client.model.geom.ModelPart;
import thaumcraft.client.platform.TcRenderTypes;

public final class GolemAccessoriesModel extends StateModel<GolemBase, GolemRenderState> {
    private final ModelPart fez;
    private final ModelPart plate;
    private final ModelPart plateLeft;
    private final ModelPart plateRight;
    private final ModelPart hat;
    private final ModelPart glasses;
    private final ModelPart visor;
    private final ModelPart hatRim;
    private final ModelPart dartgun;
    private final ModelPart mace;
    private final ModelPart bowtie;
    private final ModelPart headJar;
    private final ModelPart headBrain;
    private final ModelPart evilHead;
    private final float fezY;
    private final float hatY;

    public GolemAccessoriesModel(ModelPart root) {
        super(root, TcRenderTypes::entityTranslucent);
        ModelPart body = root.getChild("golem");
        body.xScale = body.yScale = body.zScale = 0.4F;
        fez = body.getChild("fez");
        plate = body.getChild("plate");
        plateLeft = body.getChild("plate_left");
        plateRight = body.getChild("plate_right");
        hat = body.getChild("hat");
        glasses = body.getChild("glasses");
        visor = body.getChild("visor");
        hatRim = body.getChild("hat_rim");
        dartgun = body.getChild("dartgun");
        mace = body.getChild("mace");
        bowtie = body.getChild("bowtie");
        headJar = body.getChild("head_jar");
        headBrain = body.getChild("head_brain");
        evilHead = body.getChild("evil_head");
        fezY = fez.y;
        hatY = hat.y;
    }

    private static float accessoryArmAngle(GolemRenderState state, boolean left, float current) {
        if (state.actionActive) {
            return -2.0F + 1.5F * GolemModel.triangle(state.actionTimer, 10.0F);
        }
        if (state.leftArmActive || state.rightArmActive) {
            boolean active = left ? state.leftArmActive : state.rightArmActive;
            float timer = left ? state.leftArm : state.rightArm;
            return active ? -2.0F + 1.5F * GolemModel.triangle(timer, 10.0F) : current;
        }
        if (state.carrying || state.core == 5) {
            return -1.0F;
        }
        float swing = 1.5F * GolemModel.triangle(state.walkAnimationPos, 13.0F);
        return (left ? -0.2F - swing : -0.2F + swing) * state.walkAnimationSpeed;
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        super.setupAnim(state);
        ModelPart body = root.getChild("golem");
        body.xScale = body.yScale = body.zScale = 0.4F;
        boolean resting = state.core < 0 || state.bootup < 0.0F || state.inactive;
        float yaw = resting || state.bootup > 0.0F ? 0.0F : state.yRot * ((float) Math.PI / 180.0F);
        float pitch = resting ? 0.57595867F : state.bootup > 0.0F ? state.bootup / (180.0F / (float) Math.PI) : state.xRot * ((float) Math.PI / 180.0F);
        for (ModelPart part : new ModelPart[] {fez, glasses, visor, hat, hatRim, headJar, headBrain, evilHead}) {
            part.yRot = yaw;
            part.xRot = pitch;
        }
        fez.y = fezY - (state.advanced ? 0.16F : 0.0F);
        hat.y = hatY - (state.advanced ? 0.16F : 0.0F);
        hatRim.y = hat.y;
        dartgun.xRot = accessoryArmAngle(state, true, dartgun.xRot);
        mace.xRot = accessoryArmAngle(state, false, mace.xRot);
        String decoration = state.decoration;
        fez.visible = decoration.contains("F");
        hat.visible = decoration.contains("H");
        hatRim.visible = hat.visible;
        bowtie.visible = decoration.contains("B");
        plate.visible = decoration.contains("P");
        plateLeft.visible = plate.visible;
        plateRight.visible = plate.visible;
        glasses.visible = decoration.contains("G");
        visor.visible = decoration.contains("V");
        dartgun.visible = decoration.contains("R");
        mace.visible = decoration.contains("M");
        headJar.visible = state.advanced;
        headBrain.visible = state.advanced;
        evilHead.visible = state.advanced && state.core >= 0;
    }
}
