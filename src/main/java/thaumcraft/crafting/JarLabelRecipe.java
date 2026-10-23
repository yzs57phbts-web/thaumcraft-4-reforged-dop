package thaumcraft.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.*;
import thaumcraft.registry.ModItems;

/** Label a jar using a phial without consuming its essentia, or clear a labelled tag. */
public final class JarLabelRecipe extends CustomRecipe {
    public static final JarLabelRecipe INSTANCE = new JarLabelRecipe();
    public static final RecipeSerializer<JarLabelRecipe> SERIALIZER = new RecipeSerializer<>() {
        public MapCodec<JarLabelRecipe> codec() { return MapCodec.unit(INSTANCE); }
        public StreamCodec<RegistryFriendlyByteBuf, JarLabelRecipe> streamCodec() { return StreamCodec.unit(INSTANCE); }
    };
    private JarLabelRecipe() { super(CraftingBookCategory.MISC); }
    public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }
    public RecipeSerializer<?> getSerializer() { return SERIALIZER; }

    private record Input(ItemStack label, ItemStack phial) {}
    private static Input parse(CraftingInput input) {
        ItemStack label = ItemStack.EMPTY, phial = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.JAR_LABEL.get()) && label.isEmpty()) label = stack;
            else if (stack.is(ModItems.ESSENTIA_PHIAL.get()) && phial.isEmpty()) phial = stack;
            else return null;
        }
        if (label.isEmpty()) return null;
        var tags = AspectKeys.ASPECTS.get(phial.isEmpty() ? label : phial);
        if (tags == null || tags.size() != 1 || !tags.getAspects().getFirst().isEnabled()) return null;
        if (!phial.isEmpty() && tags.visSize() <= 0) return null;
        return new Input(label, phial);
    }
    public boolean matches(CraftingInput input, Level level) { return parse(input) != null; }
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var parsed = parse(input);
        if (parsed == null) return ItemStack.EMPTY;
        var output = new ItemStack(ModItems.JAR_LABEL.get());
        if (!parsed.phial().isEmpty()) {
            var aspect = AspectKeys.ASPECTS.get(parsed.phial()).getAspects().getFirst();
            AspectKeys.ASPECTS.set(output, new AspectList().add(aspect, 0));
        }
        return output;
    }
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        var items = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        if (parse(input) != null) for (int i = 0; i < input.size(); i++)
            if (input.getItem(i).is(ModItems.ESSENTIA_PHIAL.get())) items.set(i, input.getItem(i).copyWithCount(1));
        return items;
    }
}
