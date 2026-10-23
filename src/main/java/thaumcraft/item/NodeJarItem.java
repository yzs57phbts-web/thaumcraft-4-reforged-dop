package thaumcraft.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nullable;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.node.NodeModifier;
import thaumcraft.node.NodeType;
import thaumcraft.registry.ModBlocks;

public class NodeJarItem extends BlockItem {
    public NodeJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static Properties properties(Properties properties) {
        return properties.stacksTo(1);
    }

    public static ItemStack create(AspectList aspects, NodeType type, @Nullable NodeModifier modifier, String id) {
        ItemStack stack = new ItemStack(ModBlocks.NODE_JAR_ITEM.get());
        AspectKeys.ASPECTS.set(stack, aspects.copy());
        NodeJarKeys.DATA.set(stack, new NodeJarData(type, modifier, id));
        return stack;
    }

    public static @Nullable AspectList getAspects(ItemStack stack) {
        AspectList aspects = AspectKeys.ASPECTS.get(stack);
        return aspects == null || aspects.isEmpty() ? null : aspects;
    }

    public static NodeJarData getData(ItemStack stack) {
        return NodeJarKeys.DATA.getOrDefault(stack, NodeJarData.DEFAULT);
    }
}
