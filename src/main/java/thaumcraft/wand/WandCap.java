package thaumcraft.wand;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;

public record WandCap(
    String tag,
    float baseCostModifier,
    List<Aspect> specialAspects,
    float specialCostModifier,
    ResourceLocation item,
    int craftCost,
    @Nullable String requiredMetal,
    ResourceLocation texture
) {
    public WandCap(String tag, float baseCostModifier, List<Aspect> specialAspects, float specialCostModifier, ResourceLocation item, int craftCost, @Nullable String requiredMetal) {
        this(tag, baseCostModifier, specialAspects, specialCostModifier, item, craftCost, requiredMetal, thaumcraft.Thaumcraft.id("textures/model/wand_cap_" + tag + ".png"));
    }

    public float getCostModifier(Aspect aspect) {
        return specialAspects.contains(aspect) ? specialCostModifier : baseCostModifier;
    }

    public String research() {
        return "CAP_" + tag;
    }
}
