package thaumcraft.research;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;

public sealed interface ResearchPage {
    /** Addon pages are rendered through the client-side CustomResearchPages registry. */
    record Custom(ResourceLocation type, String data) implements ResearchPage {}

    static ResearchPage custom(ResourceLocation type, String data) { return new Custom(type, data); }
    record Text(String text, List<Object> arguments) implements ResearchPage {
        public Text { arguments = List.copyOf(arguments); }
        public Text(String text) { this(text, List.of()); }
    }

    record ConcealedText(String research, String text) implements ResearchPage {
    }

    record Image(ResourceLocation image, String caption) implements ResearchPage {
    }

    record Aspects(AspectList aspects) implements ResearchPage {
    }

    record Crafting(List<String> recipes) implements ResearchPage {
    }

    record Arcane(List<String> recipes) implements ResearchPage {
    }

    record Crucible(List<String> recipes) implements ResearchPage {
    }

    record Infusion(List<String> recipes) implements ResearchPage {
    }

    record InfusionEnchantment(String recipe) implements ResearchPage {
    }

    record Compound(String recipe) implements ResearchPage {
    }

    record Smelting(TcStack input, TcStack output) implements ResearchPage {
    }

    static ResearchPage text(String text) {
        return new Text(text);
    }

    static ResearchPage formatted(String text, Object... arguments) {
        return new Text(text, List.of(arguments));
    }

    static ResearchPage concealed(String research, String text) {
        return new ConcealedText(research, text);
    }

    static ResearchPage image(String image, String caption) {
        return new Image(ResourceLocation.parse(image), caption);
    }

    static ResearchPage aspects(AspectList aspects) {
        return new Aspects(aspects);
    }

    static ResearchPage crafting(String... recipes) {
        return new Crafting(List.of(recipes));
    }

    static ResearchPage arcane(String... recipes) {
        return new Arcane(List.of(recipes));
    }

    static ResearchPage crucible(String... recipes) {
        return new Crucible(List.of(recipes));
    }

    static ResearchPage infusion(String... recipes) {
        return new Infusion(List.of(recipes));
    }

    static ResearchPage infusionEnchantment(String recipe) {
        return new InfusionEnchantment(recipe);
    }

    static ResearchPage compound(String recipe) {
        return new Compound(recipe);
    }

    static ResearchPage smelting(TcStack input, TcStack output) {
        return new Smelting(input, output);
    }
}
