package thaumcraft.client.render.armor;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ArmorLayers {
    private ArmorLayers() {
    }

    public static LayerDefinition robe(float f) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        head.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(16, 7).addBox(-4.5F, -9.0F, -4.6F, 9.0F, 9.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(52, 13).addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2268928F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood3", CubeListBuilder.create().texOffs(52, 14).addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3490659F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood4", CubeListBuilder.create().texOffs(53, 15).addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5759587F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -3.0F, 8.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt_b", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -4.0F, 8.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.141593F, 0.0F));
        body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("rarm1", CubeListBuilder.create().texOffs(88, 39).addBox(-3.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("rarm2", CubeListBuilder.create().texOffs(76, 32).addBox(-3.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("rarm3", CubeListBuilder.create().texOffs(88, 32).addBox(-2.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulderplate_top_r", CubeListBuilder.create().texOffs(56, 25).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r1", CubeListBuilder.create().texOffs(56, 33).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r2", CubeListBuilder.create().texOffs(40, 33).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r3", CubeListBuilder.create().texOffs(40, 33).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        leftArm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(16, 45).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("larm1", CubeListBuilder.create().texOffs(88, 39).mirror().addBox(-1.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("larm2", CubeListBuilder.create().texOffs(76, 32).addBox(-1.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("larm3", CubeListBuilder.create().texOffs(88, 32).addBox(-0.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulderplate_top_l", CubeListBuilder.create().texOffs(56, 25).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l1", CubeListBuilder.create().texOffs(56, 33).addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l2", CubeListBuilder.create().texOffs(40, 33).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l3", CubeListBuilder.create().texOffs(40, 33).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        rightLeg.addOrReplaceChild("legpanel_r4", CubeListBuilder.create().texOffs(76, 38).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r5", CubeListBuilder.create().texOffs(76, 42).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r6", CubeListBuilder.create().texOffs(82, 38).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(116, 25).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("sidecloth_r1", CubeListBuilder.create().texOffs(116, 42).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.122173F));
        rightLeg.addOrReplaceChild("sidecloth_r2", CubeListBuilder.create().texOffs(116, 34).addBox(-1.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.296706F));
        rightLeg.addOrReplaceChild("sidecloth_r3", CubeListBuilder.create().texOffs(116, 1).addBox(0.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5235987755982988F));
        leftLeg.addOrReplaceChild("legpanel_l4", CubeListBuilder.create().texOffs(76, 38).mirror().addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l5", CubeListBuilder.create().texOffs(76, 42).mirror().addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l6", CubeListBuilder.create().texOffs(82, 38).mirror().addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(116, 25).addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("sidecloth_l1", CubeListBuilder.create().texOffs(116, 42).addBox(1.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.122173F));
        leftLeg.addOrReplaceChild("sidecloth_l2", CubeListBuilder.create().texOffs(116, 34).addBox(0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.296706F));
        leftLeg.addOrReplaceChild("sidecloth_l3", CubeListBuilder.create().texOffs(116, 1).addBox(-1.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5235987755982988F));
        if (f < 1.0F) {
            leftLeg.addOrReplaceChild("focipouch", CubeListBuilder.create().texOffs(100, 20).addBox(3.5F, 0.5F, -2.5F, 3.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.122173F));
            body.addOrReplaceChild("frontcloth_r1", CubeListBuilder.create().texOffs(108, 38).addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(-3.0F, 11.0F, -2.9F, -0.1047198F, 0.0F, 0.0F));
            body.addOrReplaceChild("frontcloth_r2", CubeListBuilder.create().texOffs(108, 47).addBox(0.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(-3.0F, 11.0F, -2.9F, -0.3316126F, 0.0F, 0.0F));
            body.addOrReplaceChild("frontcloth_l1", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.1047198F, 0.0F, 0.0F));
            body.addOrReplaceChild("frontcloth_l2", CubeListBuilder.create().texOffs(108, 47).mirror().addBox(0.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.3316126F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloth_back_r1", CubeListBuilder.create().texOffs(118, 16).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.1047198F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloth_back_r2", CubeListBuilder.create().texOffs(123, 9).addBox(0.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloth_back_r3", CubeListBuilder.create().texOffs(120, 12).mirror().addBox(1.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloth_back_l1", CubeListBuilder.create().texOffs(118, 16).addBox(0.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.1047198F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloth_back_l2", CubeListBuilder.create().texOffs(123, 9).mirror().addBox(3.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloth_back_l3", CubeListBuilder.create().texOffs(120, 12).addBox(0.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F));
        }
        if (f >= 1.0F) {
            body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(16, 25).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("chestthing", CubeListBuilder.create().texOffs(56, 50).addBox(-2.5F, 1.0F, -4.0F, 5.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(78, 25).addBox(-2.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1919862F));
            body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 1.9F, 8.0F, 11.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("book", CubeListBuilder.create().texOffs(81, 16).addBox(1.0F, 0.0F, 4.0F, 5.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7679449F));
            body.addOrReplaceChild("clothchest_l", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(2.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("clothchest_r", CubeListBuilder.create().texOffs(108, 38).addBox(-4.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static Map<String, ModelPart> robeParts(ModelPart root) {
        Map<String, ModelPart> parts = new HashMap<>();
        if (root.getChild("head").hasChild("hood1")) {
            parts.put("hood1", root.getChild("head").getChild("hood1"));
        }
        if (root.getChild("head").hasChild("hood2")) {
            parts.put("hood2", root.getChild("head").getChild("hood2"));
        }
        if (root.getChild("head").hasChild("hood3")) {
            parts.put("hood3", root.getChild("head").getChild("hood3"));
        }
        if (root.getChild("head").hasChild("hood4")) {
            parts.put("hood4", root.getChild("head").getChild("hood4"));
        }
        if (root.getChild("body").hasChild("mbelt")) {
            parts.put("mbelt", root.getChild("body").getChild("mbelt"));
        }
        if (root.getChild("body").hasChild("mbelt_b")) {
            parts.put("mbelt_b", root.getChild("body").getChild("mbelt_b"));
        }
        if (root.getChild("body").hasChild("mbelt_l")) {
            parts.put("mbelt_l", root.getChild("body").getChild("mbelt_l"));
        }
        if (root.getChild("body").hasChild("mbelt_r")) {
            parts.put("mbelt_r", root.getChild("body").getChild("mbelt_r"));
        }
        if (root.getChild("left_leg").hasChild("focipouch")) {
            parts.put("focipouch", root.getChild("left_leg").getChild("focipouch"));
        }
        if (root.getChild("body").hasChild("frontcloth_r1")) {
            parts.put("frontcloth_r1", root.getChild("body").getChild("frontcloth_r1"));
        }
        if (root.getChild("body").hasChild("frontcloth_r2")) {
            parts.put("frontcloth_r2", root.getChild("body").getChild("frontcloth_r2"));
        }
        if (root.getChild("body").hasChild("frontcloth_l1")) {
            parts.put("frontcloth_l1", root.getChild("body").getChild("frontcloth_l1"));
        }
        if (root.getChild("body").hasChild("frontcloth_l2")) {
            parts.put("frontcloth_l2", root.getChild("body").getChild("frontcloth_l2"));
        }
        if (root.getChild("body").hasChild("cloth_back_r1")) {
            parts.put("cloth_back_r1", root.getChild("body").getChild("cloth_back_r1"));
        }
        if (root.getChild("body").hasChild("cloth_back_r2")) {
            parts.put("cloth_back_r2", root.getChild("body").getChild("cloth_back_r2"));
        }
        if (root.getChild("body").hasChild("cloth_back_r3")) {
            parts.put("cloth_back_r3", root.getChild("body").getChild("cloth_back_r3"));
        }
        if (root.getChild("body").hasChild("cloth_back_l1")) {
            parts.put("cloth_back_l1", root.getChild("body").getChild("cloth_back_l1"));
        }
        if (root.getChild("body").hasChild("cloth_back_l2")) {
            parts.put("cloth_back_l2", root.getChild("body").getChild("cloth_back_l2"));
        }
        if (root.getChild("body").hasChild("cloth_back_l3")) {
            parts.put("cloth_back_l3", root.getChild("body").getChild("cloth_back_l3"));
        }
        if (root.getChild("body").hasChild("chestplate")) {
            parts.put("chestplate", root.getChild("body").getChild("chestplate"));
        }
        if (root.getChild("body").hasChild("chestthing")) {
            parts.put("chestthing", root.getChild("body").getChild("chestthing"));
        }
        if (root.getChild("body").hasChild("scroll")) {
            parts.put("scroll", root.getChild("body").getChild("scroll"));
        }
        if (root.getChild("body").hasChild("backplate")) {
            parts.put("backplate", root.getChild("body").getChild("backplate"));
        }
        if (root.getChild("body").hasChild("book")) {
            parts.put("book", root.getChild("body").getChild("book"));
        }
        if (root.getChild("body").hasChild("clothchest_l")) {
            parts.put("clothchest_l", root.getChild("body").getChild("clothchest_l"));
        }
        if (root.getChild("body").hasChild("clothchest_r")) {
            parts.put("clothchest_r", root.getChild("body").getChild("clothchest_r"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r")) {
            parts.put("shoulder_r", root.getChild("right_arm").getChild("shoulder_r"));
        }
        if (root.getChild("right_arm").hasChild("rarm1")) {
            parts.put("rarm1", root.getChild("right_arm").getChild("rarm1"));
        }
        if (root.getChild("right_arm").hasChild("rarm2")) {
            parts.put("rarm2", root.getChild("right_arm").getChild("rarm2"));
        }
        if (root.getChild("right_arm").hasChild("rarm3")) {
            parts.put("rarm3", root.getChild("right_arm").getChild("rarm3"));
        }
        if (root.getChild("right_arm").hasChild("shoulderplate_top_r")) {
            parts.put("shoulderplate_top_r", root.getChild("right_arm").getChild("shoulderplate_top_r"));
        }
        if (root.getChild("right_arm").hasChild("shoulderplate_r1")) {
            parts.put("shoulderplate_r1", root.getChild("right_arm").getChild("shoulderplate_r1"));
        }
        if (root.getChild("right_arm").hasChild("shoulderplate_r2")) {
            parts.put("shoulderplate_r2", root.getChild("right_arm").getChild("shoulderplate_r2"));
        }
        if (root.getChild("right_arm").hasChild("shoulderplate_r3")) {
            parts.put("shoulderplate_r3", root.getChild("right_arm").getChild("shoulderplate_r3"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l")) {
            parts.put("shoulder_l", root.getChild("left_arm").getChild("shoulder_l"));
        }
        if (root.getChild("left_arm").hasChild("larm1")) {
            parts.put("larm1", root.getChild("left_arm").getChild("larm1"));
        }
        if (root.getChild("left_arm").hasChild("larm2")) {
            parts.put("larm2", root.getChild("left_arm").getChild("larm2"));
        }
        if (root.getChild("left_arm").hasChild("larm3")) {
            parts.put("larm3", root.getChild("left_arm").getChild("larm3"));
        }
        if (root.getChild("left_arm").hasChild("shoulderplate_top_l")) {
            parts.put("shoulderplate_top_l", root.getChild("left_arm").getChild("shoulderplate_top_l"));
        }
        if (root.getChild("left_arm").hasChild("shoulderplate_l1")) {
            parts.put("shoulderplate_l1", root.getChild("left_arm").getChild("shoulderplate_l1"));
        }
        if (root.getChild("left_arm").hasChild("shoulderplate_l2")) {
            parts.put("shoulderplate_l2", root.getChild("left_arm").getChild("shoulderplate_l2"));
        }
        if (root.getChild("left_arm").hasChild("shoulderplate_l3")) {
            parts.put("shoulderplate_l3", root.getChild("left_arm").getChild("shoulderplate_l3"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r4")) {
            parts.put("legpanel_r4", root.getChild("right_leg").getChild("legpanel_r4"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r5")) {
            parts.put("legpanel_r5", root.getChild("right_leg").getChild("legpanel_r5"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r6")) {
            parts.put("legpanel_r6", root.getChild("right_leg").getChild("legpanel_r6"));
        }
        if (root.getChild("right_leg").hasChild("sidepanel_r1")) {
            parts.put("sidepanel_r1", root.getChild("right_leg").getChild("sidepanel_r1"));
        }
        if (root.getChild("right_leg").hasChild("sidecloth_r1")) {
            parts.put("sidecloth_r1", root.getChild("right_leg").getChild("sidecloth_r1"));
        }
        if (root.getChild("right_leg").hasChild("sidecloth_r2")) {
            parts.put("sidecloth_r2", root.getChild("right_leg").getChild("sidecloth_r2"));
        }
        if (root.getChild("right_leg").hasChild("sidecloth_r3")) {
            parts.put("sidecloth_r3", root.getChild("right_leg").getChild("sidecloth_r3"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l4")) {
            parts.put("legpanel_l4", root.getChild("left_leg").getChild("legpanel_l4"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l5")) {
            parts.put("legpanel_l5", root.getChild("left_leg").getChild("legpanel_l5"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l6")) {
            parts.put("legpanel_l6", root.getChild("left_leg").getChild("legpanel_l6"));
        }
        if (root.getChild("left_leg").hasChild("sidepanel_l1")) {
            parts.put("sidepanel_l1", root.getChild("left_leg").getChild("sidepanel_l1"));
        }
        if (root.getChild("left_leg").hasChild("sidecloth_l1")) {
            parts.put("sidecloth_l1", root.getChild("left_leg").getChild("sidecloth_l1"));
        }
        if (root.getChild("left_leg").hasChild("sidecloth_l2")) {
            parts.put("sidecloth_l2", root.getChild("left_leg").getChild("sidecloth_l2"));
        }
        if (root.getChild("left_leg").hasChild("sidecloth_l3")) {
            parts.put("sidecloth_l3", root.getChild("left_leg").getChild("sidecloth_l3"));
        }
        return parts;
    }

    public static LayerDefinition fortress(float f) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        head.addOrReplaceChild("ornament_l", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(1.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_l2", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(3.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_r", CubeListBuilder.create().texOffs(78, 8).addBox(-3.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_r2", CubeListBuilder.create().texOffs(78, 8).addBox(-4.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 4.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet_r", CubeListBuilder.create().texOffs(21, 13).addBox(-6.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5235987755982988F));
        head.addOrReplaceChild("helmet_l", CubeListBuilder.create().texOffs(21, 13).mirror().addBox(5.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5235987755982988F));
        head.addOrReplaceChild("helmet_b", CubeListBuilder.create().texOffs(41, 21).addBox(-4.5F, -3.0F, 5.5F, 9.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5235987755982988F, 0.0F, 0.0F));
        head.addOrReplaceChild("capsthingy", CubeListBuilder.create().texOffs(21, 0).addBox(-4.5F, -6.0F, -6.5F, 9.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("flap_r", CubeListBuilder.create().texOffs(59, 10).addBox(-10.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5235987755982988F, 0.5235987755982988F));
        head.addOrReplaceChild("flap_l", CubeListBuilder.create().texOffs(59, 10).mirror().addBox(7.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5235987755982988F, -0.5235987755982988F));
        head.addOrReplaceChild("gemornament", CubeListBuilder.create().texOffs(68, 11).addBox(-1.5F, -9.0F, -7.0F, 3.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("gem", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, -8.5F, -7.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(100, 18).addBox(-4.5F, -5.0F, -4.25F, 9.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("mask0", CubeListBuilder.create().texOffs(52, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("mask1", CubeListBuilder.create().texOffs(76, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("mask2", CubeListBuilder.create().texOffs(100, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntlet_r", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntletstrap_r1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntletstrap_r2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulderplate_rtop", CubeListBuilder.create().texOffs(110, 37).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r1", CubeListBuilder.create().texOffs(110, 45).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        leftArm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntlet_l", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntletstrapl1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntletstrap_l2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulderplate_ltop", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l1", CubeListBuilder.create().texOffs(110, 45).mirror().addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l2", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l3", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        rightLeg.addOrReplaceChild("legpanel_r1", CubeListBuilder.create().texOffs(0, 51).addBox(-1.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r2", CubeListBuilder.create().texOffs(8, 51).addBox(-1.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r3", CubeListBuilder.create().texOffs(0, 56).addBox(-1.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("sidepanel_r2", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("sidepanel_r3", CubeListBuilder.create().texOffs(12, 31).addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanel_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanel_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanel_l3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l1", CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l2", CubeListBuilder.create().texOffs(8, 51).mirror().addBox(-2.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l3", CubeListBuilder.create().texOffs(0, 56).mirror().addBox(-2.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l4", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l5", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l6", CubeListBuilder.create().texOffs(6, 43).mirror().addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("sidepanel_l2", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(0.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("sidepanel_l3", CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanel_l2", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        if (f < 1.0F) {
            body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        }
        if (f >= 1.0F) {
            body.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(34, 27).addBox(-2.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1919862F));
            body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("book", CubeListBuilder.create().texOffs(100, 8).addBox(1.0F, -0.3F, 4.0F, 5.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7679449F));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static Map<String, ModelPart> fortressParts(ModelPart root) {
        Map<String, ModelPart> parts = new HashMap<>();
        if (root.getChild("head").hasChild("ornament_l")) {
            parts.put("ornament_l", root.getChild("head").getChild("ornament_l"));
        }
        if (root.getChild("head").hasChild("ornament_l2")) {
            parts.put("ornament_l2", root.getChild("head").getChild("ornament_l2"));
        }
        if (root.getChild("head").hasChild("ornament_r")) {
            parts.put("ornament_r", root.getChild("head").getChild("ornament_r"));
        }
        if (root.getChild("head").hasChild("ornament_r2")) {
            parts.put("ornament_r2", root.getChild("head").getChild("ornament_r2"));
        }
        if (root.getChild("head").hasChild("helmet")) {
            parts.put("helmet", root.getChild("head").getChild("helmet"));
        }
        if (root.getChild("head").hasChild("helmet_r")) {
            parts.put("helmet_r", root.getChild("head").getChild("helmet_r"));
        }
        if (root.getChild("head").hasChild("helmet_l")) {
            parts.put("helmet_l", root.getChild("head").getChild("helmet_l"));
        }
        if (root.getChild("head").hasChild("helmet_b")) {
            parts.put("helmet_b", root.getChild("head").getChild("helmet_b"));
        }
        if (root.getChild("head").hasChild("capsthingy")) {
            parts.put("capsthingy", root.getChild("head").getChild("capsthingy"));
        }
        if (root.getChild("head").hasChild("flap_r")) {
            parts.put("flap_r", root.getChild("head").getChild("flap_r"));
        }
        if (root.getChild("head").hasChild("flap_l")) {
            parts.put("flap_l", root.getChild("head").getChild("flap_l"));
        }
        if (root.getChild("head").hasChild("gemornament")) {
            parts.put("gemornament", root.getChild("head").getChild("gemornament"));
        }
        if (root.getChild("head").hasChild("gem")) {
            parts.put("gem", root.getChild("head").getChild("gem"));
        }
        if (root.getChild("head").hasChild("goggles")) {
            parts.put("goggles", root.getChild("head").getChild("goggles"));
        }
        if (root.getChild("head").hasChild("mask0")) {
            parts.put("mask0", root.getChild("head").getChild("mask0"));
        }
        if (root.getChild("head").hasChild("mask1")) {
            parts.put("mask1", root.getChild("head").getChild("mask1"));
        }
        if (root.getChild("head").hasChild("mask2")) {
            parts.put("mask2", root.getChild("head").getChild("mask2"));
        }
        if (root.getChild("body").hasChild("mbelt")) {
            parts.put("mbelt", root.getChild("body").getChild("mbelt"));
        }
        if (root.getChild("body").hasChild("mbelt_l")) {
            parts.put("mbelt_l", root.getChild("body").getChild("mbelt_l"));
        }
        if (root.getChild("body").hasChild("mbelt_r")) {
            parts.put("mbelt_r", root.getChild("body").getChild("mbelt_r"));
        }
        if (root.getChild("body").hasChild("belt_r")) {
            parts.put("belt_r", root.getChild("body").getChild("belt_r"));
        }
        if (root.getChild("body").hasChild("belt_l")) {
            parts.put("belt_l", root.getChild("body").getChild("belt_l"));
        }
        if (root.getChild("body").hasChild("chestplate")) {
            parts.put("chestplate", root.getChild("body").getChild("chestplate"));
        }
        if (root.getChild("body").hasChild("scroll")) {
            parts.put("scroll", root.getChild("body").getChild("scroll"));
        }
        if (root.getChild("body").hasChild("backplate")) {
            parts.put("backplate", root.getChild("body").getChild("backplate"));
        }
        if (root.getChild("body").hasChild("book")) {
            parts.put("book", root.getChild("body").getChild("book"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r")) {
            parts.put("shoulder_r", root.getChild("right_arm").getChild("shoulder_r"));
        }
        if (root.getChild("right_arm").hasChild("gauntlet_r")) {
            parts.put("gauntlet_r", root.getChild("right_arm").getChild("gauntlet_r"));
        }
        if (root.getChild("right_arm").hasChild("gauntletstrap_r1")) {
            parts.put("gauntletstrap_r1", root.getChild("right_arm").getChild("gauntletstrap_r1"));
        }
        if (root.getChild("right_arm").hasChild("gauntletstrap_r2")) {
            parts.put("gauntletstrap_r2", root.getChild("right_arm").getChild("gauntletstrap_r2"));
        }
        if (root.getChild("right_arm").hasChild("shoulderplate_rtop")) {
            parts.put("shoulderplate_rtop", root.getChild("right_arm").getChild("shoulderplate_rtop"));
        }
        if (root.getChild("right_arm").hasChild("shoulderplate_r1")) {
            parts.put("shoulderplate_r1", root.getChild("right_arm").getChild("shoulderplate_r1"));
        }
        if (root.getChild("right_arm").hasChild("shoulderplate_r2")) {
            parts.put("shoulderplate_r2", root.getChild("right_arm").getChild("shoulderplate_r2"));
        }
        if (root.getChild("right_arm").hasChild("shoulderplate_r3")) {
            parts.put("shoulderplate_r3", root.getChild("right_arm").getChild("shoulderplate_r3"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l")) {
            parts.put("shoulder_l", root.getChild("left_arm").getChild("shoulder_l"));
        }
        if (root.getChild("left_arm").hasChild("gauntlet_l")) {
            parts.put("gauntlet_l", root.getChild("left_arm").getChild("gauntlet_l"));
        }
        if (root.getChild("left_arm").hasChild("gauntletstrapl1")) {
            parts.put("gauntletstrapl1", root.getChild("left_arm").getChild("gauntletstrapl1"));
        }
        if (root.getChild("left_arm").hasChild("gauntletstrap_l2")) {
            parts.put("gauntletstrap_l2", root.getChild("left_arm").getChild("gauntletstrap_l2"));
        }
        if (root.getChild("left_arm").hasChild("shoulderplate_ltop")) {
            parts.put("shoulderplate_ltop", root.getChild("left_arm").getChild("shoulderplate_ltop"));
        }
        if (root.getChild("left_arm").hasChild("shoulderplate_l1")) {
            parts.put("shoulderplate_l1", root.getChild("left_arm").getChild("shoulderplate_l1"));
        }
        if (root.getChild("left_arm").hasChild("shoulderplate_l2")) {
            parts.put("shoulderplate_l2", root.getChild("left_arm").getChild("shoulderplate_l2"));
        }
        if (root.getChild("left_arm").hasChild("shoulderplate_l3")) {
            parts.put("shoulderplate_l3", root.getChild("left_arm").getChild("shoulderplate_l3"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r1")) {
            parts.put("legpanel_r1", root.getChild("right_leg").getChild("legpanel_r1"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r2")) {
            parts.put("legpanel_r2", root.getChild("right_leg").getChild("legpanel_r2"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r3")) {
            parts.put("legpanel_r3", root.getChild("right_leg").getChild("legpanel_r3"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r4")) {
            parts.put("legpanel_r4", root.getChild("right_leg").getChild("legpanel_r4"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r5")) {
            parts.put("legpanel_r5", root.getChild("right_leg").getChild("legpanel_r5"));
        }
        if (root.getChild("right_leg").hasChild("legpanel_r6")) {
            parts.put("legpanel_r6", root.getChild("right_leg").getChild("legpanel_r6"));
        }
        if (root.getChild("right_leg").hasChild("sidepanel_r1")) {
            parts.put("sidepanel_r1", root.getChild("right_leg").getChild("sidepanel_r1"));
        }
        if (root.getChild("right_leg").hasChild("sidepanel_r2")) {
            parts.put("sidepanel_r2", root.getChild("right_leg").getChild("sidepanel_r2"));
        }
        if (root.getChild("right_leg").hasChild("sidepanel_r3")) {
            parts.put("sidepanel_r3", root.getChild("right_leg").getChild("sidepanel_r3"));
        }
        if (root.getChild("right_leg").hasChild("backpanel_r1")) {
            parts.put("backpanel_r1", root.getChild("right_leg").getChild("backpanel_r1"));
        }
        if (root.getChild("right_leg").hasChild("backpanel_r2")) {
            parts.put("backpanel_r2", root.getChild("right_leg").getChild("backpanel_r2"));
        }
        if (root.getChild("right_leg").hasChild("backpanel_r3")) {
            parts.put("backpanel_r3", root.getChild("right_leg").getChild("backpanel_r3"));
        }
        if (root.getChild("left_leg").hasChild("backpanel_l3")) {
            parts.put("backpanel_l3", root.getChild("left_leg").getChild("backpanel_l3"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l1")) {
            parts.put("legpanel_l1", root.getChild("left_leg").getChild("legpanel_l1"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l2")) {
            parts.put("legpanel_l2", root.getChild("left_leg").getChild("legpanel_l2"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l3")) {
            parts.put("legpanel_l3", root.getChild("left_leg").getChild("legpanel_l3"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l4")) {
            parts.put("legpanel_l4", root.getChild("left_leg").getChild("legpanel_l4"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l5")) {
            parts.put("legpanel_l5", root.getChild("left_leg").getChild("legpanel_l5"));
        }
        if (root.getChild("left_leg").hasChild("legpanel_l6")) {
            parts.put("legpanel_l6", root.getChild("left_leg").getChild("legpanel_l6"));
        }
        if (root.getChild("left_leg").hasChild("sidepanel_l1")) {
            parts.put("sidepanel_l1", root.getChild("left_leg").getChild("sidepanel_l1"));
        }
        if (root.getChild("left_leg").hasChild("sidepanel_l2")) {
            parts.put("sidepanel_l2", root.getChild("left_leg").getChild("sidepanel_l2"));
        }
        if (root.getChild("left_leg").hasChild("sidepanel_l3")) {
            parts.put("sidepanel_l3", root.getChild("left_leg").getChild("sidepanel_l3"));
        }
        if (root.getChild("left_leg").hasChild("backpanel_l1")) {
            parts.put("backpanel_l1", root.getChild("left_leg").getChild("backpanel_l1"));
        }
        if (root.getChild("left_leg").hasChild("backpanel_l2")) {
            parts.put("backpanel_l2", root.getChild("left_leg").getChild("backpanel_l2"));
        }
        return parts;
    }

    public static LayerDefinition knight(float f) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulder_r0", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        rightArm.addOrReplaceChild("shoulder_r1", CubeListBuilder.create().texOffs(0, 19).addBox(-3.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        rightArm.addOrReplaceChild("shoulder_r2", CubeListBuilder.create().texOffs(0, 11).addBox(-2.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        rightArm.addOrReplaceChild("gauntlet_r", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntletstrap_r1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntletstrap_r2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulder_l0", CubeListBuilder.create().texOffs(0, 0).addBox(1.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        leftArm.addOrReplaceChild("shoulder_l1", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        leftArm.addOrReplaceChild("shoulder_l2", CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        leftArm.addOrReplaceChild("gauntlet_l", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntletstrap_l1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntletstrap_l2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanel_r0", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        rightLeg.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(96, 7).mirror().addBox(0.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        rightLeg.addOrReplaceChild("sidepanel_r2", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        rightLeg.addOrReplaceChild("sidepanel_r3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        leftLeg.addOrReplaceChild("sidepanel_l0", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        leftLeg.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(96, 7).addBox(-2.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        leftLeg.addOrReplaceChild("sidepanel_l2", CubeListBuilder.create().texOffs(114, 5).addBox(0.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        leftLeg.addOrReplaceChild("sidepanel_l3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        if (f >= 1.0F) {
            body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("frontcloth1", CubeListBuilder.create().texOffs(120, 39).addBox(0.0F, 0.0F, 0.0F, 6.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(-3.0F, 11.0F, -3.5F, -0.1047198F, 0.0F, 0.0F));
            body.addOrReplaceChild("frontcloth2", CubeListBuilder.create().texOffs(100, 37).addBox(0.0F, 7.5F, 1.8F, 6.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(-3.0F, 11.0F, -3.5F, -0.3316126F, 0.0F, 0.0F));
            body.addOrReplaceChild("tabbard", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 1.2F, -3.5F, 6.0F, 10.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(0.0F, 0.0F, 0.0F, 9.0F, 12.0F, 1.0F), PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.1396263F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 11.7F, -2.0F, 9.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.3069452F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 15.2F, -4.2F, 9.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.4465716F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak_at_l", CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, 2.0F, 2.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396263F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak_at_r", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, 2.0F, 2.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396263F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static Map<String, ModelPart> knightParts(ModelPart root) {
        Map<String, ModelPart> parts = new HashMap<>();
        if (root.getChild("head").hasChild("helmet")) {
            parts.put("helmet", root.getChild("head").getChild("helmet"));
        }
        if (root.getChild("body").hasChild("mbelt")) {
            parts.put("mbelt", root.getChild("body").getChild("mbelt"));
        }
        if (root.getChild("body").hasChild("mbelt_l")) {
            parts.put("mbelt_l", root.getChild("body").getChild("mbelt_l"));
        }
        if (root.getChild("body").hasChild("mbelt_r")) {
            parts.put("mbelt_r", root.getChild("body").getChild("mbelt_r"));
        }
        if (root.getChild("body").hasChild("chestplate")) {
            parts.put("chestplate", root.getChild("body").getChild("chestplate"));
        }
        if (root.getChild("body").hasChild("frontcloth1")) {
            parts.put("frontcloth1", root.getChild("body").getChild("frontcloth1"));
        }
        if (root.getChild("body").hasChild("frontcloth2")) {
            parts.put("frontcloth2", root.getChild("body").getChild("frontcloth2"));
        }
        if (root.getChild("body").hasChild("tabbard")) {
            parts.put("tabbard", root.getChild("body").getChild("tabbard"));
        }
        if (root.getChild("body").hasChild("backplate")) {
            parts.put("backplate", root.getChild("body").getChild("backplate"));
        }
        if (root.getChild("body").hasChild("cloak1")) {
            parts.put("cloak1", root.getChild("body").getChild("cloak1"));
        }
        if (root.getChild("body").hasChild("cloak2")) {
            parts.put("cloak2", root.getChild("body").getChild("cloak2"));
        }
        if (root.getChild("body").hasChild("cloak3")) {
            parts.put("cloak3", root.getChild("body").getChild("cloak3"));
        }
        if (root.getChild("body").hasChild("cloak_at_l")) {
            parts.put("cloak_at_l", root.getChild("body").getChild("cloak_at_l"));
        }
        if (root.getChild("body").hasChild("cloak_at_r")) {
            parts.put("cloak_at_r", root.getChild("body").getChild("cloak_at_r"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r")) {
            parts.put("shoulder_r", root.getChild("right_arm").getChild("shoulder_r"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r0")) {
            parts.put("shoulder_r0", root.getChild("right_arm").getChild("shoulder_r0"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r1")) {
            parts.put("shoulder_r1", root.getChild("right_arm").getChild("shoulder_r1"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r2")) {
            parts.put("shoulder_r2", root.getChild("right_arm").getChild("shoulder_r2"));
        }
        if (root.getChild("right_arm").hasChild("gauntlet_r")) {
            parts.put("gauntlet_r", root.getChild("right_arm").getChild("gauntlet_r"));
        }
        if (root.getChild("right_arm").hasChild("gauntletstrap_r1")) {
            parts.put("gauntletstrap_r1", root.getChild("right_arm").getChild("gauntletstrap_r1"));
        }
        if (root.getChild("right_arm").hasChild("gauntletstrap_r2")) {
            parts.put("gauntletstrap_r2", root.getChild("right_arm").getChild("gauntletstrap_r2"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l")) {
            parts.put("shoulder_l", root.getChild("left_arm").getChild("shoulder_l"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l0")) {
            parts.put("shoulder_l0", root.getChild("left_arm").getChild("shoulder_l0"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l1")) {
            parts.put("shoulder_l1", root.getChild("left_arm").getChild("shoulder_l1"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l2")) {
            parts.put("shoulder_l2", root.getChild("left_arm").getChild("shoulder_l2"));
        }
        if (root.getChild("left_arm").hasChild("gauntlet_l")) {
            parts.put("gauntlet_l", root.getChild("left_arm").getChild("gauntlet_l"));
        }
        if (root.getChild("left_arm").hasChild("gauntletstrap_l1")) {
            parts.put("gauntletstrap_l1", root.getChild("left_arm").getChild("gauntletstrap_l1"));
        }
        if (root.getChild("left_arm").hasChild("gauntletstrap_l2")) {
            parts.put("gauntletstrap_l2", root.getChild("left_arm").getChild("gauntletstrap_l2"));
        }
        if (root.getChild("right_leg").hasChild("sidepanel_r0")) {
            parts.put("sidepanel_r0", root.getChild("right_leg").getChild("sidepanel_r0"));
        }
        if (root.getChild("right_leg").hasChild("sidepanel_r1")) {
            parts.put("sidepanel_r1", root.getChild("right_leg").getChild("sidepanel_r1"));
        }
        if (root.getChild("right_leg").hasChild("sidepanel_r2")) {
            parts.put("sidepanel_r2", root.getChild("right_leg").getChild("sidepanel_r2"));
        }
        if (root.getChild("right_leg").hasChild("sidepanel_r3")) {
            parts.put("sidepanel_r3", root.getChild("right_leg").getChild("sidepanel_r3"));
        }
        if (root.getChild("left_leg").hasChild("sidepanel_l0")) {
            parts.put("sidepanel_l0", root.getChild("left_leg").getChild("sidepanel_l0"));
        }
        if (root.getChild("left_leg").hasChild("sidepanel_l1")) {
            parts.put("sidepanel_l1", root.getChild("left_leg").getChild("sidepanel_l1"));
        }
        if (root.getChild("left_leg").hasChild("sidepanel_l2")) {
            parts.put("sidepanel_l2", root.getChild("left_leg").getChild("sidepanel_l2"));
        }
        if (root.getChild("left_leg").hasChild("sidepanel_l3")) {
            parts.put("sidepanel_l3", root.getChild("left_leg").getChild("sidepanel_l3"));
        }
        return parts;
    }

    public static LayerDefinition leader(float f) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulder_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        rightArm.addOrReplaceChild("shoulder_r2", CubeListBuilder.create().texOffs(0, 19).addBox(-3.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        rightArm.addOrReplaceChild("shoulder_r3", CubeListBuilder.create().texOffs(0, 11).addBox(-2.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        rightArm.addOrReplaceChild("shoulder_r4", CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, -4.0F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        rightArm.addOrReplaceChild("shoulder_r5", CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, 3.0F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        rightArm.addOrReplaceChild("gauntlet_r", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntlet_r2", CubeListBuilder.create().texOffs(102, 37).addBox(-5.0F, 3.5F, -2.0F, 1.0F, 5.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1675516F));
        rightArm.addOrReplaceChild("gauntletstrap_r1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("gauntletstrap_r2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulder_l1", CubeListBuilder.create().texOffs(0, 0).addBox(1.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        leftArm.addOrReplaceChild("shoulder_l2", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        leftArm.addOrReplaceChild("shoulder_l3", CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        leftArm.addOrReplaceChild("shoulder_l4", CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, -4.0F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        leftArm.addOrReplaceChild("shoulder_l5", CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, 3.0F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        leftArm.addOrReplaceChild("gauntlet_l", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntlet_l2", CubeListBuilder.create().texOffs(102, 37).addBox(4.0F, 3.5F, -2.0F, 1.0F, 5.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1675516F));
        leftArm.addOrReplaceChild("gauntletstrap_l1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("gauntletstrap_l2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -0.5F, 2.5F, 5.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanel_r2", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        rightLeg.addOrReplaceChild("backpanel_r3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        rightLeg.addOrReplaceChild("backpanel_r4", CubeListBuilder.create().texOffs(0, 25).mirror().addBox(-3.0F, -0.5F, -3.5F, 5.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -0.5F, 2.5F, 5.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanel_l2", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        leftLeg.addOrReplaceChild("backpanel_l3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        leftLeg.addOrReplaceChild("backpanel_l4", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -0.5F, -3.5F, 5.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F, 0.0F));
        if (f >= 1.0F) {
            body.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.8F, 8.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("chest_ornament", CubeListBuilder.create().texOffs(76, 53).addBox(-2.5F, 3.0F, -4.8F, 5.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("chest_cloth_r", CubeListBuilder.create().texOffs(20, 47).addBox(-4.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0663225F, 0.0F, 0.0F));
            body.addOrReplaceChild("chest_cloth_l", CubeListBuilder.create().texOffs(20, 47).mirror().addBox(1.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0663225F, 0.0F, 0.0F));
            body.addOrReplaceChild("leg_cloth_r", CubeListBuilder.create().texOffs(20, 55).addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(-4.5F, 10.4F, -3.9F, -0.0349066F, 0.0F, 0.0F));
            body.addOrReplaceChild("leg_cloth_l", CubeListBuilder.create().texOffs(20, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(1.5F, 10.4F, -3.9F, -0.0349066F, 0.0F, 0.0F));
            body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            body.addOrReplaceChild("collar_b", CubeListBuilder.create().texOffs(17, 26).addBox(-4.5F, -1.5F, 7.0F, 9.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F));
            body.addOrReplaceChild("collar_r", CubeListBuilder.create().texOffs(17, 11).addBox(-5.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F));
            body.addOrReplaceChild("collar_l", CubeListBuilder.create().texOffs(17, 11).addBox(4.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F));
            body.addOrReplaceChild("collar_f", CubeListBuilder.create().texOffs(17, 31).addBox(-4.5F, -1.5F, -3.0F, 9.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-4.5F, 2.0F, 1.0F, 9.0F, 12.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 14.0F, -1.3F, 9.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.3069452F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 17.0F, -3.7F, 9.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.4465716F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak_tl", CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));
            body.addOrReplaceChild("cloak_tr", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static Map<String, ModelPart> leaderParts(ModelPart root) {
        Map<String, ModelPart> parts = new HashMap<>();
        if (root.getChild("head").hasChild("helmet")) {
            parts.put("helmet", root.getChild("head").getChild("helmet"));
        }
        if (root.getChild("body").hasChild("mbelt")) {
            parts.put("mbelt", root.getChild("body").getChild("mbelt"));
        }
        if (root.getChild("body").hasChild("mbelt_l")) {
            parts.put("mbelt_l", root.getChild("body").getChild("mbelt_l"));
        }
        if (root.getChild("body").hasChild("mbelt_r")) {
            parts.put("mbelt_r", root.getChild("body").getChild("mbelt_r"));
        }
        if (root.getChild("body").hasChild("belt_l")) {
            parts.put("belt_l", root.getChild("body").getChild("belt_l"));
        }
        if (root.getChild("body").hasChild("belt_r")) {
            parts.put("belt_r", root.getChild("body").getChild("belt_r"));
        }
        if (root.getChild("body").hasChild("chestplate")) {
            parts.put("chestplate", root.getChild("body").getChild("chestplate"));
        }
        if (root.getChild("body").hasChild("chest_ornament")) {
            parts.put("chest_ornament", root.getChild("body").getChild("chest_ornament"));
        }
        if (root.getChild("body").hasChild("chest_cloth_r")) {
            parts.put("chest_cloth_r", root.getChild("body").getChild("chest_cloth_r"));
        }
        if (root.getChild("body").hasChild("chest_cloth_l")) {
            parts.put("chest_cloth_l", root.getChild("body").getChild("chest_cloth_l"));
        }
        if (root.getChild("body").hasChild("leg_cloth_r")) {
            parts.put("leg_cloth_r", root.getChild("body").getChild("leg_cloth_r"));
        }
        if (root.getChild("body").hasChild("leg_cloth_l")) {
            parts.put("leg_cloth_l", root.getChild("body").getChild("leg_cloth_l"));
        }
        if (root.getChild("body").hasChild("backplate")) {
            parts.put("backplate", root.getChild("body").getChild("backplate"));
        }
        if (root.getChild("body").hasChild("collar_b")) {
            parts.put("collar_b", root.getChild("body").getChild("collar_b"));
        }
        if (root.getChild("body").hasChild("collar_r")) {
            parts.put("collar_r", root.getChild("body").getChild("collar_r"));
        }
        if (root.getChild("body").hasChild("collar_l")) {
            parts.put("collar_l", root.getChild("body").getChild("collar_l"));
        }
        if (root.getChild("body").hasChild("collar_f")) {
            parts.put("collar_f", root.getChild("body").getChild("collar_f"));
        }
        if (root.getChild("body").hasChild("cloak1")) {
            parts.put("cloak1", root.getChild("body").getChild("cloak1"));
        }
        if (root.getChild("body").hasChild("cloak2")) {
            parts.put("cloak2", root.getChild("body").getChild("cloak2"));
        }
        if (root.getChild("body").hasChild("cloak3")) {
            parts.put("cloak3", root.getChild("body").getChild("cloak3"));
        }
        if (root.getChild("body").hasChild("cloak_tl")) {
            parts.put("cloak_tl", root.getChild("body").getChild("cloak_tl"));
        }
        if (root.getChild("body").hasChild("cloak_tr")) {
            parts.put("cloak_tr", root.getChild("body").getChild("cloak_tr"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r")) {
            parts.put("shoulder_r", root.getChild("right_arm").getChild("shoulder_r"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r1")) {
            parts.put("shoulder_r1", root.getChild("right_arm").getChild("shoulder_r1"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r2")) {
            parts.put("shoulder_r2", root.getChild("right_arm").getChild("shoulder_r2"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r3")) {
            parts.put("shoulder_r3", root.getChild("right_arm").getChild("shoulder_r3"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r4")) {
            parts.put("shoulder_r4", root.getChild("right_arm").getChild("shoulder_r4"));
        }
        if (root.getChild("right_arm").hasChild("shoulder_r5")) {
            parts.put("shoulder_r5", root.getChild("right_arm").getChild("shoulder_r5"));
        }
        if (root.getChild("right_arm").hasChild("gauntlet_r")) {
            parts.put("gauntlet_r", root.getChild("right_arm").getChild("gauntlet_r"));
        }
        if (root.getChild("right_arm").hasChild("gauntlet_r2")) {
            parts.put("gauntlet_r2", root.getChild("right_arm").getChild("gauntlet_r2"));
        }
        if (root.getChild("right_arm").hasChild("gauntletstrap_r1")) {
            parts.put("gauntletstrap_r1", root.getChild("right_arm").getChild("gauntletstrap_r1"));
        }
        if (root.getChild("right_arm").hasChild("gauntletstrap_r2")) {
            parts.put("gauntletstrap_r2", root.getChild("right_arm").getChild("gauntletstrap_r2"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l")) {
            parts.put("shoulder_l", root.getChild("left_arm").getChild("shoulder_l"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l1")) {
            parts.put("shoulder_l1", root.getChild("left_arm").getChild("shoulder_l1"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l2")) {
            parts.put("shoulder_l2", root.getChild("left_arm").getChild("shoulder_l2"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l3")) {
            parts.put("shoulder_l3", root.getChild("left_arm").getChild("shoulder_l3"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l4")) {
            parts.put("shoulder_l4", root.getChild("left_arm").getChild("shoulder_l4"));
        }
        if (root.getChild("left_arm").hasChild("shoulder_l5")) {
            parts.put("shoulder_l5", root.getChild("left_arm").getChild("shoulder_l5"));
        }
        if (root.getChild("left_arm").hasChild("gauntlet_l")) {
            parts.put("gauntlet_l", root.getChild("left_arm").getChild("gauntlet_l"));
        }
        if (root.getChild("left_arm").hasChild("gauntlet_l2")) {
            parts.put("gauntlet_l2", root.getChild("left_arm").getChild("gauntlet_l2"));
        }
        if (root.getChild("left_arm").hasChild("gauntletstrap_l1")) {
            parts.put("gauntletstrap_l1", root.getChild("left_arm").getChild("gauntletstrap_l1"));
        }
        if (root.getChild("left_arm").hasChild("gauntletstrap_l2")) {
            parts.put("gauntletstrap_l2", root.getChild("left_arm").getChild("gauntletstrap_l2"));
        }
        if (root.getChild("right_leg").hasChild("backpanel_r1")) {
            parts.put("backpanel_r1", root.getChild("right_leg").getChild("backpanel_r1"));
        }
        if (root.getChild("right_leg").hasChild("backpanel_r2")) {
            parts.put("backpanel_r2", root.getChild("right_leg").getChild("backpanel_r2"));
        }
        if (root.getChild("right_leg").hasChild("backpanel_r3")) {
            parts.put("backpanel_r3", root.getChild("right_leg").getChild("backpanel_r3"));
        }
        if (root.getChild("right_leg").hasChild("backpanel_r4")) {
            parts.put("backpanel_r4", root.getChild("right_leg").getChild("backpanel_r4"));
        }
        if (root.getChild("left_leg").hasChild("backpanel_l1")) {
            parts.put("backpanel_l1", root.getChild("left_leg").getChild("backpanel_l1"));
        }
        if (root.getChild("left_leg").hasChild("backpanel_l2")) {
            parts.put("backpanel_l2", root.getChild("left_leg").getChild("backpanel_l2"));
        }
        if (root.getChild("left_leg").hasChild("backpanel_l3")) {
            parts.put("backpanel_l3", root.getChild("left_leg").getChild("backpanel_l3"));
        }
        if (root.getChild("left_leg").hasChild("backpanel_l4")) {
            parts.put("backpanel_l4", root.getChild("left_leg").getChild("backpanel_l4"));
        }
        return parts;
    }
}
