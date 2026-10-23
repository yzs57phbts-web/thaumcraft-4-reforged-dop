package thaumcraft.test;

import java.util.ArrayList;
import java.util.List;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.*;
import thaumcraft.research.ResearchCategories;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class ContentAuditGameTests {
    @GameTest(template = "test/platform", batch = "content_audit_registry")
    public static void legacyRegistryCoverage(GameTestHelper helper) throws Exception {
        try (var input = ContentAuditGameTests.class.getResourceAsStream("/data/thaumcraft/audit/legacy_inventory.json")) {
            helper.assertTrue(input != null, "legacy inventory manifest missing");
            var expected = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            List<String> missing = new ArrayList<>();
            for (var id : expected.getAsJsonArray("blocks")) if (!BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse(id.getAsString()))) missing.add("block " + id.getAsString());
            for (var id : expected.getAsJsonArray("items")) if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(id.getAsString()))) missing.add("item " + id.getAsString());
            System.out.println("TC4_REGISTRY_AUDIT missing=" + missing);
            helper.assertTrue(missing.isEmpty(), "legacy content not registered: " + missing);
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "content_audit_references")
    public static void runtimeRecipeReferences(GameTestHelper helper) {
        List<String> errors = new ArrayList<>();
        List<String> unavailableIntegrations = new ArrayList<>();
        int checked = 0;
        for (TcRecipe recipe : TcRecipes.all()) {
            if (List.of("PureTin","PureSilver","PureLead","TransTin","TransSilver","TransLead","WandCapSilverInert","WandCapSilver").contains(recipe.key())
                && !thaumcraft.modern.MetalIntegration.researchAvailable(recipe.research())) { unavailableIntegrations.add(recipe.key()); continue; }
            if (!recipe.research().isEmpty() && ResearchCategories.getResearch(recipe.research()) == null) errors.add(recipe.key() + ": missing research " + recipe.research());
            TcStack output = null;
            List<TcIngredient> ingredients = List.of();
            if (recipe instanceof InfusionRecipe infusion && !InfusionRunicAugmentRecipe.isDisplayRecipe(recipe.key())) {
                output = infusion.result();
                ingredients = new ArrayList<>(infusion.components());
                ingredients.add(infusion.central());
            } else if (recipe instanceof ShapedArcaneRecipe arcane) {
                output = arcane.result();
                ingredients = arcane.grid();
            } else if (recipe instanceof ShapelessArcaneRecipe arcane) {
                output = arcane.result();
                ingredients = arcane.ingredients();
            } else if (recipe instanceof CrucibleRecipe crucible) {
                output = crucible.result();
                ingredients = List.of(crucible.catalyst());
            } else if (recipe instanceof InfusionEnchantmentRecipe enchantment) {
                helper.assertTrue(InfusionCrafting.enchantment(helper.getLevel(), enchantment.enchantment()) != null, "missing enchantment " + enchantment.enchantment());
                ingredients = enchantment.components();
            }
            if (output != null && !output.exists()) errors.add(recipe.key() + ": missing output " + output);
            for (TcIngredient ingredient : ingredients) if (!ingredient.exists()) {
                if (recipe instanceof CrucibleRecipe && List.of("PureTin", "PureSilver", "PureLead").contains(recipe.key()) && ingredient instanceof TcIngredient.Tag tag
                    && List.of("c:raw_materials/tin", "c:raw_materials/silver", "c:raw_materials/lead").contains(tag.tag().location().toString())) unavailableIntegrations.add(recipe.key());
                else errors.add(recipe.key() + ": missing ingredient " + ingredient);
            }
            checked++;
        }
        System.out.println("TC4_CONTENT_AUDIT recipes=" + checked + " unavailable_integrations=" + unavailableIntegrations + " missing_references=" + errors);
        helper.assertTrue(errors.isEmpty(), "unusable recipe references: " + errors);
        helper.succeed();
    }
}
