package thaumcraft.client.platform;

public final class CameraRenderState {
    public final org.joml.Quaternionf orientation = new org.joml.Quaternionf(net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
}
