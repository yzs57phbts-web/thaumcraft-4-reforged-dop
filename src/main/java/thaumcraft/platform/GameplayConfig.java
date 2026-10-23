package thaumcraft.platform;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import thaumcraft.research.ResearchConfig;
import thaumcraft.world.WorldGenConfig;

public final class GameplayConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue DIFFICULTY;
    private static final ModConfigSpec.IntValue ASPECT_CAP;
    private static final ModConfigSpec.BooleanValue CHEAT_SHEET;
    private static final ModConfigSpec.BooleanValue WUSS_MODE;
    private static final ModConfigSpec.BooleanValue AURA;
    private static final ModConfigSpec.BooleanValue STRUCTURES;
    private static final ModConfigSpec.BooleanValue CINNABAR;
    private static final ModConfigSpec.BooleanValue AMBER;
    private static final ModConfigSpec.BooleanValue INFUSED_STONE;
    private static final ModConfigSpec.BooleanValue TREES;
    private static final ModConfigSpec.IntValue NODE_RARITY;
    private static final ModConfigSpec.IntValue SPECIAL_RARITY;
    private static final ModConfigSpec.BooleanValue HARD_NODES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("research");
        DIFFICULTY = builder.comment("-1: purchase all research; 0: original mixed research; 1: solve research notes.")
            .defineInRange("difficulty", 0, -1, 1);
        ASPECT_CAP = builder.comment("Aspect pool threshold at which new research points have diminishing returns; not a hard storage limit.")
            .defineInRange("aspectSoftCap", 100, 1, 1000000);
        CHEAT_SHEET = builder.comment("Allow the creative cheat Thaumonomicon to grant all research.")
            .define("allowCheatSheet", false);
        WUSS_MODE = builder.comment("Disable new warp and warp events; existing warp remains saved.")
            .define("wussMode", false);
        builder.pop().push("worldgen");
        AURA = builder.define("generateAuraNodes", true);
        STRUCTURES = builder.define("generateStructures", true);
        CINNABAR = builder.define("generateCinnabar", true);
        AMBER = builder.define("generateAmber", true);
        INFUSED_STONE = builder.define("generateInfusedStone", true);
        TREES = builder.define("generateTrees", true);
        NODE_RARITY = builder.comment("Higher values make aura nodes rarer. Affects newly generated terrain.")
            .defineInRange("nodeRarity", 36, 1, 1000000);
        SPECIAL_RARITY = builder.comment("Higher values make special nodes rarer. Minimum 2 keeps modifier rolls valid.")
            .defineInRange("specialNodeRarity", 18, 2, 1000000);
        HARD_NODES = builder.define("hardNodeEffects", true);
        builder.pop();
        SPEC = builder.build();
    }

    private GameplayConfig() {
    }

    public static void register(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SPEC, "thaumcraft-server.toml");
        bus.addListener(GameplayConfig::load);
        bus.addListener(GameplayConfig::reload);
        bus.addListener(GameplayConfig::unload);
    }

    private static void load(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) apply();
    }

    private static void reload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) apply();
    }

    private static void unload(ModConfigEvent.Unloading event) {
        if (event.getConfig().getSpec() == SPEC) reset();
    }

    public static void apply() {
        ResearchConfig.RESEARCH_DIFFICULTY = DIFFICULTY.get();
        ResearchConfig.ASPECT_TOTAL_CAP = ASPECT_CAP.get();
        ResearchConfig.ALLOW_CHEAT_SHEET = CHEAT_SHEET.get();
        ResearchConfig.WUSS_MODE = WUSS_MODE.get();
        WorldGenConfig.genAura = AURA.get();
        WorldGenConfig.genStructure = STRUCTURES.get();
        WorldGenConfig.genCinnibar = CINNABAR.get();
        WorldGenConfig.genAmber = AMBER.get();
        WorldGenConfig.genInfusedStone = INFUSED_STONE.get();
        WorldGenConfig.genTrees = TREES.get();
        WorldGenConfig.nodeRarity = NODE_RARITY.get();
        WorldGenConfig.specialNodeRarity = SPECIAL_RARITY.get();
        WorldGenConfig.hardNode = HARD_NODES.get();
    }

    public static void reset() {
        ResearchConfig.RESEARCH_DIFFICULTY = 0;
        ResearchConfig.ASPECT_TOTAL_CAP = 100;
        ResearchConfig.ALLOW_CHEAT_SHEET = false;
        ResearchConfig.WUSS_MODE = false;
        WorldGenConfig.genAura = true;
        WorldGenConfig.genStructure = true;
        WorldGenConfig.genCinnibar = true;
        WorldGenConfig.genAmber = true;
        WorldGenConfig.genInfusedStone = true;
        WorldGenConfig.genTrees = true;
        WorldGenConfig.nodeRarity = 36;
        WorldGenConfig.specialNodeRarity = 18;
        WorldGenConfig.hardNode = true;
    }
}
