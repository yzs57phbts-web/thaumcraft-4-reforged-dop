package thaumcraft.client.render.entity.taint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import thaumcraft.entity.monster.taint.TaintedSheep;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;

public class TaintSheepFurModel extends EntityModel<TaintedSheep> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("taint_sheep_fur"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart rearLeftLeg;
    private final ModelPart rearRightLeg;

    public TaintSheepFurModel(ModelPart root) {
        super();
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.rearLeftLeg = root.getChild("rear_left_leg");
        this.rearRightLeg = root.getChild("rear_right_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.6F)), PartPose.offset(0.0F, 6.0F, -8.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new CubeDeformation(1.75F)), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2.0), 0.0F, 0.0F));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F));
        root.addOrReplaceChild("rear_left_leg", leg, PartPose.offset(-3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("rear_right_leg", leg, PartPose.offset(3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("front_left_leg", leg, PartPose.offset(-3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("front_right_leg", leg, PartPose.offset(3.0F, 12.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(TaintedSheep entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = limbSwing * 0.6662F;
        float partialTick = ageInTicks - entity.tickCount;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;
        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        int timer = entity.getGrazeTicks();
        float progress = timer <= 0 ? 0.0F
            : timer >= 4 && timer <= 36 ? 1.0F
            : timer < 4 ? (timer - partialTick) / 4.0F
            : -(timer - 40.0F - partialTick) / 4.0F;
        this.head.y = 6.0F + progress * 9.0F;
        if (timer > 0) {
            this.head.xRot = timer > 4 && timer <= 36
                ? (float) (Math.PI / 5.0) + 0.2199115F * Mth.sin((timer - 4.0F - partialTick) / 32.0F * 28.7F)
                : (float) (Math.PI / 5.0);
        }
        this.body.xRot = (float) (Math.PI / 2.0);
        this.frontLeftLeg.xRot = Mth.cos(walk) * 1.4F * limbSwingAmount;
        this.rearRightLeg.xRot = this.frontLeftLeg.xRot;
        this.frontRightLeg.xRot = Mth.cos(walk + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rearLeftLeg.xRot = this.frontRightLeg.xRot;
    }
}
