package thaumcraft.platform;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public record TcToolMaterial(
    int durability,
    float miningSpeed,
    float attackDamageBonus,
    int enchantability,
    TagKey<Item> repairItems,
    TagKey<Block> incorrectForDrops
) {
}
