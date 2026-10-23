package thaumcraft.research;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.TcRecipes;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class RecipeSync {
    private RecipeSync() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        validate(event.getServer());
    }

    public static int validate(MinecraftServer server) {
        int missing = 0;
        for (ResearchItem research : ResearchCategories.allResearch()) {
            for (ResearchPage page : research.pages()) {
                switch (page) {
                    case ResearchPage.Crafting crafting -> missing += checkAll(server, research, crafting.recipes());
                    case ResearchPage.Arcane arcane -> missing += checkAll(server, research, arcane.recipes());
                    case ResearchPage.Crucible crucible -> missing += checkAll(server, research, crucible.recipes());
                    case ResearchPage.Infusion infusion -> missing += checkAll(server, research, infusion.recipes());
                    case ResearchPage.InfusionEnchantment enchantment -> missing += check(server, research, enchantment.recipe());
                    case ResearchPage.Compound compound -> missing += check(server, research, compound.recipe());
                    default -> {
                    }
                }
            }
        }
        return missing;
    }

    private static int checkAll(MinecraftServer server, ResearchItem research, Iterable<String> keys) {
        int missing = 0;
        for (String key : keys) {
            missing += check(server, research, key);
        }
        return missing;
    }

    private static int check(MinecraftServer server, ResearchItem research, String key) {
        if (TcRecipes.get(key) == null) {
            Thaumcraft.LOGGER.warn("Research {} references unknown recipe {}", research.key(), key);
            return 1;
        }
        if (TcRecipes.get(key) instanceof CraftingRecipeRef reference) {
            ResourceLocation id = reference.recipe();
            if (server.getRecipeManager().byKey(id).isEmpty()) {
                Thaumcraft.LOGGER.debug("Research {} recipe {} points to missing crafting recipe {}", research.key(), key, id);
                return 1;
            }
        }
        return 0;
    }
}
