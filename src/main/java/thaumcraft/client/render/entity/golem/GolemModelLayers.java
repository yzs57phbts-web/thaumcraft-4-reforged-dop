package thaumcraft.client.render.entity.golem;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import thaumcraft.Thaumcraft;

public final class GolemModelLayers {
    public static final ModelLayerLocation GOLEM = layer("golem");
    public static final ModelLayerLocation ACCESSORIES = layer("golem_accessories");
    public static final ModelLayerLocation MARKER = layer("golem_marker");
    public static final ModelLayerLocation TRUNK = layer("traveling_trunk");

    private GolemModelLayers() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Thaumcraft.id(name), "main");
    }

    private static CubeListBuilder box(int u, int v, float x, float y, float z, float width, float height, float depth) {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, width, height, depth);
    }

    private static void part(PartDefinition parent, String name, CubeListBuilder box, float x, float y, float z) {
        parent.addOrReplaceChild(name, box, PartPose.offset(x, y, z));
    }

    private static void golemPart(PartDefinition parent, String name, CubeListBuilder box, float x, float y, float z) {
        parent.addOrReplaceChild(name, box, PartPose.offset(x, y + 30.0F, z));
    }

    public static LayerDefinition golem() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO);
        golemPart(body, "head", box(0, 0, -4, -11, -5.5F, 8, 9, 8), 0, 0, -2);
        golemPart(body, "body", CubeListBuilder.create().texOffs(0, 40).addBox(-8, -2, -6, 16, 12, 11).texOffs(0, 70).addBox(-4.5F, 10, -3, 9, 5, 6, new CubeDeformation(0.5F)), 0, 0, 0);
        limbs(body);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void limbs(PartDefinition body) {
        golemPart(body, "right_arm", box(60, 21, -12, -2.5F, -3, 4, 25, 6), 0, 0, 0);
        golemPart(body, "left_arm", CubeListBuilder.create().texOffs(60, 21).mirror().addBox(8, -2.5F, -3, 4, 25, 6), 0, 0, 0);
        golemPart(body, "right_leg", box(37, 0, -3.5F, -3, -3, 6, 16, 5), -4, 18, 0);
        golemPart(body, "left_leg", CubeListBuilder.create().texOffs(37, 0).mirror().addBox(-3.5F, -3, -3, 6, 16, 5), 5, 18, 0);
    }

    public static LayerDefinition accessories() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO);
        golemPart(body, "fez", box(0, 94, -4.5F, -15, -6, 9, 7, 9), 0, 0, -2);
        golemPart(body, "plate", box(32, 40, -6.5F, -1, -7, 13, 12, 13), 0, 0, 0);
        golemPart(body, "plate_left", box(0, 44, -8.5F, -4, -6.5F, 3, 6, 12), 0, 0, 0);
        golemPart(body, "plate_right", CubeListBuilder.create().texOffs(0, 44).mirror().addBox(5.5F, -4, -6.5F, 3, 6, 12), 0, 0, 0);
        golemPart(body, "hat", box(0, 110, -4.5F, -17, -6, 9, 9, 9), 0, 0, -2);
        golemPart(body, "glasses", box(0, 80, -4.5F, -8, -6, 9, 4, 9), 0, 0, -2);
        golemPart(body, "visor", box(0, 70, -5, -8, -6, 10, 5, 5), 0, 0, -2);
        golemPart(body, "hat_rim", box(36, 114, -6.5F, -9, -8, 13, 1, 13), 0, 0, -2);
        golemPart(body, "dartgun", box(80, 80, 7.9F, 7.5F, -3.5F, 6, 16, 7), 0, 0, 0);
        golemPart(body, "mace", box(80, 26, -13, 15, -5, 6, 8, 10), 0, 0, 0);
        golemPart(body, "bowtie", box(0, 0, -8.5F, -2, -6.5F, 17, 4, 12), 0, 0, 0);
        golemPart(body, "head_jar", box(96, 56, -4, -15, -5.5F, 8, 4, 8), 0, 0, -2);
        golemPart(body, "head_brain", box(96, 70, -3.5F, -14, -5, 7, 3, 7), 0, 0, -2);
        golemPart(body, "evil_head", box(64, 65, -4, -9, -5.5F, 8, 7, 8), 0, 0, -2);
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition marker() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO);
        golemPart(body, "marker", box(0, 88, -2, 3, -7, 4, 6, 1), 0, 0, 0);
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition trunk() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "lid", box(0, 0, 0, -5, -14, 14, 5, 14), 1, 7, 15);
        part(root, "knob", box(0, 0, -1, -2, -15, 2, 4, 1), 8, 7, 15);
        part(root, "bottom", box(0, 19, 0, 0, 0, 14, 10, 14), 1, 6, 1);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
