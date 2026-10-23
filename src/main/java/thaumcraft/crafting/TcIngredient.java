package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public sealed interface TcIngredient {
    TcIngredient EMPTY = new Empty();

    boolean test(ItemStack stack);

    List<ItemStack> displayStacks();

    boolean exists();

    static TcIngredient item(String id) {
        return new Stack(TcStack.of(id));
    }

    static TcIngredient of(TcStack stack) {
        return new Stack(stack);
    }

    static TcIngredient tag(String id) {
        return new Tag(TagKey.create(Registries.ITEM, ResourceLocation.parse(id)));
    }

    static TcIngredient anyOf(TcIngredient... options) {
        return new AnyOf(List.of(options));
    }

    /** Exclusions apply to crafting and the book/JEI ingredient list together. */
    record Excluding(TcIngredient source, TcIngredient excluded) implements TcIngredient {
        public boolean test(ItemStack stack) { return source.test(stack) && !excluded.test(stack); }
        public List<ItemStack> displayStacks() { return source.displayStacks().stream().filter(stack -> !excluded.test(stack)).toList(); }
        public boolean exists() { return !displayStacks().isEmpty(); }
    }

    /** Required by the ritual, but retained on its pedestal after it is processed. */
    record Reusable(TcIngredient ingredient) implements TcIngredient {
        public boolean test(ItemStack stack) { return ingredient.test(stack); }
        public List<ItemStack> displayStacks() {
            return ingredient.displayStacks().stream().map(original -> {
                ItemStack copy = original.copy();
                copy.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(
                    List.of(net.minecraft.network.chat.Component.translatable("tc.modern.template_retained"))));
                return copy;
            }).toList();
        }
        public boolean exists() { return ingredient.exists(); }
    }

    record Empty() implements TcIngredient {
        @Override
        public boolean test(ItemStack stack) {
            return stack.isEmpty();
        }

        @Override
        public List<ItemStack> displayStacks() {
            return List.of();
        }

        @Override
        public boolean exists() {
            return true;
        }
    }

    record Stack(TcStack stack) implements TcIngredient {
        @Override
        public boolean test(ItemStack candidate) {
            return stack.matches(candidate);
        }

        @Override
        public List<ItemStack> displayStacks() {
            ItemStack created = stack.create();
            return created.isEmpty() ? List.of() : List.of(created);
        }

        @Override
        public boolean exists() {
            return stack.exists();
        }
    }

    record Tag(TagKey<Item> tag) implements TcIngredient {
        @Override
        public boolean test(ItemStack stack) {
            return stack.is(tag);
        }

        @Override
        public List<ItemStack> displayStacks() {
            List<ItemStack> stacks = new ArrayList<>();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                stacks.add(new ItemStack(holder));
            }
            return stacks;
        }

        @Override
        public boolean exists() {
            return !displayStacks().isEmpty();
        }
    }

    record AnyOf(List<TcIngredient> options) implements TcIngredient {
        @Override
        public boolean test(ItemStack stack) {
            for (TcIngredient option : options) {
                if (option.test(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<ItemStack> displayStacks() {
            List<ItemStack> stacks = new ArrayList<>();
            for (TcIngredient option : options) {
                stacks.addAll(option.displayStacks());
            }
            return stacks;
        }

        @Override
        public boolean exists() {
            for (TcIngredient option : options) {
                if (option.exists()) {
                    return true;
                }
            }
            return false;
        }
    }
}
