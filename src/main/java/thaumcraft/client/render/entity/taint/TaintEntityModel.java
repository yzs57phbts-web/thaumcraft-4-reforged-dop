package thaumcraft.client.render.entity.taint;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entity.monster.taint.Taintacle;
import thaumcraft.entity.monster.taint.TaintSpore;
import thaumcraft.entity.monster.taint.TaintedChicken;
import thaumcraft.entity.monster.taint.TaintedSheep;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.client.platform.TcRenderTypes;

public class TaintEntityModel<T extends Mob> extends EntityModel<T> {
    public enum Kind {
        SPIDER("taint_spider", "textures/model/taint_spider.png"),
        TENTACLE("taintacle", "textures/model/taintacle.png"),
        TENTACLE_TINY("taintacle_small", "textures/model/taintacle.png"),
        SPORE("taint_spore", "textures/model/taint_spore.png"),
        SWARMER("taint_spore_swarmer", "textures/model/taint_spore.png"),
        CHICKEN("tainted_chicken", "textures/model/chicken.png"),
        COW("tainted_cow", "textures/model/cow.png"),
        CREEPER("tainted_creeper", "textures/model/creeper.png"),
        PIG("tainted_pig", "textures/model/pig.png"),
        SHEEP("tainted_sheep", "textures/model/sheep.png"),
        VILLAGER("tainted_villager", "textures/model/villager.png");

        private final ModelLayerLocation layer;
        private final ResourceLocation texture;

        Kind(String layer, String texture) {
            this.layer = new ModelLayerLocation(Thaumcraft.id(layer), "main");
            this.texture = Thaumcraft.id(texture);
        }

        public ModelLayerLocation layer() {
            return this.layer;
        }

        public ResourceLocation texture() {
            return this.texture;
        }
    }

    private final ModelPart root;
    private final Kind kind;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart rearLeftLeg;
    private final ModelPart rearRightLeg;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final List<ModelPart> spiderLegs = new ArrayList<>();
    private final List<ModelPart> tentacleSegments = new ArrayList<>();

    public TaintEntityModel(ModelPart root, Kind kind) {
        super(kind == Kind.SPORE || kind == Kind.SWARMER ? RenderType::entityTranslucent : TcRenderTypes::entityCutoutNoCull);
        this.root = root;
        this.kind = kind;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.rearLeftLeg = root.getChild("rear_left_leg");
        this.rearRightLeg = root.getChild("rear_right_leg");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        if (kind == Kind.SPIDER) {
            for (int index = 0; index < 4; index++) {
                this.spiderLegs.add(this.leftLeg.getChild("leg_" + index));
                this.spiderLegs.add(this.rightLeg.getChild("leg_" + index));
            }
        }
        if (kind == Kind.TENTACLE || kind == Kind.TENTACLE_TINY) {
            ModelPart part = root.getChild("tentacle_base");
            int segmentCount = kind == Kind.TENTACLE ? 9 : 5;
            for (int index = 0; index < segmentCount; index++) {
                part = part.getChild("tentacle_" + index);
                this.tentacleSegments.add(part);
            }
        }
    }

