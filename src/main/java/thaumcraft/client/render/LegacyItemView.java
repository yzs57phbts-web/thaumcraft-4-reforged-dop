package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;

public enum LegacyItemView {
    INVENTORY("inventory"),
    EQUIPPED("equipped"),
    FIRST_PERSON("first_person"),
    ENTITY("entity");

    private final String name;

    LegacyItemView(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static LegacyItemView of(ItemDisplayContext context) {
        return switch (context) {
            case GUI -> INVENTORY;
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> EQUIPPED;
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> FIRST_PERSON;
            default -> ENTITY;
        };
    }

    public void apply(PoseStack poseStack) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        switch (this) {
            case INVENTORY -> {
                poseStack.scale(1.0F / 16.0F, -1.0F / 16.0F, 1.0F / 16.0F);
                poseStack.translate(-10.0F, -5.0F, 0.0F);
                poseStack.scale(10.0F, 10.0F, 10.0F);
                poseStack.translate(1.0F, 0.5F, 1.0F);
                poseStack.scale(1.0F, 1.0F, -1.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(210.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            }
            case EQUIPPED -> {
                poseStack.translate(-1.0F / 16.0F, -2.0F / 16.0F, 10.0F / 16.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(-180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.translate(-0.0625F, 0.4375F, 0.0625F);
                poseStack.translate(0.0F, 0.1875F, 0.0F);
                poseStack.scale(0.625F, -0.625F, 0.625F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-100.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case FIRST_PERSON -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.4F, 0.4F, 0.4F);
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case ENTITY -> poseStack.scale(0.5F, 0.5F, 0.5F);
        }
    }
}
