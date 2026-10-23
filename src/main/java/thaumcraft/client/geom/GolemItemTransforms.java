package thaumcraft.client.geom;

import org.joml.Matrix4f;

public final class GolemItemTransforms {
    private GolemItemTransforms() { }

    public static Matrix4f carriedBlock() {
        return new Matrix4f().scale(0.4F).translate(0.0F, 2.5F, -1.25F)
            .rotateX((float) Math.PI).rotateY((float) Math.PI / 2.0F);
    }

    public static Matrix4f carriedJar(float scale) {
        return new Matrix4f().scale(0.4F).translate(0.0F, 2.5F, -1.0F).scale(scale)
            .translate(-0.5F, 0.0F, -0.8F).rotateX((float) Math.PI).rotateY((float) Math.PI)
            .rotateZ((float) Math.toRadians(-335.0F)).rotateY((float) Math.toRadians(-50.0F))
            .translate(0.5F, 0.5F, 0.5F);
    }

    public static Matrix4f carriedItem() {
        return new Matrix4f().scale(0.4F).translate(-0.5F, 2.5F, -1.25F)
            .rotateX((float) Math.PI).rotateY((float) Math.PI)
            .rotateZ((float) Math.toRadians(-335.0F)).rotateY((float) Math.toRadians(-50.0F))
            .translate(0.0F, -0.3F, 0.0F).scale(1.5F)
            .rotateY((float) Math.toRadians(50.0F)).rotateZ((float) Math.toRadians(335.0F))
            .translate(-0.9375F, -0.0625F, 0.0F)
            .translate(0.5F, 0.5F, -1.0F / 32.0F).rotateY((float) Math.PI);
    }

    public static Matrix4f fishingRod(float armAngle) {
        return new Matrix4f()
            .rotateX((float) Math.toRadians(5.0F) + armAngle * 0.5F)
            .translate(-0.26875F, 1.6F, -0.53F)
            .rotateY((float) -Math.PI / 2.0F)
            .rotateZ((float) -Math.PI / 6.0F)
            .scale(0.66F, -0.66F, 0.66F)
            .translate(0.5F, 0.5F, -1.0F / 32.0F)
            .rotateY((float) Math.PI);
    }
}
