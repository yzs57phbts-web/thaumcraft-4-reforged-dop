package thaumcraft.node;

import javax.annotation.Nullable;
import thaumcraft.aspect.AspectList;

public interface NodeView {
    String getId();

    AspectList getAspects();

    NodeType getNodeType();

    @Nullable NodeModifier getNodeModifier();
}