    private static LayerDefinition createBodyLayer(Kind kind) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose bodyPose = PartPose.ZERO;
        PartPose headPose = PartPose.ZERO;
        PartPose leftLegPose = PartPose.ZERO;
        PartPose rightLegPose = PartPose.ZERO;
        PartPose rearLeftLegPose = PartPose.ZERO;
        PartPose rearRightLegPose = PartPose.ZERO;
        PartPose leftArmPose = PartPose.ZERO;
        PartPose rightArmPose = PartPose.ZERO;
        PartPose leftWingPose = PartPose.ZERO;
        PartPose rightWingPose = PartPose.ZERO;
        switch (kind) {
            case SPIDER -> {
                bodyPose = PartPose.offset(0.0F, 15.0F, 0.0F);
                headPose = PartPose.offset(0.0F, 15.0F, -3.0F);
                leftLegPose = PartPose.offset(-4.0F, 15.0F, 0.0F);
                rightLegPose = PartPose.offset(4.0F, 15.0F, 0.0F);
            }
            case SPORE -> bodyPose = PartPose.offset(0.0F, 24.0F, 0.0F);
            case CHICKEN -> {
                bodyPose = PartPose.offset(0.0F, 16.0F, 0.0F);
                headPose = PartPose.offset(0.0F, 15.0F, -4.0F);
                leftLegPose = PartPose.offset(-2.0F, 19.0F, 1.0F);
                rightLegPose = PartPose.offset(2.0F, 19.0F, 1.0F);
                leftWingPose = PartPose.offset(4.0F, 13.0F, 0.0F);
                rightWingPose = PartPose.offset(-4.0F, 13.0F, 0.0F);
            }
            case COW -> {
                bodyPose = PartPose.offset(0.0F, 5.0F, 2.0F);
                headPose = PartPose.offset(0.0F, 4.0F, -8.0F);
                leftLegPose = PartPose.offset(-3.0F, 12.0F, 7.0F);
                rightLegPose = PartPose.offset(3.0F, 12.0F, 7.0F);
                rearLeftLegPose = PartPose.offset(-3.0F, 12.0F, -5.0F);
                rearRightLegPose = PartPose.offset(3.0F, 12.0F, -5.0F);
            }
            case PIG -> {
                bodyPose = PartPose.offset(0.0F, 11.0F, 2.0F);
                headPose = PartPose.offset(0.0F, 12.0F, -6.0F);
                leftLegPose = PartPose.offset(-3.0F, 18.0F, 7.0F);
                rightLegPose = PartPose.offset(3.0F, 18.0F, 7.0F);
                rearLeftLegPose = PartPose.offset(-3.0F, 18.0F, -5.0F);
                rearRightLegPose = PartPose.offset(3.0F, 18.0F, -5.0F);
            }
            case SHEEP -> {
                bodyPose = PartPose.offset(0.0F, 5.0F, 2.0F);
                headPose = PartPose.offset(0.0F, 6.0F, -8.0F);
                leftLegPose = PartPose.offset(-3.0F, 12.0F, 7.0F);
                rightLegPose = PartPose.offset(3.0F, 12.0F, 7.0F);
                rearLeftLegPose = PartPose.offset(-3.0F, 12.0F, -5.0F);
                rearRightLegPose = PartPose.offset(3.0F, 12.0F, -5.0F);
            }
            case CREEPER -> {
                bodyPose = PartPose.offset(0.0F, 6.0F, 0.0F);
                headPose = PartPose.offset(0.0F, 6.0F, 0.0F);
                leftLegPose = PartPose.offset(-2.0F, 18.0F, 4.0F);
                rightLegPose = PartPose.offset(2.0F, 18.0F, 4.0F);
                rearLeftLegPose = PartPose.offset(-2.0F, 18.0F, -4.0F);
                rearRightLegPose = PartPose.offset(2.0F, 18.0F, -4.0F);
            }
            case VILLAGER -> {
                leftLegPose = PartPose.offset(2.0F, 12.0F, 0.0F);
                rightLegPose = PartPose.offset(-2.0F, 12.0F, 0.0F);
                leftArmPose = PartPose.offset(0.0F, 3.0F, -1.0F);
                rightArmPose = PartPose.offset(0.0F, 3.0F, -1.0F);
            }
            default -> {
            }
        }
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), bodyPose);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), headPose);
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), leftLegPose);
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), rightLegPose);
        PartDefinition rearLeftLeg = root.addOrReplaceChild("rear_left_leg", CubeListBuilder.create(), rearLeftLegPose);
        PartDefinition rearRightLeg = root.addOrReplaceChild("rear_right_leg", CubeListBuilder.create(), rearRightLegPose);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), leftArmPose);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), rightArmPose);
        root.addOrReplaceChild("left_wing", CubeListBuilder.create(), leftWingPose);
        root.addOrReplaceChild("right_wing", CubeListBuilder.create(), rightWingPose);
        switch (kind) {
            case TENTACLE, TENTACLE_TINY -> addTentacle(root, kind == Kind.TENTACLE ? 10 : 6);
            case SPORE -> {
                body.addOrReplaceChild("inner", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 2.0F, -6.0F, 12.0F, 12.0F, 12.0F), PartPose.ZERO);
                body.addOrReplaceChild("spore", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.ZERO);
            }
            case SWARMER -> {
                body.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
            }
            case SPIDER -> addSpider(body, head, leftLeg, rightLeg);
            case CHICKEN -> addChicken(body, head, leftLeg, rightLeg, root);
            case COW -> addCow(body, head, leftLeg, rightLeg, rearLeftLeg, rearRightLeg);
            case PIG -> addPig(body, head, leftLeg, rightLeg, rearLeftLeg, rearRightLeg);
            case SHEEP -> addSheep(body, head, leftLeg, rightLeg, rearLeftLeg, rearRightLeg);
            case CREEPER -> addCreeper(body, head, leftLeg, rightLeg, rearLeftLeg, rearRightLeg);
            case VILLAGER -> addVillager(body, head, leftLeg, rightLeg, root);
        }
        int textureHeight = kind == Kind.TENTACLE || kind == Kind.TENTACLE_TINY || kind == Kind.SPORE || kind == Kind.SWARMER || kind == Kind.VILLAGER ? 64 : 32;
        return LayerDefinition.create(mesh, 64, textureHeight);
    }

    private static void addTentacle(PartDefinition root, int length) {
        PartDefinition base = root.addOrReplaceChild(
            "tentacle_base",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
            PartPose.offset(0.0F, 12.0F, 0.0F)
        );
        PartDefinition part = base;
        for (int index = 0; index < length - 1; index++) {
            part = part.addOrReplaceChild(
                "tentacle_" + index,
                CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F)
            );
        }
        part.addOrReplaceChild(
            "tentacle_tip",
            CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F),
            PartPose.offset(0.0F, -8.0F, 0.0F)
        );
        part.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, -8.0F, 0.0F));
    }

    private static void addSpider(PartDefinition body, PartDefinition head, PartDefinition leftLeg, PartDefinition rightLeg) {
        body.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 9.0F));
        body.addOrReplaceChild("thorax", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        for (int index = 0; index < 4; index++) {
            float legZ = 2.0F - index;
            leftLeg.addOrReplaceChild("leg_" + index, CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 0.0F, legZ));
            rightLeg.addOrReplaceChild("leg_" + index, CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 0.0F, legZ));
        }
    }

    private static void addChicken(PartDefinition body, PartDefinition head, PartDefinition leftLeg, PartDefinition rightLeg, PartDefinition root) {
        body.addOrReplaceChild("body_cube", CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F), PartPose.ZERO);
        head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F).texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F).texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        leftLeg.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F), PartPose.ZERO);
        rightLeg.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F), PartPose.ZERO);
        root.getChild("left_wing").addOrReplaceChild("wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.ZERO);
        root.getChild("right_wing").addOrReplaceChild("wing", CubeListBuilder.create().texOffs(24, 13).mirror().addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.ZERO);
    }

    private static void addCow(PartDefinition body, PartDefinition head, PartDefinition leftLeg, PartDefinition rightLeg, PartDefinition rearLeftLeg, PartDefinition rearRightLeg) {
        body.addOrReplaceChild("body_cube", CubeListBuilder.create().texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F).texOffs(52, 0).addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F).texOffs(22, 0).addBox(-5.0F, -5.0F, -4.0F, 1.0F, 3.0F, 1.0F).texOffs(22, 0).mirror().addBox(4.0F, -5.0F, -4.0F, 1.0F, 3.0F, 1.0F), PartPose.ZERO);
        addQuadrupedLegs(leftLeg, rightLeg, rearLeftLeg, rearRightLeg, 12.0F, 0.0F, 12.0F, 7.0F, -5.0F);
    }

    private static void addPig(PartDefinition body, PartDefinition head, PartDefinition leftLeg, PartDefinition rightLeg, PartDefinition rearLeftLeg, PartDefinition rearRightLeg) {
        body.addOrReplaceChild("body_cube", CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F), PartPose.ZERO);
        head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F).texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F), PartPose.ZERO);
        addQuadrupedLegs(leftLeg, rightLeg, rearLeftLeg, rearRightLeg, 6.0F, 0.0F, 18.0F, 7.0F, -5.0F);
    }

    private static void addSheep(PartDefinition body, PartDefinition head, PartDefinition leftLeg, PartDefinition rightLeg, PartDefinition rearLeftLeg, PartDefinition rearRightLeg) {
        body.addOrReplaceChild("body_cube", CubeListBuilder.create().texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F), PartPose.ZERO);
        head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        addQuadrupedLegs(leftLeg, rightLeg, rearLeftLeg, rearRightLeg, 12.0F, 0.0F, 12.0F, 7.0F, -5.0F);
    }

    private static void addQuadrupedLegs(PartDefinition leftFront, PartDefinition rightFront, PartDefinition leftRear, PartDefinition rightRear, float length, float inflation, float y, float zFront, float zRear) {
        CubeListBuilder first = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, length, 4.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(inflation));
        leftFront.addOrReplaceChild("leg", first, PartPose.ZERO);
        rightFront.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, length, 4.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(inflation)), PartPose.ZERO);
        leftRear.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, length, 4.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(inflation)), PartPose.ZERO);
        rightRear.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, length, 4.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(inflation)), PartPose.ZERO);
    }

    private static void addCreeper(PartDefinition body, PartDefinition head, PartDefinition leftLeg, PartDefinition rightLeg, PartDefinition rearLeftLeg, PartDefinition rearRightLeg) {
        body.addOrReplaceChild("body_cube", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.ZERO);
        head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        addQuadrupedLegs(leftLeg, rightLeg, rearLeftLeg, rearRightLeg, 6.0F, 0.0F, 16.0F, 4.0F, -4.0F);
    }

    private static void addVillager(PartDefinition body, PartDefinition head, PartDefinition leftLeg, PartDefinition rightLeg, PartDefinition root) {
        body.addOrReplaceChild("body_cube", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F), PartPose.ZERO);
        body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(0, 38)
            .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
        head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F).texOffs(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.ZERO);
        PartDefinition hat = head.addOrReplaceChild("hat",
            CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.51F)), PartPose.ZERO);
        hat.addOrReplaceChild("hat_rim",
            CubeListBuilder.create().texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F),
            PartPose.rotation((float) (-Math.PI / 2.0), 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.ZERO);
        rightLeg.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.ZERO);
        root.getChild("right_arm").addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.ZERO);
        root.addOrReplaceChild("folded_arms", CubeListBuilder.create().texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
            PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
    }

    public static <T extends Mob> LayerDefinition createLayer(Kind kind) {
        return createBodyLayer(kind);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = limbSwing * 0.6662F;
        float walkSpeed = limbSwingAmount;
        float partialTick = ageInTicks - entity.tickCount;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;
        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        TaintedSheep sheep = entity instanceof TaintedSheep value ? value : null;
        TaintedChicken chicken = entity instanceof TaintedChicken value ? value : null;
        Taintacle tentacle = entity instanceof Taintacle value ? value : null;
        TaintSpore spore = entity instanceof TaintSpore value ? value : null;
        float age = ageInTicks;
        if (this.kind == Kind.SHEEP) {
            int timer = sheep == null ? 0 : sheep.getGrazeTicks();
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
        }
        if (this.kind == Kind.CHICKEN || this.kind == Kind.COW || this.kind == Kind.PIG || this.kind == Kind.SHEEP || this.kind == Kind.CREEPER || this.kind == Kind.VILLAGER) {
            this.leftLeg.xRot = Mth.cos(walk) * 1.4F * walkSpeed;
            this.rightLeg.xRot = Mth.cos(walk + (float) Math.PI) * 1.4F * walkSpeed;
            this.rearLeftLeg.xRot = this.rightLeg.xRot;
            this.rearRightLeg.xRot = this.leftLeg.xRot;
        }
        if (this.kind == Kind.COW || this.kind == Kind.PIG || this.kind == Kind.SHEEP) {
            this.body.xRot = (float) (Math.PI / 2.0);
        }
        if (this.kind == Kind.VILLAGER) {
            this.leftArm.xRot = -0.75F;
            this.rightArm.xRot = -0.75F;
        }
        if (this.kind == Kind.CHICKEN) {
            this.body.xRot = (float) (Math.PI / 2.0);
        }
        if (this.kind == Kind.SPIDER) {
            float[] baseYaw = { (float) Math.PI / 4.0F, -(float) Math.PI / 4.0F, (float) Math.PI / 8.0F, -(float) Math.PI / 8.0F,
                -(float) Math.PI / 8.0F, (float) Math.PI / 8.0F, -(float) Math.PI / 4.0F, (float) Math.PI / 4.0F };
            float[] stride = {
                0.0F, 0.0F, (float) Math.PI, (float) Math.PI, (float) (Math.PI / 2.0), (float) (Math.PI / 2.0), (float) (Math.PI * 1.5), (float) (Math.PI * 1.5)
            };
            for (int index = 0; index < this.spiderLegs.size(); index++) {
                ModelPart leg = this.spiderLegs.get(index);
                float side = index % 2 == 0 ? -1.0F : 1.0F;
                leg.zRot = side * (float) Math.PI / 4.0F * (index < 2 || index > 5 ? 1.0F : 0.74F)
                    + side * Mth.abs(Mth.sin(walk + stride[index]) * 0.4F) * walkSpeed;
                leg.yRot = baseYaw[index] + (index % 2 == 0 ? -1.0F : 1.0F) * Mth.cos(walk * 2.0F + stride[index]) * 0.4F * walkSpeed;
                leg.xRot = 0.0F;
            }
        }
        if (this.kind == Kind.CHICKEN) {
            float flap = (Mth.sin((chicken == null ? 0.0F : Mth.lerp(partialTick, chicken.oldFlap, chicken.flap))) + 1.0F) * (chicken == null ? 0.0F : Mth.lerp(partialTick, chicken.oldFlapSpeed, chicken.flapSpeed));
            this.leftWing.zRot = flap;
            this.rightWing.zRot = -flap;
        } else if (this.kind == Kind.TENTACLE || this.kind == Kind.TENTACLE_TINY) {
            float modulation = 0.0625F * 0.2F;
            float intensity = (tentacle == null ? 1.0F : tentacle.flailIntensity);
            float speed = (tentacle != null && tentacle.isAgitatedForRender()) ? 3.0F : 1.0F + ((tentacle != null && tentacle.isAgitatedForRender()) ? modulation : -modulation);
            float flail = intensity + ((tentacle != null && tentacle.isAttackingForRender()) ? modulation : -modulation);
            for (int index = 0; index < this.tentacleSegments.size(); index++) {
                ModelPart segment = this.tentacleSegments.get(index);
                segment.xScale = 0.88F;
                segment.yScale = 0.88F;
                segment.zScale = 0.88F;
                segment.xRot = 0.15F * flail * Mth.sin(age * 0.1F * speed - index / 2.0F);
                segment.zRot = 0.1F / flail * Mth.sin(age * 0.15F - index / 2.0F);
            }
        } else if (this.kind == Kind.SPORE) {
            float intensity = entity.hurtTime > 0 ? 0.04F : 0.02F;
            this.body.xRot = intensity * Mth.sin(age * 0.05F);
            this.body.zRot = intensity * Mth.sin(age * 0.1F);
        }
    }
}
