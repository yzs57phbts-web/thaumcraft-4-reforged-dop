package thaumcraft.client.render.entity.golem;

import thaumcraft.client.platform.LivingEntityRenderState;
import thaumcraft.client.platform.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;

public final class TravelingTrunkRenderState extends LivingEntityRenderState {
    public final ItemStackRenderState upgradeItem = new ItemStackRenderState();
    public ResourceLocation texture;
    public int upgrade;
    public int anger;
    public float lidOpen;
}
