package thaumcraft.client.render.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ArmedModel;
import com.mojang.blaze3d.vertex.VertexConsumer;
import thaumcraft.entity.monster.Pech;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;

public final class PechModel extends EntityModel<Pech> implements ArmedModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("pech"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jowls;
    private final ModelPart lowerPack;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public PechModel(ModelPart root) {
        super();
        this.root = root;
        body = root.getChild("body");
        head = root.getChild("head");
        jowls = root.getChild("jowls");
        lowerPack = root.getChild("lower_pack");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(34, 12).addBox(-3, 0, 0, 6, 10, 6), PartPose.offsetAndRotation(0, 9, -3, 0.3129957F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 11).addBox(-3.5F, -5, -5, 7, 5, 5), PartPose.offset(0, 8, 0));
        root.addOrReplaceChild("jowls", CubeListBuilder.create().texOffs(1, 21).addBox(-4, -1, -6, 8, 3, 5), PartPose.offset(0, 8, 0));
        root.addOrReplaceChild("lower_pack", CubeListBuilder.create().texOffs(0, 0).addBox(-5, 0, 0, 10, 5, 5), PartPose.offsetAndRotation(0, 10, 3.5F, 0.3013602F, 0.0F, 0.0F));
        root.addOrReplaceChild("upper_pack", CubeListBuilder.create().texOffs(64, 1).addBox(-7.5F, -14, 0, 15, 14, 11), PartPose.offsetAndRotation(0, 10, 3, 0.4537856F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 2).addBox(-2, 0, -1, 2, 6, 2), PartPose.offset(-3, 10, -1));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 2).mirror().addBox(0, 0, -1, 2, 6, 2), PartPose.offset(3, 10, -1));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(35, 1).mirror().addBox(-2.9F, 0, 0, 3, 6, 3), PartPose.offset(0, 18, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(35, 1).addBox(-0.1F, 0, 0, 3, 6, 3), PartPose.offset(0, 18, 0));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(Pech entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = ageInTicks - entity.tickCount;
        float swingAnimation = entity.getAttackAnim(partialTick);
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        jowls.yRot = head.yRot;
        jowls.xRot = head.xRot + Mth.PI / 12.0F + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.25F
            + 0.34906587F * Mth.abs(Mth.sin(entity.getMumble()));
        rightArm.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * limbSwingAmount;
        leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        rightArm.zRot = Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        leftArm.zRot = -rightArm.zRot;
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        lowerPack.yRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.25F;
        lowerPack.zRot = lowerPack.yRot;
        rightArm.yRot = 0.0F;
        leftArm.yRot = 0.0F;
        if (entity.isPassenger()) {
            rightArm.xRot += -Mth.PI / 5.0F;
            leftArm.xRot += -Mth.PI / 5.0F;
            rightLeg.xRot = leftLeg.xRot = -Mth.PI * 2.0F / 5.0F;
            rightLeg.yRot = Mth.PI / 10.0F;
            leftLeg.yRot = -Mth.PI / 10.0F;
        } else {
            rightLeg.yRot = leftLeg.yRot = 0.0F;
        }
        if (swingAnimation > 0.0F) {
            rightArm.yRot += body.yRot;
            leftArm.yRot += body.yRot;
            leftArm.xRot += body.yRot;
            float swing = 1.0F - swingAnimation;
            swing *= swing;
            swing *= swing;
            swing = 1.0F - swing;
            float swingSin = Mth.sin(swing * Mth.PI);
            float headPull = Mth.sin(swingAnimation * Mth.PI) * -(head.xRot - 0.7F) * 0.75F;
            rightArm.xRot -= swingSin * 1.2F + headPull;
            rightArm.yRot += body.yRot * 2.0F;
            rightArm.zRot = Mth.sin(swingAnimation * Mth.PI) * -0.4F;
        }
        if (entity.isCrouching()) {
            rightArm.xRot += 0.4F;
            leftArm.xRot += 0.4F;
        }
        rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
        leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        root.translateAndRotate(poseStack);
        (arm == HumanoidArm.LEFT ? leftArm : rightArm).translateAndRotate(poseStack);
    }
}
