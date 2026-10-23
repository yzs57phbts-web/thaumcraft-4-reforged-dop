import org.joml.Matrix4f;
import org.joml.Vector3f;
import thaumcraft.client.geom.GolemItemTransforms;

public final class GolemItemTransformsTest {
    public static void main(String[] args) {
        Matrix4f idle = GolemItemTransforms.fishingRod(0.0F);
        Vector3f handle = idle.transformPosition(new Vector3f(-0.375F, -0.375F, 0.0F));
        Vector3f tip = idle.transformPosition(new Vector3f(0.375F, 0.375F, 0.0F));
        if (tip.z >= handle.z || tip.y >= handle.y) {
            throw new AssertionError("Rod must point forward and upward from the hand");
        }
        for (float arm : new float[]{-3.5F, -2.0F, -1.0F, 0.0F, 1.0F}) {
            Matrix4f legacy = new Matrix4f()
                .rotateX((float) Math.toRadians(5.0F + 90.0F * arm / (float) Math.PI))
                .translate(-0.26875F, 1.6F, -0.53F)
                .rotateY((float) -Math.PI / 2.0F)
                .rotateZ((float) -Math.PI / 6.0F)
                .scale(0.66F, -0.66F, 0.66F);
            for (float u : new float[]{0.0F, 0.25F, 1.0F}) {
                for (float v : new float[]{0.0F, 0.75F, 1.0F}) {
                    Vector3f expected = legacy.transformPosition(new Vector3f(1.0F - u, 1.0F - v, -1.0F / 32.0F));
                    Vector3f actual = GolemItemTransforms.fishingRod(arm).transformPosition(new Vector3f(u - 0.5F, 0.5F - v, 0.0F));
                    if (actual.distance(expected) > 0.00001F) {
                        throw new AssertionError("Modern rod lost the TC4 grip during arm animation");
                    }
                }
            }
        }
        Vector3f blockCenter = GolemItemTransforms.carriedBlock().transformPosition(new Vector3f());
        if (blockCenter.distance(new Vector3f(0.0F, 1.0F, -0.5F)) > 0.00001F) {
            throw new AssertionError("Carried block must stay centered between the hands");
        }
        Matrix4f flat = new Matrix4f().scale(0.4F).translate(-0.5F, 2.5F, -1.25F)
            .rotateX((float) Math.PI).rotateY((float) Math.PI)
            .rotateZ((float) Math.toRadians(-335)).rotateY((float) Math.toRadians(-50))
            .translate(0, -0.3F, 0).scale(1.5F)
            .rotateY((float) Math.toRadians(50)).rotateZ((float) Math.toRadians(335))
            .translate(-0.9375F, -0.0625F, 0);
        for (float u : new float[]{0, 0.25F, 1}) {
            for (float v : new float[]{0, 0.75F, 1}) {
                Vector3f expected = flat.transformPosition(new Vector3f(1 - u, 1 - v, -1.0F / 32));
                Vector3f actual = GolemItemTransforms.carriedItem().transformPosition(new Vector3f(u - 0.5F, 0.5F - v, 0));
                if (actual.distance(expected) > 0.00001F) throw new AssertionError("Flat item lost its TC4 grip");
            }
        }
        for (float scale : new float[]{0.5F, 0.5625F, 0.75F, 1.0F}) {
            Matrix4f jar = new Matrix4f().scale(0.4F).translate(0, 2.5F, -1).scale(scale)
                .translate(-0.5F, 0, -0.8F).rotateX((float) Math.PI).rotateY((float) Math.PI)
                .rotateZ((float) Math.toRadians(-335)).rotateY((float) Math.toRadians(-50));
            for (float x : new float[]{0.1875F, 0.8125F}) {
                for (float y : new float[]{0, 0.875F}) {
                    for (float z : new float[]{0.1875F, 0.8125F}) {
                        Vector3f expected = jar.transformPosition(new Vector3f(x, y, z));
                        Vector3f actual = GolemItemTransforms.carriedJar(scale).transformPosition(new Vector3f(x - 0.5F, y - 0.5F, z - 0.5F));
                        if (actual.distance(expected) > 0.00001F) throw new AssertionError("Jar scale moved its grip");
                    }
                }
            }
        }
        System.out.println("Golem block, item, jar and animated fishing grip checks passed");
    }
}
