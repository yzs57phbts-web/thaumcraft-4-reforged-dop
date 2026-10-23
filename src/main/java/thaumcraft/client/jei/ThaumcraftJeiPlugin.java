package thaumcraft.client.jei;

import java.util.ArrayList;
import java.util.List;
import thaumcraft.wand.Wands;
import thaumcraft.wand.WandRegistry;
import thaumcraft.client.platform.JeiWandSubtypes;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.ArcaneRecipe;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionEnchantmentRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModBlocks;

@JeiPlugin
public final class ThaumcraftJeiPlugin implements IModPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThaumcraftJeiPlugin.class);

    @Override
    public ResourceLocation getPluginUid() {
        return Thaumcraft.id("jei");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        JeiWandSubtypes.register(registration);
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> wands = new ArrayList<>();
        wands.add(Wands.create(WandRegistry.CAP_IRON, WandRegistry.ROD_WOOD, false));
        for (ArcaneDisplay recipe : ArcaneDisplay.wandRecipes()) {
            wands.add(recipe.result());
        }
        registration.addExtraItemStacks(wands);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        List<ItemStack> hidden = new ArrayList<>();
        hidden.add(new ItemStack(ModBlocks.NODE_JAR_ITEM.get()));
        for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if ((id.getNamespace().equals("thaumicexploration") || id.getNamespace().equals("thaumcraft")) && id.getPath().endsWith("_floating_candle")
                || id.toString().equals("thaumichorizons:greatwood_boat")
                || id.getNamespace().equals("thaumcraft") && !thaumcraft.modern.MetalIntegration.itemAvailable(id.getPath())) hidden.add(new ItemStack(item));
        }
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hidden);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
            new CrucibleCategory(helper),
            new ArcaneCategory(helper),
            new InfusionCategory(helper),
            new EnchantmentCategory(helper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<CrucibleRecipe> crucible = TcRecipes.all(CrucibleRecipe.class).stream().filter(recipe -> recipe.result().exists() && recipe.catalyst().exists() && thaumcraft.modern.MetalIntegration.researchAvailable(recipe.research())).toList();
        List<ArcaneDisplay> arcane = new ArrayList<>(TcRecipes.all(ArcaneRecipe.class).stream()
            .filter(recipe -> recipe.result().exists() && thaumcraft.modern.MetalIntegration.researchAvailable(recipe.research())).map(ArcaneDisplay::of).toList());
        arcane.addAll(ArcaneDisplay.wandRecipes());
        List<InfusionRecipe> infusion = TcRecipes.all(InfusionRecipe.class).stream().filter(recipe -> recipe.result().exists() && thaumcraft.modern.MetalIntegration.researchAvailable(recipe.research())).toList();
        List<EnchantmentCategory.Entry> enchantments = enchantments();
        registration.addRecipes(CrucibleCategory.TYPE, crucible);
        registration.addRecipes(ArcaneCategory.TYPE, arcane);
        registration.addRecipes(InfusionCategory.TYPE, infusion);
        registration.addRecipes(EnchantmentCategory.TYPE, enchantments);
        LOGGER.info("JEI recipes: crucible {}, arcane {}, infusion {}, infusion enchantment {}", crucible.size(), arcane.size(), infusion.size(), enchantments.size());
    }

    private static List<EnchantmentCategory.Entry> enchantments() {
        List<EnchantmentCategory.Entry> entries = new ArrayList<>();
        if (Minecraft.getInstance().level == null) {
            return entries;
        }
        HolderLookup.RegistryLookup<Enchantment> lookup = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (InfusionEnchantmentRecipe recipe : TcRecipes.all(InfusionEnchantmentRecipe.class)) {
            Holder<Enchantment> holder = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, recipe.enchantment())).orElse(null);
            if (holder == null) {
                continue;
            }
            List<ItemStack> books = new ArrayList<>();
            for (int level = 1; level <= holder.value().getMaxLevel(); level++) {
                books.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level)));
            }
            entries.add(new EnchantmentCategory.Entry(recipe, books));
        }
        return entries;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(CrucibleCategory.TYPE, ModBlocks.CRUCIBLE.get());
        registration.addRecipeCatalysts(ArcaneCategory.TYPE, ModBlocks.ARCANE_WORKTABLE.get());
        registration.addRecipeCatalysts(InfusionCategory.TYPE, ModBlocks.RUNIC_MATRIX.get());
        registration.addRecipeCatalysts(EnchantmentCategory.TYPE, ModBlocks.RUNIC_MATRIX.get());
    }
}
