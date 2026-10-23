package thaumcraft.client.render.entity.eldritch;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import thaumcraft.Thaumcraft;

public final class EldritchCubeModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("eldritch_cube"), "main");

    private EldritchCubeModel() {
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static ModelPart bake(ModelPart root) {
        return root.getChild("cube");
    }
}
