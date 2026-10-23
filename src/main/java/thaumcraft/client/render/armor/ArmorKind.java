package thaumcraft.client.render.armor;

import java.util.Map;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import thaumcraft.Thaumcraft;

public enum ArmorKind {
    ROBE("robe", f -> ArmorLayers.robe((float) f), ArmorLayers::robeParts),
    FORTRESS("fortress", f -> ArmorLayers.fortress((float) f), ArmorLayers::fortressParts),
    KNIGHT("knight", f -> ArmorLayers.knight((float) f), ArmorLayers::knightParts),
    LEADER("leader", f -> ArmorLayers.leader((float) f), ArmorLayers::leaderParts);

    private final ModelLayerLocation outer;
    private final ModelLayerLocation inner;
    private final DoubleFunction<LayerDefinition> layer;
    private final Function<ModelPart, Map<String, ModelPart>> parts;

    ArmorKind(String name, DoubleFunction<LayerDefinition> layer, Function<ModelPart, Map<String, ModelPart>> parts) {
        this.outer = new ModelLayerLocation(Thaumcraft.id("armor_" + name), "outer");
        this.inner = new ModelLayerLocation(Thaumcraft.id("armor_" + name), "inner");
        this.layer = layer;
        this.parts = parts;
    }

    public ModelLayerLocation outer() {
        return outer;
    }

    public ModelLayerLocation inner() {
        return inner;
    }

    public LayerDefinition createOuter() {
        return layer.apply(1.0);
    }

    public LayerDefinition createInner() {
        return layer.apply(0.5);
    }

    public Map<String, ModelPart> parts(ModelPart root) {
        return parts.apply(root);
    }
}
