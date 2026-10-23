package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.RobeArmorDyeRecipe;
import thaumcraft.crafting.VoidRobeArmorDyeRecipe;

public final class ModRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Thaumcraft.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<thaumcraft.crafting.JarLabelRecipe>> JAR_LABEL_ASPECT = SERIALIZERS.register(
        "jar_label_aspect", () -> thaumcraft.crafting.JarLabelRecipe.SERIALIZER
    );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RobeArmorDyeRecipe>> ROBE_ARMOR_DYE = SERIALIZERS.register(
        "robe_armor_dye", () -> RobeArmorDyeRecipe.SERIALIZER
    );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<VoidRobeArmorDyeRecipe>> VOID_ROBE_ARMOR_DYE = SERIALIZERS.register(
        "void_robe_armor_dye", () -> VoidRobeArmorDyeRecipe.SERIALIZER
    );

    private ModRecipes() {
    }

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
