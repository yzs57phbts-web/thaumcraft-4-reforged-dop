package thaumcraft.client.render.entity.golem;

import thaumcraft.client.platform.LivingEntityRenderState;
import thaumcraft.client.platform.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;

public final class GolemRenderState extends LivingEntityRenderState {
    public final ItemStackRenderState carried = new ItemStackRenderState();
    public ResourceLocation texture;
    public int core;
    public String decoration = "";
    public float healthPercent = 1.0F;
    public float bootup;
    public boolean inactive;
    public boolean advanced;
    public boolean awakeWithoutCore;
    public boolean actionActive;
    public float actionTimer;
    public boolean leftArmActive;
    public float leftArm;
    public boolean rightArmActive;
    public float rightArm;
    public int healing;
    public int carryLimit;
    public boolean carrying;
    public boolean carriedBlock;
    public boolean carriedJar;
    public float carriedScale = 1.0F;
    public boolean fishing;
    public int upgradeCount;
    public final ItemStackRenderState coreItem = new ItemStackRenderState();
    public final ItemStackRenderState fishingRod = new ItemStackRenderState();
    public final ItemStackRenderState[] upgrades = new ItemStackRenderState[6];

    public GolemRenderState() {
        for (int index = 0; index < upgrades.length; index++) {
            upgrades[index] = new ItemStackRenderState();
        }
    }
}
