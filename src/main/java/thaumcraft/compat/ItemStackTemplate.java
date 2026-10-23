package thaumcraft.compat;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemStackTemplate(Holder<Item> item, int count, DataComponentPatch components) {
    public static final Codec<ItemStackTemplate> CODEC = ItemStack.CODEC.xmap(ItemStackTemplate::fromNonEmptyStack, ItemStackTemplate::create);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackTemplate> STREAM_CODEC = ItemStack.STREAM_CODEC.map(ItemStackTemplate::fromNonEmptyStack, ItemStackTemplate::create);

    public static ItemStackTemplate fromNonEmptyStack(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Stack must be non-empty");
        }
        return new ItemStackTemplate(stack.getItemHolder(), stack.getCount(), stack.getComponentsPatch());
    }

    public ItemStack create() {
        return new ItemStack(item, count, components);
    }
}
