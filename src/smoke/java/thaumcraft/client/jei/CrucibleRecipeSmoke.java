package thaumcraft.client.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.research.ResearchPageScreen;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchCategories;

@JeiPlugin
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class CrucibleRecipeSmoke implements IModPlugin {
    private static final List<String> KEYS = List.of("AltHoneyBottle", "AltHoneycomb", "AltGlowInk", "AltPoisonousPotato", "AltString", "AltQuartz");
    private static IJeiRuntime runtime;
    private static boolean started;
    private static boolean initialized;
    private static boolean verified;
    private static int ticks;
    private static int captures;

    @Override
    public ResourceLocation getPluginUid() {
        return Thaumcraft.id("crucible25_smoke");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime value) {
        runtime = value;
    }

    private static void verifyJei() {
        var manager = runtime.getRecipeManager();
        var category = manager.getRecipeCategory(CrucibleCategory.TYPE);
        if (category == null) throw new IllegalStateException("Crucible JEI category missing");
        var shown = manager.createRecipeLookup(CrucibleCategory.TYPE).get().toList();
        for (String key : KEYS) {
            var recipe = (CrucibleRecipe) TcRecipes.get(key);
            if (shown.stream().noneMatch(entry -> entry.key().equals(key))) throw new IllegalStateException("JEI recipe missing: " + key);
            var layout = manager.createRecipeLayoutDrawable(category, recipe, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElseThrow();
            var inputs = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT);
            var outputs = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT);
            if (inputs.size() != 1 || outputs.size() != 1) throw new IllegalStateException("Wrong JEI slot layout");
            var inputStacks = inputs.getFirst().getItemStacks().toList();
            if (inputStacks.isEmpty() || inputStacks.stream().anyMatch(stack -> stack.getCount() != 1 || !recipe.catalyst().test(stack))) {
                throw new IllegalStateException("JEI catalyst mismatch: " + key);
            }
            var results = outputs.getFirst().getItemStacks().toList();
            if (results.size() != 1 || !results.getFirst().is(recipe.result().resolve()) || results.getFirst().getCount() != recipe.result().count()) {
                throw new IllegalStateException("JEI result mismatch: " + key);
            }
            if (key.equals("AltString") && inputStacks.size() != 16) throw new IllegalStateException("JEI does not show all wool colors");
        }
        if (shown.stream().anyMatch(recipe -> recipe.key().equals("AltCrackedBrick") || recipe.key().equals("AltMossyCobble"))) {
            throw new IllegalStateException("Removed crucible recipe still shown by JEI");
        }
        System.out.println("CRUCIBLE25_JEI_PASS recipes=6 removed=2 input_count=1 outputs=exact wool_colors=16");
    }

    private static void capture(String name) {
        var minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.getMainRenderTarget(), message -> captures++);
    }

    private static void show(int step) {
        var minecraft = Minecraft.getInstance();
        if (step < KEYS.size()) {
            var recipe = (CrucibleRecipe) TcRecipes.get(KEYS.get(step));
            runtime.getRecipesGui().showRecipes(runtime.getRecipeManager().getRecipeCategory(CrucibleCategory.TYPE), List.of(recipe), List.of());
        } else {
            String key = switch (step) {
                case 6 -> "ALCHEMICALDUPLICATION";
                case 7, 8 -> "ALCHEMICALMANUFACTURE";
                default -> "ENTROPICPROCESSING";
            };
            int page = switch (step) {
                case 6 -> 6;
                case 8 -> 4;
                default -> 2;
            };
            minecraft.setScreen(new ResearchPageScreen(ResearchCategories.getResearch(key), page));
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("thaumcraft.crucibleSmoke")) return;
        var minecraft = Minecraft.getInstance();
        minecraft.options.pauseOnLostFocus = false;
        if (!started && minecraft.getOverlay() == null && minecraft.screen != null) {
            started = true;
            minecraft.options.renderDistance().set(5);
            minecraft.options.simulationDistance().set(5);
            var rules = new GameRules();
            rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
            minecraft.createWorldOpenFlows().createFreshLevel("crucible25-" + System.currentTimeMillis(),
                new LevelSettings("Crucible recipes verification", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT),
                new WorldOptions(4182, false, false),
                registry -> registry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), minecraft.screen);
        }
        if (minecraft.level == null || minecraft.player == null || minecraft.getOverlay() != null) return;
        if (!initialized) {
            initialized = true;
            minecraft.setScreen(null);
            var id = minecraft.player.getUUID();
            minecraft.getSingleplayerServer().execute(() -> {
                var player = minecraft.getSingleplayerServer().getPlayerList().getPlayer(id);
                for (String key : List.of("ALCHEMICALDUPLICATION", "ALCHEMICALMANUFACTURE", "ENTROPICPROCESSING")) Knowledge.get(player).addResearch(key);
                for (Aspect aspect : Aspect.values()) if (aspect.isEnabled()) Knowledge.get(player).discoverAspect(aspect);
                Knowledge.sync(player);
            });
        }
        if (runtime == null || !Knowledge.get(minecraft.player).isResearchComplete("ENTROPICPROCESSING")) return;
        if (!verified) {
            verifyJei();
            verified = true;
        }
        ticks++;
        int offset = ticks - 20;
        if (offset >= 0 && offset < 300) {
            int step = offset / 30;
            if (offset % 30 == 0) show(step);
            if (offset % 30 == 12) capture("crucible25-" + String.format(java.util.Locale.ROOT, "%02d", step) + (step < 6 ? "-jei-" + KEYS.get(step) : "-thaumonomicon"));
        }
        if (ticks == 330) {
            if (captures != 10) throw new IllegalStateException("Expected ten recipe screenshots");
            System.out.println("CRUCIBLE25_CLIENT_PASS jei=6 book=4 screenshots=" + captures);
            minecraft.stop();
        }
    }
}
