package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.infusion.InfusionKeys;
import thaumcraft.research.ResearchGate;

public final class InfusionCrafting {
    public static final int TYPE_RECIPE = 0;
    public static final int TYPE_ENCHANTMENT = 1;
    public static final int TYPE_RUNIC = 2;

    public record Plan(int type, String key, List<TcIngredient> components, int instability, AspectList aspects, int xp) {
        public Plan {
            components = List.copyOf(components);
        }
    }

    private InfusionCrafting() {
    }

    public static boolean matchesComponents(List<TcIngredient> components, List<ItemStack> input) {
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : input) {
            remaining.add(stack.copy());
        }
        for (TcIngredient component : components) {
            boolean found = false;
            for (int index = 0; index < remaining.size(); index++) {
                if (component.test(remaining.get(index))) {
                    remaining.remove(index);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    public static @Nullable Plan findRecipe(List<ItemStack> items, ItemStack central, Player player) {
        for (InfusionRecipe recipe : TcRecipes.all(InfusionRecipe.class)) {
            if (InfusionRunicAugmentRecipe.isDisplayRecipe(recipe.key())) {
                continue;
            }
            if (matches(recipe, items, central, player)) {
                return new Plan(TYPE_RECIPE, recipe.key(), recipe.components(), recipe.instability(), recipe.aspects().copy(), 0);
            }
        }
        if (InfusionRunicAugmentRecipe.matches(items, central, player)) {
            return runicPlan(central);
        }
        return null;
    }

    public static @Nullable Plan findEnchantment(Level level, List<ItemStack> items, ItemStack central, Player player) {
        for (InfusionEnchantmentRecipe recipe : TcRecipes.all(InfusionEnchantmentRecipe.class)) {
            if (matches(level, recipe, items, central, player)) {
                return enchantmentPlan(level, recipe, central);
            }
        }
        return null;
    }

    public static @Nullable Plan resolve(Level level, int type, String key, ItemStack central) {
        if (type == TYPE_RUNIC) {
            return runicPlan(central);
        }
        TcRecipe recipe = TcRecipes.get(key);
        if (type == TYPE_RECIPE && recipe instanceof InfusionRecipe infusion) {
            return new Plan(TYPE_RECIPE, key, infusion.components(), infusion.instability(), infusion.aspects().copy(), 0);
        }
        if (type == TYPE_ENCHANTMENT && recipe instanceof InfusionEnchantmentRecipe enchantment) {
            return enchantmentPlan(level, enchantment, central);
        }
        return null;
    }

    public static ItemStack result(Level level, int type, String key, ItemStack central) {
        if (type == TYPE_RUNIC) {
            return InfusionRunicAugmentRecipe.getOutput(central);
        }
        if (type == TYPE_ENCHANTMENT) {
            return enchanted(level, key, central);
        }
        if (TcRecipes.get(key) instanceof InfusionRecipe recipe) {
            TcStack result = recipe.result();
            if (isTagResult(result)) {
                ItemStack tagged = central.copy();
                Map<String, String> tags = new HashMap<>(InfusionKeys.TAGS.getOrDefault(tagged, Map.of()));
                tags.putAll(result.data());
                InfusionKeys.TAGS.set(tagged, tags);
                return tagged;
            }
            return result.create();
        }
        return ItemStack.EMPTY;
    }

    public static boolean isTagResult(TcStack result) {
        return !result.data().isEmpty() && !(result.data().containsKey("cap") && result.data().containsKey("rod")) && !result.hasDataHandler();
    }

    public static @Nullable Holder<Enchantment> enchantment(Level level, ResourceLocation id) {
        return level.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .get(ResourceKey.create(Registries.ENCHANTMENT, id))
            .map(reference -> (Holder<Enchantment>) reference)
            .orElse(null);
    }

    public static int enchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return EnchantmentHelper.getEnchantmentsForCrafting(stack).getLevel(enchantment);
    }

    private static boolean researched(Player player, String research) {
        return research.isEmpty() || ResearchGate.isComplete(player, research);
    }

    private static boolean matches(InfusionRecipe recipe, List<ItemStack> items, ItemStack central, Player player) {
        if (!recipe.central().exists() || !researched(player, recipe.research())) {
            return false;
        }
        if (!recipe.central().test(central)) {
            return false;
        }
        return matchesComponents(recipe.components(), items);
    }

    private static boolean matches(Level level, InfusionEnchantmentRecipe recipe, List<ItemStack> items, ItemStack central, Player player) {
        if (!researched(player, recipe.research())) {
            return false;
        }
        Holder<Enchantment> holder = enchantment(level, recipe.enchantment());
        if (holder == null || !holder.value().isSupportedItem(central) || central.getMaxStackSize() != 1 || !central.isDamageableItem()) {
            return false;
        }
        ItemEnchantments existing = EnchantmentHelper.getEnchantmentsForCrafting(central);
        for (Holder<Enchantment> other : existing.keySet()) {
            if (other.equals(holder)) {
                if (existing.getLevel(other) >= holder.value().getMaxLevel()) {
                    return false;
                }
            } else if (!Enchantment.areCompatible(holder, other) || !Enchantment.areCompatible(other, holder)) {
                return false;
            }
        }
        return matchesComponents(recipe.components(), items);
    }

    private static Plan runicPlan(ItemStack central) {
        return new Plan(
            TYPE_RUNIC,
            InfusionRunicAugmentRecipe.KEY,
            InfusionRunicAugmentRecipe.getComponents(central),
            InfusionRunicAugmentRecipe.getInstability(central),
            InfusionRunicAugmentRecipe.getAspects(central),
            0
        );
    }

    private static @Nullable Plan enchantmentPlan(Level level, InfusionEnchantmentRecipe recipe, ItemStack central) {
        Holder<Enchantment> holder = enchantment(level, recipe.enchantment());
        if (holder == null) {
            return null;
        }
        ItemEnchantments existing = EnchantmentHelper.getEnchantmentsForCrafting(central);
        int total = 0;
        float modifier = existing.getLevel(holder);
        for (Holder<Enchantment> other : existing.keySet()) {
            total += existing.getLevel(other);
            if (!other.equals(holder)) {
                modifier += existing.getLevel(other) * 0.1F;
            }
        }
        int instability = total / 2 + recipe.instability();
        AspectList cost = recipe.aspects().copy();
        for (Aspect aspect : cost.getAspects()) {
            cost.add(aspect, (int) (cost.getAmount(aspect) * modifier));
        }
        int baseXp = Math.max(1, holder.value().getMinCost(1) / 3);
        int xp = baseXp * (1 + existing.getLevel(holder));
        return new Plan(TYPE_ENCHANTMENT, recipe.key(), recipe.components(), instability, cost, xp);
    }

    private static ItemStack enchanted(Level level, String key, ItemStack central) {
        ItemStack output = central.copy();
        if (TcRecipes.get(key) instanceof InfusionEnchantmentRecipe recipe) {
            Holder<Enchantment> holder = enchantment(level, recipe.enchantment());
            if (holder != null) {
                EnchantmentHelper.updateEnchantments(output, enchantments -> enchantments.set(holder, enchantments.getLevel(holder) + 1));
            }
        }
        return output;
    }
}
