package thaumcraft.node;

import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;

public interface Node extends AspectContainer, NodeView {
    String getId();

    default void setId(String id) {
    }

    AspectList getAspectsBase();

    NodeType getNodeType();

    void setNodeType(NodeType type);

    @Nullable NodeModifier getNodeModifier();

    void setNodeModifier(@Nullable NodeModifier modifier);

    int getNodeVisBase(Aspect aspect);

    void setNodeVisBase(Aspect aspect, int amount);
}
