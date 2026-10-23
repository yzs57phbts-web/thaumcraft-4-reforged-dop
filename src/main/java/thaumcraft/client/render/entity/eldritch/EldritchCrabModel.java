package thaumcraft.client.render.entity.eldritch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import thaumcraft.entity.monster.eldritch.EldritchCrab;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;

public final class EldritchCrabModel extends EntityModel<EldritchCrab> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("eldritch_crab"), "main");
    private final ModelPart root;
    private final ModelPart tailHelm;
    private final ModelPart tailBare;
    private final ModelPart[] legs = new ModelPart[8];
    private final ModelPart rightClaw1;
    private final ModelPart rightClaw2;
    private final ModelPart leftClaw1;
    private final ModelPart leftClaw2;

    public EldritchCrabModel(ModelPart root) {
        super();
        this.root = root;
        tailHelm = root.getChild("tail_helm");
        tailBare = root.getChild("tail_bare");
        for (int i = 0; i < legs.length; i++) {
            legs[i] = root.getChild("leg_" + i);
        }
        rightClaw1 = root.getChild("right_claw_1");
        rightClaw2 = root.getChild("right_claw_2");
        leftClaw1 = root.getChild("left_claw_1");
        leftClaw2 = root.getChild("left_claw_2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tail_helm", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.5F, -0.4F, 9.0F, 9.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail_bare", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, -4.0F, -0.4F, 8.0F, 8.0F, 8.0F), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -3.5F, -6.066667F, 7.0F, 7.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0523599F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 31).addBox(-2.5F, -2.0F, -8.066667F, 5.0F, 5.0F, 2.0F).texOffs(0, 38).addBox(-2.0F, -1.5F, -9.066667F, 4.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -5.066667F, 2.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(-3.0F, 17.0F, -4.0F, 0.0F, 0.7504916F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -4.066667F, 2.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(4.0F, 17.0F, -5.0F, 0.0F, -0.7504916F, 0.0F));
        root.addOrReplaceChild("right_claw_0", CubeListBuilder.create().texOffs(0, 55).addBox(-2.0F, -2.5F, -3.066667F, 4.0F, 5.0F, 3.0F), PartPose.offset(-6.0F, 17.0F, -7.0F));
        root.addOrReplaceChild("left_claw_0", CubeListBuilder.create().texOffs(0, 55).mirror().addBox(-2.0F, -2.5F, -3.066667F, 4.0F, 5.0F, 3.0F), PartPose.offset(6.0F, 17.0F, -7.0F));
        root.addOrReplaceChild("right_claw_1", CubeListBuilder.create().texOffs(0, 47).addBox(-2.0F, -1.0F, -5.066667F, 4.0F, 3.0F, 5.0F), PartPose.offset(-6.0F, 15.5F, -10.0F));
        root.addOrReplaceChild("left_claw_1", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-2.0F, -1.0F, -5.066667F, 4.0F, 3.0F, 5.0F), PartPose.offset(6.0F, 15.5F, -10.0F));
        root.addOrReplaceChild("right_claw_2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(-6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F));
        root.addOrReplaceChild("left_claw_2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F));
        addLeg(root, 0, -4.0F, -1.5F, -4.5F, -4.5F, -0.9F, 0.2094395F, 0.4363323F);
        addLeg(root, 1, -4.0F, -3.5F, -5.0F, -5.0F, -1.066667F, -0.2094395F, 0.4363323F);
        addLeg(root, 2, 4.0F, -3.5F, -1.0F, 3.0F, -1.066667F, 0.2094395F, -0.4363323F);
        addLeg(root, 3, 4.0F, -1.5F, -1.5F, 2.5F, -0.9F, -0.2094395F, -0.4363323F);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void addLeg(PartDefinition root, int index, float x, float z, float firstX, float lowerX, float partZ, float yaw, float roll) {
        PartPose pose = PartPose.offsetAndRotation(x, 20.0F, z, 0.0F, yaw, roll);
        root.addOrReplaceChild("leg_" + (index * 2), CubeListBuilder.create().texOffs(36, 0).addBox(firstX, -1.0F, partZ, 6.0F, 2.0F, 2.0F), pose);
        root.addOrReplaceChild("leg_" + (index * 2 + 1), CubeListBuilder.create().texOffs(36, 4).addBox(lowerX, 1.0F, partZ, 2.0F, 5.0F, 2.0F), pose);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(EldritchCrab entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        tailHelm.visible = entity.hasHelm();
        tailBare.visible = !entity.hasHelm();
        float phase = limbSwing * 1.3324F;
        float front = -Mth.cos(phase) * 0.4F * limbSwingAmount;
        float rear = -Mth.cos(phase + Mth.PI) * 0.4F * limbSwingAmount;
        legs[0].yRot += front;
        legs[1].yRot += front;
        legs[0].zRot += front;
        legs[1].zRot += front;
        legs[2].yRot += rear;
        legs[3].yRot += rear;
        legs[2].zRot += rear;
        legs[3].zRot += rear;
        legs[4].yRot -= rear;
        legs[5].yRot -= rear;
        legs[4].zRot -= rear;
        legs[5].zRot -= rear;
        legs[6].yRot -= front;
        legs[7].yRot -= front;
        legs[6].zRot -= front;
        legs[7].zRot -= front;
        tailHelm.yRot = tailBare.yRot = Mth.cos(ageInTicks * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
        tailHelm.zRot = tailBare.zRot = Mth.cos(ageInTicks * 0.6662F) * limbSwingAmount * 0.125F;
        rightClaw2.xRot = 0.3141593F - Mth.sin(ageInTicks / 4.0F) * 0.25F;
        leftClaw2.xRot = 0.3141593F + Mth.sin(ageInTicks / 4.1F) * 0.25F;
        rightClaw1.xRot = Mth.sin(ageInTicks / 4.0F) * 0.125F;
        leftClaw1.xRot = -Mth.sin(ageInTicks / 4.1F) * 0.125F;
    }
}
