package thaumcraft.wand;

import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;

public record WandRod(
    String tag,
    String textureTag,
    int capacity,
    ResourceLocation item,
    int craftCost,
    @Nullable RodUpdate onUpdate,
    boolean glowing,
    boolean staff,
    boolean runes,
    ResourceLocation texture
) {
    public WandRod(String tag, String textureTag, int capacity, ResourceLocation item, int craftCost, @Nullable RodUpdate onUpdate, boolean glowing, boolean staff, boolean runes) {
        this(tag, textureTag, capacity, item, craftCost, onUpdate, glowing, staff, runes, thaumcraft.Thaumcraft.id("textures/model/wand_rod_" + textureTag + ".png"));
    }

    public String research() {
        return "ROD_" + tag;
    }
}
