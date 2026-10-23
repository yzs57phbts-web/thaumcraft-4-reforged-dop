package thaumcraft.registry;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

final class RegistryHelper {
    private RegistryHelper() {
    }

    static <I extends Item> DeferredItem<I> registerItem(DeferredRegister.Items items, String name, Function<Item.Properties, ? extends I> factory) {
        return items.registerItem(name, factory);
    }

    static <I extends Item> DeferredItem<I> registerItem(DeferredRegister.Items items, String name, Function<Item.Properties, ? extends I> factory, UnaryOperator<Item.Properties> properties) {
        return items.register(name, () -> factory.apply(properties.apply(new Item.Properties())));
    }

    static DeferredItem<Item> registerSimpleItem(DeferredRegister.Items items, String name) {
        return items.registerSimpleItem(name);
    }

    static DeferredItem<Item> registerSimpleItem(DeferredRegister.Items items, String name, UnaryOperator<Item.Properties> properties) {
        return items.register(name, () -> new Item(properties.apply(new Item.Properties())));
    }

    static DeferredItem<BlockItem> registerSimpleBlockItem(DeferredRegister.Items items, Holder<Block> block) {
        return items.registerSimpleBlockItem(block);
    }

    static DeferredItem<BlockItem> registerSimpleBlockItem(DeferredRegister.Items items, Holder<Block> block, UnaryOperator<Item.Properties> properties) {
        return items.register(block.unwrapKey().orElseThrow().location().getPath(), () -> new BlockItem(block.value(), properties.apply(new Item.Properties())));
    }

    static <B extends Block> DeferredBlock<B> registerBlock(DeferredRegister.Blocks blocks, String name, Function<BlockBehaviour.Properties, ? extends B> factory) {
        return blocks.registerBlock(name, factory);
    }

    static <B extends Block> DeferredBlock<B> registerBlock(DeferredRegister.Blocks blocks, String name, Function<BlockBehaviour.Properties, ? extends B> factory, UnaryOperator<BlockBehaviour.Properties> properties) {
        return blocks.register(name, () -> factory.apply(properties.apply(BlockBehaviour.Properties.of())));
    }

    static DeferredBlock<Block> registerSimpleBlock(DeferredRegister.Blocks blocks, String name, UnaryOperator<BlockBehaviour.Properties> properties) {
        return blocks.register(name, () -> new Block(properties.apply(BlockBehaviour.Properties.of())));
    }
}
