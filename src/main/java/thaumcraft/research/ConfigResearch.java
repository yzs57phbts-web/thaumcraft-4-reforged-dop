package thaumcraft.research;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;

public final class ConfigResearch {
    private ConfigResearch() {
    }

    public static void init() {
        initCategories();
        initBasics();
        initThaumaturgy();
        initAlchemy();
        initArtifice();
        initGolemancy();
        initEldritch();
    }

    private static void initCategories() {
        ResearchCategories.registerCategory("BASICS", ResourceLocation.parse("thaumcraft:textures/item/thaumonomiconcheat.png"), ResourceLocation.parse("thaumcraft:textures/gui/gui_researchback.png"));
        ResearchCategories.registerCategory("THAUMATURGY", ResourceLocation.parse("thaumcraft:textures/misc/r_thaumaturgy.png"), ResourceLocation.parse("thaumcraft:textures/gui/gui_researchback.png"));
        ResearchCategories.registerCategory("ALCHEMY", ResourceLocation.parse("thaumcraft:textures/misc/r_crucible.png"), ResourceLocation.parse("thaumcraft:textures/gui/gui_researchback.png"));
        ResearchCategories.registerCategory("ARTIFICE", ResourceLocation.parse("thaumcraft:textures/misc/r_artifice.png"), ResourceLocation.parse("thaumcraft:textures/gui/gui_researchback.png"));
        ResearchCategories.registerCategory("GOLEMANCY", ResourceLocation.parse("thaumcraft:textures/misc/r_golemancy.png"), ResourceLocation.parse("thaumcraft:textures/gui/gui_researchback.png"));
        ResearchCategories.registerCategory("ELDRITCH", ResourceLocation.parse("thaumcraft:textures/misc/r_eldritch.png"), ResourceLocation.parse("thaumcraft:textures/gui/gui_researchbackeldritch.png"));
    }

    private static void initBasics() {
        initBasics1();
        initBasics2();
    }

    private static void initBasics1() {
        new ResearchItem("ASPECTS", "BASICS", new AspectList(), 0, 0, 0, ResourceLocation.parse("thaumcraft:textures/misc/r_aspects.png"))
            .setPages(
                ResearchPage.text("tc.research_page.ASPECTS.1"),
                ResearchPage.text("tc.research_page.ASPECTS.2"),
                ResearchPage.text("tc.research_page.ASPECTS.3")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("PECH", "BASICS", new AspectList(), -4, -4, 0, ResourceLocation.parse("thaumcraft:textures/misc/r_pech.png"))
            .setPages(
                ResearchPage.text("tc.research_page.PECH.1"),
                ResearchPage.text("tc.research_page.PECH.2")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("NODES", "BASICS", new AspectList(), -2, 0, 0, ResourceLocation.parse("thaumcraft:textures/misc/r_nodes.png"))
            .setPages(
                ResearchPage.text("tc.research_page.NODES.1"),
                ResearchPage.text("tc.research_page.NODES.2"),
                ResearchPage.text("tc.research_page.NODES.3")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("WARP", "BASICS", new AspectList(), 0, 2, 0, ResourceLocation.parse("thaumcraft:textures/misc/r_warp.png"))
            .setPages(
                ResearchPage.text("tc.research_page.WARP.1"),
                ResearchPage.text("tc.research_page.WARP.2"),
                ResearchPage.text("tc.research_page.WARP.3")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("RESEARCH", "BASICS", new AspectList(), 2, 0, 0, TcStack.of("thaumcraft:scribing_tools"))
            .setPages(
                ResearchPage.text("tc.research_page.RESEARCH.1"),
                ResearchPage.text("tc.research_page.RESEARCH.2"),
                ResearchPage.crafting("Thaumometer"),
                ResearchPage.text("tc.research_page.RESEARCH.3"),
                ResearchPage.text("tc.research_page.RESEARCH.4"),
                ResearchPage.crafting("Scribe1"),
                ResearchPage.crafting("Scribe2"),
                ResearchPage.crafting("Scribe3"),
                ResearchPage.text("tc.research_page.RESEARCH.5"),
                ResearchPage.text("tc.research_page.RESEARCH.6"),
                ResearchPage.text("tc.research_page.RESEARCH.7"),
                ResearchPage.text("tc.research_page.RESEARCH.8"),
                ResearchPage.text("tc.research_page.RESEARCH.9"),
                ResearchPage.text("tc.research_page.RESEARCH.10"),
                ResearchPage.text("tc.research_page.RESEARCH.11"),
                ResearchPage.text("tc.research_page.RESEARCH.12")
            )
            .setAutoUnlock()
            .setStub()
            .setRound()
            .registerResearchItem();
        new ResearchItem("KNOWFRAG", "BASICS", new AspectList(), 3, -2, 0, TcStack.of("thaumcraft:knowledge_fragment"))
            .setPages(
                ResearchPage.text("tc.research_page.KNOWFRAG.1"),
                ResearchPage.crafting("KnowFrag")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .setParents("RESEARCH")
            .registerResearchItem();
        new ResearchItem("THAUMONOMICON", "BASICS", new AspectList(), 1, -2, 0, TcStack.of("thaumcraft:thaumonomicon"))
            .setPages(
                ResearchPage.text("tc.research_page.THAUMONOMICON.1"),
                ResearchPage.compound("Thaumonomicon")
            )
            .setAutoUnlock()
            .setStub()
            .setRound()
            .setParents("RESEARCH")
            .registerResearchItem();
        new ResearchItem("ORE", "BASICS", new AspectList(), -2, -2, 0, TcStack.of("thaumcraft:cinnabar_ore"))
            .setPages(
                ResearchPage.text("tc.research_page.ORE.1"),
                ResearchPage.text("tc.research_page.ORE.2"),
                ResearchPage.crafting("Clusters0", "Clusters1", "Clusters2", "Clusters3", "Clusters4", "Clusters5", "Clusters6"),
                ResearchPage.text("tc.research_page.ORE.3"),
                ResearchPage.text("tc.research_page.ORE.4")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("PLANTS", "BASICS", new AspectList(), -2, -4, 0, TcStack.of("thaumcraft:greatwood_sapling"))
            .setPages(
                ResearchPage.text("tc.research_page.PLANTS.1"),
                ResearchPage.crafting("PlankGreatwood"),
                ResearchPage.text("tc.research_page.PLANTS.2"),
                ResearchPage.crafting("PlankSilverwood"),
                ResearchPage.text("tc.research_page.PLANTS.3"),
                ResearchPage.text("tc.research_page.PLANTS.4"),
                ResearchPage.text("tc.research_page.PLANTS.5"),
                ResearchPage.text("tc.research_page.PLANTS.6")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("ENCHANT", "BASICS", new AspectList(), -4, -2, 0, ResourceLocation.parse("thaumcraft:textures/misc/r_enchant.png"))
            .setPages(
                ResearchPage.text("tc.research_page.ENCHANT.1"),
                ResearchPage.text("tc.research_page.ENCHANT.2")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("NODETAPPER1", "BASICS", new AspectList().add(Aspect.AURA, 3).add(Aspect.MAGIC, 3).add(Aspect.MOTION, 3).add(Aspect.EXCHANGE, 3), -4, 1, 2, ResourceLocation.parse("thaumcraft:textures/misc/r_nodetap1.png"))
            .setPages(
                ResearchPage.text("tc.research_page.NODETAPPER1.1")
            )
            .setParents("NODES")
            .setRound()
            .registerResearchItem();
        new ResearchItem("NODEPRESERVE", "BASICS", new AspectList().add(Aspect.AURA, 3).add(Aspect.GREED, 3).add(Aspect.SENSES, 3), -6, 2, 2, ResourceLocation.parse("thaumcraft:textures/misc/r_nodepreserve.png"))
            .setPages(
                ResearchPage.text("tc.research_page.NODEPRESERVE")
            )
            .setParents("NODETAPPER1")
            .setRound()
            .registerResearchItem();
    }

    private static void initBasics2() {
        new ResearchItem("NODEJAR", "BASICS", new AspectList().add(Aspect.AURA, 6).add(Aspect.GREED, 3).add(Aspect.EXCHANGE, 3).add(Aspect.MOTION, 3), -7, 4, 3, TcStack.of("thaumcraft:node_jar"))
            .setPages(
                ResearchPage.text("tc.research_page.NODEJAR.1"),
                ResearchPage.compound("NodeJar"),
                ResearchPage.text("tc.research_page.NODEJAR.2")
            )
            .setParents("NODEPRESERVE")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("NODETAPPER2", "BASICS", new AspectList().add(Aspect.AURA, 6).add(Aspect.MAGIC, 3).add(Aspect.MOTION, 3).add(Aspect.EXCHANGE, 3), -3, 3, 2, ResourceLocation.parse("thaumcraft:textures/misc/r_nodetap2.png"))
            .setPages(
                ResearchPage.text("tc.research_page.NODETAPPER2.1")
            )
            .setParents("NODETAPPER1")
            .setSpecial()
            .setRound()
            .registerResearchItem();
        new ResearchItem("RESEARCHER1", "BASICS", new AspectList().add(Aspect.MIND, 3).add(Aspect.SENSES, 3).add(Aspect.ORDER, 3), 4, 1, 1, ResourceLocation.parse("thaumcraft:textures/misc/r_researcher1.png"))
            .setPages(
                ResearchPage.text("tc.research_page.RESEARCHER1.1")
            )
            .setRound()
            .setParents("RESEARCH")
            .registerResearchItem();
        new ResearchItem("DECONSTRUCTOR", "BASICS", new AspectList().add(Aspect.MIND, 3).add(Aspect.CRAFT, 3).add(Aspect.ENTROPY, 3), 6, 2, 1, TcStack.of("thaumcraft:deconstruction_table"))
            .setPages(
                ResearchPage.text("tc.research_page.DECONSTRUCTOR.1"),
                ResearchPage.arcane("Deconstructor"),
                ResearchPage.text("tc.research_page.DECONSTRUCTOR.2")
            )
            .setRound()
            .setParents("RESEARCHER1")
            .registerResearchItem();
        new ResearchItem("RESEARCHER2", "BASICS", new AspectList().add(Aspect.MIND, 6).add(Aspect.ORDER, 3).add(Aspect.SENSES, 3).add(Aspect.MAGIC, 3), 3, 3, 2, ResourceLocation.parse("thaumcraft:textures/misc/r_researcher2.png"))
            .setPages(
                ResearchPage.text("tc.research_page.RESEARCHER2.1")
            )
            .setRound()
            .setSpecial()
            .setParents("RESEARCHER1")
            .setWarp(1)
            .registerResearchItem();
        new ResearchItem("RESEARCHDUPE", "BASICS", new AspectList().add(Aspect.MIND, 6).add(Aspect.EXCHANGE, 3).add(Aspect.SENSES, 3).add(Aspect.GREED, 3).add(Aspect.CRAFT, 3), 4, 5, 3, ResourceLocation.parse("thaumcraft:textures/misc/r_resdupe.png"))
            .setPages(
                ResearchPage.text("tc.research_page.RESEARCHDUPE.1")
            )
            .setRound()
            .setParents("RESEARCHER2")
            .registerResearchItem();
        new ResearchItem("CRIMSON", "BASICS", new AspectList(), 0, 4, 0, TcStack.of("thaumcraft:crimson_rites"))
            .setPages(
                ResearchPage.text("tc.research_page.CRIMSON.1")
            )
            .setStub()
            .setHidden()
            .setRound()
            .setSpecial()
            .setWarp(3)
            .registerResearchItem();
    }

    private static void initThaumaturgy() {
        initThaumaturgy1();
        initThaumaturgy2();
        initThaumaturgy3();
        initThaumaturgy4();
    }

    private static void initThaumaturgy1() {
        new ResearchItem("BASICTHAUMATURGY", "THAUMATURGY", new AspectList(), 0, 0, 0, TcStack.of("thaumcraft:wand"))
            .setPages(
                ResearchPage.text("tc.research_page.BASICTHAUMATURGY.1"),
                ResearchPage.text("tc.research_page.BASICTHAUMATURGY.2"),
                ResearchPage.crafting("WandCapIron"),
                ResearchPage.crafting("WandBasic")
            )
            .setAutoUnlock()
            .setStub()
            .setRound()
            .registerResearchItem();
        new ResearchItem("FOCUSFIRE", "THAUMATURGY", new AspectList().add(Aspect.FIRE, 3).add(Aspect.MAGIC, 3), 2, -2, 1, TcStack.of("thaumcraft:focus_fire"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSFIRE.1"),
                ResearchPage.text("tc.research_page.FOCUSFIRE.2"),
                ResearchPage.arcane("FocusFire")
            )
            .setParents("BASICTHAUMATURGY")
            .registerResearchItem();
        new ResearchItem("FOCUSFROST", "THAUMATURGY", new AspectList().add(Aspect.WATER, 3).add(Aspect.MAGIC, 3).add(Aspect.COLD, 6), 1, -5, 1, TcStack.of("thaumcraft:focus_frost"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSFROST.1"),
                ResearchPage.arcane("FocusFrost")
            )
            .setConcealed()
            .setSecondary()
            .setParents("FOCUSFIRE")
            .registerResearchItem();
        new ResearchItem("FOCUSHELLBAT", "THAUMATURGY", new AspectList().add(Aspect.TRAVEL, 3).add(Aspect.BEAST, 6).add(Aspect.FIRE, 3).add(Aspect.MAGIC, 3), 3, -7, 2, TcStack.of("thaumcraft:focus_nine_hells"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSHELLBAT.1"),
                ResearchPage.infusion("FocusHellbat")
            )
            .setHidden()
            .setEntityTriggers("thaumcraft:firebat")
            .setAspectTriggers(Aspect.FIRE)
            .setParentsHidden("FOCUSFIRE", "INFUSION")
            .setWarp(2)
            .registerResearchItem();
        new ResearchItem("FOCUSEXCAVATION", "THAUMATURGY", new AspectList().add(Aspect.EARTH, 3).add(Aspect.ENTROPY, 3).add(Aspect.MAGIC, 3), 0, -3, 2, TcStack.of("thaumcraft:focus_excavation"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSEXCAVATION.1"),
                ResearchPage.arcane("FocusExcavation")
            )
            .setConcealed()
            .setParents("FOCUSFIRE")
            .registerResearchItem();
        new ResearchItem("FOCUSWARDING", "THAUMATURGY", new AspectList().add(Aspect.EARTH, 6).add(Aspect.ARMOR, 3).add(Aspect.ORDER, 3).add(Aspect.MIND, 3), -2, -4, 3, TcStack.of("thaumcraft:focus_warding"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSWARDING.1"),
                ResearchPage.infusion("FocusWarding")
            )
            .setConcealed()
            .setParents("FOCUSEXCAVATION", "INFUSION")
            .registerResearchItem();
        new ResearchItem("FOCUSSHOCK", "THAUMATURGY", new AspectList().add(Aspect.AIR, 3).add(Aspect.ENERGY, 6).add(Aspect.MAGIC, 3), 3, -5, 1, TcStack.of("thaumcraft:focus_shock"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSSHOCK.1"),
                ResearchPage.arcane("FocusShock")
            )
            .setConcealed()
            .setSecondary()
            .setParents("FOCUSFIRE")
            .registerResearchItem();
        new ResearchItem("FOCUSTRADE", "THAUMATURGY", new AspectList().add(Aspect.EARTH, 3).add(Aspect.EXCHANGE, 6).add(Aspect.MAGIC, 3), 4, -3, 2, TcStack.of("thaumcraft:focus_equal_trade"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSTRADE.1"),
                ResearchPage.arcane("FocusTrade")
            )
            .setConcealed()
            .setParents("FOCUSFIRE")
            .registerResearchItem();
        new ResearchItem("FOCUSPORTABLEHOLE", "THAUMATURGY", new AspectList().add(Aspect.TRAVEL, 3).add(Aspect.ENTROPY, 3).add(Aspect.ELDRITCH, 6).add(Aspect.AIR, 3), 7, -2, 2, TcStack.of("thaumcraft:focus_portable_hole"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSPORTABLEHOLE.1"),
                ResearchPage.infusion("FocusPortableHole")
            )
            .setConcealed()
            .setParents("FOCUSTRADE", "INFUSION")
            .registerResearchItem();
        new ResearchItem("FOCUSPOUCH", "THAUMATURGY", new AspectList().add(Aspect.VOID, 6).add(Aspect.MAGIC, 3).add(Aspect.TOOL, 3), 4, -1, 1, TcStack.of("thaumcraft:focus_pouch"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSPOUCH.1"),
                ResearchPage.arcane("FocusPouch")
            )
            .setParents("FOCUSFIRE")
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("CAP_iron", "THAUMATURGY")
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("CAP_gold", "THAUMATURGY", new AspectList().add(Aspect.METAL, 3).add(Aspect.GREED, 3).add(Aspect.TOOL, 3), 3, 2, 1, TcStack.of("thaumcraft:gold_wand_cap"))
            .setPages(
                ResearchPage.text("tc.research_page.CAP_gold.1"),
                ResearchPage.arcane("WandCapGold")
            )
            .setParents("BASICTHAUMATURGY")
            .registerResearchItem();
    }

    private static void initThaumaturgy2() {
        new ResearchItem("CAP_thaumium", "THAUMATURGY", new AspectList().add(Aspect.METAL, 6).add(Aspect.MAGIC, 6).add(Aspect.TOOL, 3).add(Aspect.AURA, 3), 5, 4, 2, TcStack.of("thaumcraft:thaumium_wand_cap"))
            .setPages(
                ResearchPage.text("tc.research_page.CAP_thaumium.1"),
                ResearchPage.arcane("WandCapThaumiumInert"),
                ResearchPage.infusion("WandCapThaumium")
            )
            .setParents("CAP_gold", "THAUMIUM", "INFUSION")
            .registerResearchItem();
        new ResearchItem("CAP_copper", "THAUMATURGY", new AspectList().add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 3).add(Aspect.TOOL, 3), 2, 0, 1, TcStack.of("thaumcraft:copper_wand_cap"))
            .setPages(
                ResearchPage.text("tc.research_page.CAP_copper.1"),
                ResearchPage.arcane("WandCapCopper")
            )
            .setParents("BASICTHAUMATURGY")
            .registerResearchItem();
        new ResearchItem("CAP_silver", "THAUMATURGY", new AspectList().add(Aspect.METAL, 3).add(Aspect.GREED, 3).add(Aspect.TOOL, 3).add(Aspect.AURA, 3), 5, 1, 1, TcStack.of("thaumcraft:silver_wand_cap"))
            .setPages(
                ResearchPage.text("tc.research_page.CAP_silver.1"),
                ResearchPage.arcane("WandCapSilverInert"),
                ResearchPage.infusion("WandCapSilver")
            )
            .setConcealed()
            .setParents("CAP_gold", "INFUSION")
            .registerResearchItem();
        new ResearchItem("ROD_wood", "THAUMATURGY")
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("ROD_greatwood", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.TREE, 6).add(Aspect.MAGIC, 3), -5, 2, 1, TcStack.of("thaumcraft:greatwood_rod"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_greatwood.1"),
                ResearchPage.arcane("WandRodGreatwood")
            )
            .setParents("BASICTHAUMATURGY")
            .registerResearchItem();
        new ResearchItem("ROD_reed", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.AIR, 6).add(Aspect.PLANT, 3).add(Aspect.MAGIC, 3), -5, -1, 2, TcStack.of("thaumcraft:reed_rod"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_reed.1"),
                ResearchPage.infusion("WandRodReed")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_greatwood", "INFUSION")
            .registerResearchItem();
        new ResearchItem("ROD_blaze", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.FIRE, 6).add(Aspect.ENERGY, 3).add(Aspect.MAGIC, 3), -7, 0, 2, TcStack.of("thaumcraft:blazing_rod"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_blaze.1"),
                ResearchPage.infusion("WandRodBlaze")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_greatwood", "INFUSION")
            .registerResearchItem();
        new ResearchItem("ROD_obsidian", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.EARTH, 6).add(Aspect.FIRE, 3).add(Aspect.MAGIC, 3), -8, 2, 2, TcStack.of("thaumcraft:obsidian_rod"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_obsidian.1"),
                ResearchPage.infusion("WandRodObsidian")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_greatwood", "INFUSION")
            .registerResearchItem();
        new ResearchItem("ROD_ice", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.COLD, 6).add(Aspect.WATER, 3).add(Aspect.MAGIC, 3), -7, 4, 2, TcStack.of("thaumcraft:icy_rod"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_ice.1"),
                ResearchPage.infusion("WandRodIce")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_greatwood", "INFUSION")
            .registerResearchItem();
        new ResearchItem("ROD_quartz", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.ORDER, 6).add(Aspect.CRYSTAL, 3).add(Aspect.MAGIC, 3), -5, 5, 2, TcStack.of("thaumcraft:quartz_rod"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_quartz.1"),
                ResearchPage.infusion("WandRodQuartz")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_greatwood", "INFUSION")
            .registerResearchItem();
        new ResearchItem("ROD_bone", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.ENTROPY, 6).add(Aspect.UNDEAD, 3).add(Aspect.MAGIC, 3), -3, 0, 2, TcStack.of("thaumcraft:bone_rod"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_bone.1"),
                ResearchPage.infusion("WandRodBone")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_greatwood", "INFUSION")
            .setWarp(1)
            .registerResearchItem();
        new ResearchItem("ROD_silverwood", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 6).add(Aspect.TREE, 6).add(Aspect.MAGIC, 9), -2, 5, 3, TcStack.of("thaumcraft:silverwood_rod"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_silverwood.1"),
                ResearchPage.infusion("WandRodSilverwood")
            )
            .setParents("ROD_greatwood", "INFUSION")
            .registerResearchItem();
    }

    private static void initThaumaturgy3() {
        TcRecipes.register(new ShapedArcaneRecipe("ConfigResearch.Sceptre1", "SCEPTRE", TcStack.of("thaumcraft:wand"), new AspectList().add(Aspect.AIR, 1).add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1), 3, 3, List.of(TcIngredient.EMPTY, TcIngredient.item("thaumcraft:iron_wand_cap"), TcIngredient.item("thaumcraft:primal_charm"), TcIngredient.EMPTY, TcIngredient.item("minecraft:stick"), TcIngredient.item("thaumcraft:iron_wand_cap"), TcIngredient.item("thaumcraft:iron_wand_cap"), TcIngredient.EMPTY, TcIngredient.EMPTY)));
        TcRecipes.register(new ShapedArcaneRecipe("ConfigResearch.Sceptre2", "SCEPTRE", TcStack.of("thaumcraft:wand"), new AspectList().add(Aspect.AIR, 13).add(Aspect.FIRE, 13).add(Aspect.WATER, 13).add(Aspect.EARTH, 13).add(Aspect.ORDER, 13).add(Aspect.ENTROPY, 13), 3, 3, List.of(TcIngredient.EMPTY, TcIngredient.item("thaumcraft:gold_wand_cap"), TcIngredient.item("thaumcraft:primal_charm"), TcIngredient.EMPTY, TcIngredient.item("thaumcraft:greatwood_rod"), TcIngredient.item("thaumcraft:gold_wand_cap"), TcIngredient.item("thaumcraft:gold_wand_cap"), TcIngredient.EMPTY, TcIngredient.EMPTY)));
        TcRecipes.register(new ShapedArcaneRecipe("ConfigResearch.Sceptre3", "SCEPTRE", TcStack.of("thaumcraft:wand"), new AspectList().add(Aspect.AIR, 81).add(Aspect.FIRE, 81).add(Aspect.WATER, 81).add(Aspect.EARTH, 81).add(Aspect.ORDER, 81).add(Aspect.ENTROPY, 81), 3, 3, List.of(TcIngredient.EMPTY, TcIngredient.item("thaumcraft:thaumium_wand_cap"), TcIngredient.item("thaumcraft:primal_charm"), TcIngredient.EMPTY, TcIngredient.item("thaumcraft:silverwood_rod"), TcIngredient.item("thaumcraft:thaumium_wand_cap"), TcIngredient.item("thaumcraft:thaumium_wand_cap"), TcIngredient.EMPTY, TcIngredient.EMPTY)));
        new ResearchItem("SCEPTRE", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 6).add(Aspect.CRAFT, 6).add(Aspect.TREE, 6).add(Aspect.MAGIC, 9), 0, 4, 3, TcStack.of("thaumcraft:wand"))
            .setPages(
                ResearchPage.text("tc.research_page.SCEPTRE.1"),
                ResearchPage.arcane("ConfigResearch.Sceptre1", "ConfigResearch.Sceptre2", "ConfigResearch.Sceptre3")
            )
            .setConcealed()
            .setParents("ROD_silverwood")
            .registerResearchItem();
        new ResearchItem("ROD_greatwood_staff", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.TREE, 6).add(Aspect.MAGIC, 3), -1, 7, 1, TcStack.of("thaumcraft:greatwood_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_greatwood_staff.1"),
                ResearchPage.text("tc.research_page.ROD_greatwood_staff.2"),
                ResearchPage.arcane("WandRodGreatwoodStaff")
            )
            .setParents("ROD_silverwood")
            .registerResearchItem();
        new ResearchItem("ROD_reed_staff", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.AIR, 6).add(Aspect.PLANT, 3).add(Aspect.MAGIC, 3), -5, -2, 2, TcStack.of("thaumcraft:reed_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_reed_staff.1"),
                ResearchPage.arcane("WandRodReedStaff")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_reed")
            .setParentsHidden("ROD_greatwood_staff")
            .registerResearchItem();
        new ResearchItem("ROD_blaze_staff", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.FIRE, 6).add(Aspect.ENERGY, 3).add(Aspect.MAGIC, 3), -8, -1, 2, TcStack.of("thaumcraft:blazing_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_blaze_staff.1"),
                ResearchPage.arcane("WandRodBlazeStaff")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_blaze")
            .setParentsHidden("ROD_greatwood_staff")
            .registerResearchItem();
        new ResearchItem("ROD_obsidian_staff", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.EARTH, 6).add(Aspect.FIRE, 3).add(Aspect.MAGIC, 3), -9, 2, 2, TcStack.of("thaumcraft:obsidian_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_obsidian_staff.1"),
                ResearchPage.arcane("WandRodObsidianStaff")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_obsidian")
            .setParentsHidden("ROD_greatwood_staff")
            .registerResearchItem();
        new ResearchItem("ROD_ice_staff", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.COLD, 6).add(Aspect.WATER, 3).add(Aspect.MAGIC, 3), -8, 5, 2, TcStack.of("thaumcraft:icy_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_ice_staff.1"),
                ResearchPage.arcane("WandRodIceStaff")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_ice")
            .setParentsHidden("ROD_greatwood_staff")
            .registerResearchItem();
        new ResearchItem("ROD_quartz_staff", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.ORDER, 6).add(Aspect.CRYSTAL, 3).add(Aspect.MAGIC, 3), -4, 6, 2, TcStack.of("thaumcraft:quartz_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_quartz_staff.1"),
                ResearchPage.arcane("WandRodQuartzStaff")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_quartz")
            .setParentsHidden("ROD_greatwood_staff")
            .registerResearchItem();
        new ResearchItem("ROD_bone_staff", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.ENTROPY, 6).add(Aspect.UNDEAD, 3).add(Aspect.MAGIC, 3), -2, -1, 2, TcStack.of("thaumcraft:bone_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_bone_staff.1"),
                ResearchPage.arcane("WandRodBoneStaff")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_bone")
            .setParentsHidden("ROD_greatwood_staff")
            .setWarp(1)
            .registerResearchItem();
        new ResearchItem("ROD_silverwood_staff", "THAUMATURGY", new AspectList().add(Aspect.TOOL, 6).add(Aspect.TREE, 6).add(Aspect.MAGIC, 9), -1, 5, 3, TcStack.of("thaumcraft:silverwood_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_silverwood_staff.1"),
                ResearchPage.arcane("WandRodSilverwoodStaff")
            )
            .setSecondary()
            .setConcealed()
            .setParents("ROD_silverwood")
            .setParentsHidden("ROD_greatwood_staff")
            .registerResearchItem();
        new ResearchItem("WANDPED", "THAUMATURGY", new AspectList().add(Aspect.AURA, 6).add(Aspect.MAGIC, 3).add(Aspect.EXCHANGE, 3).add(Aspect.ENERGY, 3), -9, -6, 2, TcStack.of("thaumcraft:wand_recharge_pedestal"))
            .setPages(
                ResearchPage.text("tc.research_page.WANDPED.1"),
                ResearchPage.infusion("WandPed")
            )
            .setConcealed()
            .setParents("INFUSION", "NODEPRESERVE", "NODESTABILIZER")
            .registerResearchItem();
        new ResearchItem("VISAMULET", "THAUMATURGY", new AspectList().add(Aspect.AURA, 3).add(Aspect.MAGIC, 6).add(Aspect.ENERGY, 3).add(Aspect.VOID, 3), -9, -8, 2, TcStack.of("thaumcraft:vis_amulet"))
            .setPages(
                ResearchPage.text("tc.research_page.VISAMULET.1"),
                ResearchPage.infusion("VisAmulet"),
                ResearchPage.text("tc.research_page.VISAMULET.2")
            )
            .setConcealed()
            .setParents("WANDPED")
            .registerResearchItem();
        new ResearchItem("WANDPEDFOC", "THAUMATURGY", new AspectList().add(Aspect.AURA, 6).add(Aspect.MAGIC, 6).add(Aspect.EXCHANGE, 6).add(Aspect.ENERGY, 3).add(Aspect.TOOL, 3), -10, -7, 3, TcStack.of("thaumcraft:compound_recharge_focus"))
            .setPages(
                ResearchPage.text("tc.research_page.WANDPEDFOC.1"),
                ResearchPage.infusion("WandPedFocus")
            )
            .setSecondary()
            .setConcealed()
            .setParents("WANDPED")
            .registerResearchItem();
    }

    private static void initThaumaturgy4() {
        new ResearchItem("NODESTABILIZER", "THAUMATURGY", new AspectList().add(Aspect.AURA, 4).add(Aspect.ORDER, 4).add(Aspect.ENERGY, 4), -7, -4, 1, TcStack.of("thaumcraft:node_stabilizer"))
            .setPages(
                ResearchPage.text("tc.research_page.NODESTABILIZER.1"),
                ResearchPage.arcane("NodeStabilizer"),
                ResearchPage.text("tc.research_page.NODESTABILIZER.2")
            )
            .setParents("NODEPRESERVE")
            .registerResearchItem();
        new ResearchItem("NODESTABILIZERADV", "THAUMATURGY", new AspectList().add(Aspect.AURA, 9).add(Aspect.MAGIC, 6).add(Aspect.ORDER, 6).add(Aspect.ENERGY, 6), -8, -3, 2, TcStack.of("thaumcraft:node_stabilizer"))
            .setPages(
                ResearchPage.text("tc.research_page.NODESTABILIZERADV.1"),
                ResearchPage.infusion("NodeStabilizerAdv")
            )
            .setSecondary()
            .setConcealed()
            .setParents("NODESTABILIZER")
            .registerResearchItem();
        new ResearchItem("VISPOWER", "THAUMATURGY", new AspectList().add(Aspect.AURA, 3).add(Aspect.MECHANISM, 3).add(Aspect.ENERGY, 6), -5, -6, 2, TcStack.of("thaumcraft:node_transducer"))
            .setPages(
                ResearchPage.text("tc.research_page.VISPOWER.1"),
                ResearchPage.arcane("NodeTransducer"),
                ResearchPage.text("tc.research_page.VISPOWER.2"),
                ResearchPage.text("tc.research_page.VISPOWER.3"),
                ResearchPage.arcane("NodeRelay"),
                ResearchPage.text("tc.research_page.VISPOWER.4"),
                ResearchPage.text("tc.research_page.VISPOWER.5")
            )
            .setParents("NODESTABILIZER")
            .setSpecial()
            .registerResearchItem();
        new ResearchItem("VISCHARGERELAY", "THAUMATURGY", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.AURA, 3).add(Aspect.MECHANISM, 3).add(Aspect.ENERGY, 6), -7, -6, 2, TcStack.of("thaumcraft:vis_charge_relay"))
            .setPages(
                ResearchPage.text("tc.research_page.VISCHARGERELAY.1"),
                ResearchPage.arcane("NodeChargeRelay")
            )
            .setParents("VISPOWER", "WANDPED")
            .setParentsHidden("ROD_greatwood")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("FOCALMANIPULATION", "THAUMATURGY", new AspectList().add(Aspect.MAGIC, 8).add(Aspect.TOOL, 8).add(Aspect.CRAFT, 5).add(Aspect.CRYSTAL, 5).add(Aspect.ENERGY, 5), -3, -8, 2, TcStack.of("thaumcraft:focal_manipulator"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCALMANIPULATION.1"),
                ResearchPage.arcane("FocalManipulator"),
                ResearchPage.text("tc.research_page.FOCALMANIPULATION.2")
            )
            .setParentsHidden("INFUSION", "FOCUSFIRE")
            .setParents("VISPOWER")
            .registerResearchItem();
        new ResearchItem("VAMPBAT", "THAUMATURGY", new AspectList().add(Aspect.HUNGER, 5).add(Aspect.LIFE, 5).add(Aspect.MAGIC, 5), 4, -8, 1, ResourceLocation.parse("thaumcraft:textures/foci/vampirebats.png"))
            .setPages(
                ResearchPage.text("focus.upgrade.vampirebats.text")
            )
            .setSecondary()
            .setParents("FOCUSHELLBAT")
            .setParentsHidden("FOCALMANIPULATION")
            .registerResearchItem();
    }

    private static void initAlchemy() {
        initAlchemy1();
        initAlchemy2();
        initAlchemy3();
    }

    private static void initAlchemy1() {
        new ResearchItem("PHIAL", "ALCHEMY", new AspectList(), 0, -2, 0, TcStack.of("thaumcraft:glass_phial"))
            .setPages(
                ResearchPage.text("tc.research_page.PHIAL.1"),
                ResearchPage.crafting("Phial")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("CRUCIBLE", "ALCHEMY", new AspectList(), 0, 0, 0, TcStack.of("thaumcraft:crucible"))
            .setPages(
                ResearchPage.text("tc.research_page.CRUCIBLE.1"),
                ResearchPage.text("tc.research_page.CRUCIBLE.2"),
                ResearchPage.text("tc.research_page.CRUCIBLE.3"),
                ResearchPage.compound("Crucible"),
                ResearchPage.text("tc.research_page.CRUCIBLE.4"),
                ResearchPage.crucible("BalancedShard_0", "BalancedShard_1", "BalancedShard_2", "BalancedShard_3", "BalancedShard_4", "BalancedShard_5"),
                ResearchPage.text("tc.research_page.CRUCIBLE.5"),
                ResearchPage.smelting(TcStack.of("thaumcraft:balanced_shard"), TcStack.of("thaumcraft:salis_mundus"))
            )
            .setStub()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("NITOR", "ALCHEMY", new AspectList().add(Aspect.LIGHT, 3).add(Aspect.FIRE, 1), 2, -1, 1, TcStack.of("thaumcraft:nitor"))
            .setPages(
                ResearchPage.text("tc.research_page.NITOR.1"),
                ResearchPage.crucible("Nitor")
            )
            .setParents("CRUCIBLE")
            .registerResearchItem();
        new ResearchItem("ALUMENTUM", "ALCHEMY", new AspectList().add(Aspect.ENERGY, 3).add(Aspect.FIRE, 1), 2, 1, 1, TcStack.of("thaumcraft:alumentum"))
            .setPages(
                ResearchPage.text("tc.research_page.ALUMENTUM.1"),
                ResearchPage.crucible("Alumentum")
            )
            .setParents("CRUCIBLE")
            .registerResearchItem();
        new ResearchItem("ALCHEMICALDUPLICATION", "ALCHEMY", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.GREED, 3).add(Aspect.CRAFT, 3), -4, 0, 1, ResourceLocation.parse("thaumcraft:textures/misc/r_alchmult.png"))
            .setPages(
                ResearchPage.text("tc.research_page.ALCHEMICALDUPLICATION.1"),
                ResearchPage.crucible("AltGunpowder"),
                ResearchPage.crucible("AltSlime"),
                ResearchPage.crucible("AltClay"),
                ResearchPage.crucible("AltGlowstone"),
                ResearchPage.crucible("AltInk"),
                ResearchPage.crucible("AltHoneyBottle"),
                ResearchPage.crucible("AltHoneycomb")
            )
            .setConcealed()
            .setSecondary()
            .setParents("TALLOW")
            .registerResearchItem();
        new ResearchItem("ALCHEMICALMANUFACTURE", "ALCHEMY", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.EXCHANGE, 3).add(Aspect.CRAFT, 3), -5, -2, 1, ResourceLocation.parse("thaumcraft:textures/misc/r_alchman.png"))
            .setPages(
                ResearchPage.text("tc.research_page.ALCHEMICALMANUFACTURE.1"),
                ResearchPage.crucible("AltWeb"),
                ResearchPage.crucible("AltIce"),
                ResearchPage.crucible("AltGlowInk"),
                ResearchPage.crucible("AltPoisonousPotato")
            )
            .setConcealed()
            .setSecondary()
            .setParents("ALCHEMICALDUPLICATION")
            .registerResearchItem();
        new ResearchItem("ENTROPICPROCESSING", "ALCHEMY", new AspectList().add(Aspect.MAGIC, 1).add(Aspect.ENTROPY, 3).add(Aspect.CRAFT, 1), -6, 1, 1, ResourceLocation.parse("thaumcraft:textures/misc/r_alchent.png"))
            .setPages(
                ResearchPage.text("tc.research_page.ENTROPICPROCESSING.1"),
                ResearchPage.crucible("AltBonemeal"),
                ResearchPage.crucible("AltString"),
                ResearchPage.crucible("AltQuartz")
            )
            .setConcealed()
            .setSecondary()
            .setParents("ALCHEMICALDUPLICATION")
            .registerResearchItem();
        new ResearchItem("LIQUIDDEATH", "ALCHEMY", new AspectList().add(Aspect.DEATH, 3).add(Aspect.POISON, 3).add(Aspect.ENTROPY, 1).add(Aspect.WATER, 1), -7, 3, 2, TcStack.of("thaumcraft:liquid_death_bucket"))
            .setPages(
                ResearchPage.text("tc.research_page.LIQUIDDEATH.1"),
                ResearchPage.crucible("LiquidDeath")
            )
            .setHidden()
            .setAspectTriggers(Aspect.DEATH, Aspect.POISON)
            .setParents("ENTROPICPROCESSING")
            .setWarp(3)
            .registerResearchItem();
        new ResearchItem("BOTTLETAINT", "ALCHEMY", new AspectList().add(Aspect.TAINT, 5).add(Aspect.MAGIC, 3).add(Aspect.ENTROPY, 1).add(Aspect.WATER, 1), -8, 1, 2, TcStack.of("thaumcraft:bottled_taint"))
            .setPages(
                ResearchPage.text("tc.research_page.BOTTLETAINT.1"),
                ResearchPage.crucible("BottleTaint")
            )
            .setHidden()
            .setAspectTriggers(Aspect.TAINT)
            .setParents("ENTROPICPROCESSING")
            .setWarp(2)
            .registerResearchItem();
        new ResearchItem("THAUMIUM", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.MAGIC, 3), -1, 3, 1, TcStack.of("thaumcraft:thaumium_ingot"))
            .setPages(
                ResearchPage.text("tc.research_page.THAUMIUM.1"),
                ResearchPage.crucible("Thaumium"),
                ResearchPage.crafting("ThaumiumAxe"),
                ResearchPage.crafting("ThaumiumSword"),
                ResearchPage.crafting("ThaumiumPick"),
                ResearchPage.crafting("ThaumiumShovel"),
                ResearchPage.crafting("ThaumiumHoe"),
                ResearchPage.crafting("ThaumiumHelm"),
                ResearchPage.crafting("ThaumiumChest"),
                ResearchPage.crafting("ThaumiumLegs"),
                ResearchPage.crafting("ThaumiumBoots")
            )
            .setHidden()
            .setAspectTriggers(Aspect.METAL)
            .setParents("CRUCIBLE")
            .registerResearchItem();
        new ResearchItem("PUREIRON", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.ORDER, 3), -2, 5, 1, TcStack.of("thaumcraft:native_iron_cluster"))
            .setPages(
                ResearchPage.text("tc.research_page.PUREIRON.1"),
                ResearchPage.crucible("PureIron")
            )
            .setConcealed()
            .setParents("THAUMIUM")
            .registerResearchItem();
        new ResearchItem("PUREGOLD", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.ORDER, 2).add(Aspect.GREED, 1), -4, 3, 1, TcStack.of("thaumcraft:native_gold_cluster"))
            .setPages(
                ResearchPage.text("tc.research_page.PUREGOLD.1"),
                ResearchPage.crucible("PureGold")
            )
            .setConcealed()
            .setSecondary()
            .setParents("PUREIRON")
            .registerResearchItem();
    }

    private static void initAlchemy2() {
        new ResearchItem("PURECOPPER", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.ORDER, 2).add(Aspect.EXCHANGE, 1), -4, 5, 1, TcStack.of("thaumcraft:native_copper_cluster"))
            .setPages(
                ResearchPage.text("tc.research_page.PURECOPPER.1"),
                ResearchPage.crucible("PureCopper")
            )
            .setConcealed()
            .setSecondary()
            .setParents("PUREIRON")
            .registerResearchItem();
        new ResearchItem("PURETIN", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.ORDER, 2).add(Aspect.CRYSTAL, 1), -4, 7, 1, TcStack.of("thaumcraft:native_tin_cluster"))
            .setPages(
                ResearchPage.text("tc.research_page.PURETIN.1"),
                ResearchPage.crucible("PureTin")
            )
            .setConcealed()
            .setSecondary()
            .setParents("PUREIRON")
            .registerResearchItem();
        new ResearchItem("PURESILVER", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.ORDER, 2).add(Aspect.GREED, 1), -3, 8, 1, TcStack.of("thaumcraft:native_silver_cluster"))
            .setPages(
                ResearchPage.text("tc.research_page.PURESILVER.1"),
                ResearchPage.crucible("PureSilver")
            )
            .setConcealed()
            .setSecondary()
            .setParents("PUREIRON")
            .registerResearchItem();
        new ResearchItem("PURELEAD", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.ORDER, 3), -2, 9, 1, TcStack.of("thaumcraft:native_lead_cluster"))
            .setPages(
                ResearchPage.text("tc.research_page.PURELEAD.1"),
                ResearchPage.crucible("PureLead")
            )
            .setConcealed()
            .setSecondary()
            .setParents("PUREIRON")
            .registerResearchItem();
        new ResearchItem("TRANSIRON", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 3), 0, 5, 1, TcStack.of("minecraft:iron_nugget"))
            .setPages(
                ResearchPage.text("tc.research_page.TRANSIRON.1"),
                ResearchPage.crucible("TransIron")
            )
            .setConcealed()
            .setParents("THAUMIUM")
            .registerResearchItem();
        new ResearchItem("TRANSGOLD", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 3), 2, 3, 1, TcStack.of("minecraft:gold_nugget"))
            .setPages(
                ResearchPage.text("tc.research_page.TRANSGOLD.1"),
                ResearchPage.crucible("TransGold")
            )
            .setConcealed()
            .setSecondary()
            .setParents("TRANSIRON")
            .registerResearchItem();
        new ResearchItem("TRANSCOPPER", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 3), 2, 5, 1, TcStack.of("thaumcraft:copper_nugget"))
            .setPages(
                ResearchPage.text("tc.research_page.TRANSCOPPER.1"),
                ResearchPage.crucible("TransCopper")
            )
            .setConcealed()
            .setSecondary()
            .setParents("TRANSIRON")
            .registerResearchItem();
        new ResearchItem("TRANSTIN", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 2).add(Aspect.CRYSTAL, 1), 2, 7, 1, TcStack.of("thaumcraft:tin_nugget"))
            .setPages(
                ResearchPage.text("tc.research_page.TRANSTIN.1"),
                ResearchPage.crucible("TransTin")
            )
            .setConcealed()
            .setSecondary()
            .setParents("TRANSIRON")
            .registerResearchItem();
        new ResearchItem("TRANSSILVER", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 2).add(Aspect.GREED, 1), 1, 8, 1, TcStack.of("thaumcraft:silver_nugget"))
            .setPages(
                ResearchPage.text("tc.research_page.TRANSSILVER.1"),
                ResearchPage.crucible("TransSilver")
            )
            .setConcealed()
            .setSecondary()
            .setParents("TRANSIRON")
            .registerResearchItem();
        new ResearchItem("TRANSLEAD", "ALCHEMY", new AspectList().add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 2).add(Aspect.ORDER, 1), 0, 9, 1, TcStack.of("thaumcraft:lead_nugget"))
            .setPages(
                ResearchPage.text("tc.research_page.TRANSLEAD.1"),
                ResearchPage.crucible("TransLead")
            )
            .setConcealed()
            .setSecondary()
            .setParents("TRANSIRON")
            .registerResearchItem();
        new ResearchItem("TALLOW", "ALCHEMY", new AspectList().add(Aspect.FLESH, 3).add(Aspect.MAGIC, 1), -2, 0, 1, TcStack.of("thaumcraft:magic_tallow"))
            .setPages(
                ResearchPage.text("tc.research_page.TALLOW.1"),
                ResearchPage.crucible("Tallow"),
                ResearchPage.crafting("TallowCandle")
            )
            .setParents("CRUCIBLE")
            .registerResearchItem();
        new ResearchItem("ETHEREALBLOOM", "ALCHEMY", new AspectList().add(Aspect.MAGIC, 1).add(Aspect.PLANT, 6).add(Aspect.HEAL, 3).add(Aspect.TAINT, 6), -2, -3, 2, TcStack.of("thaumcraft:ethereal_bloom"))
            .setPages(
                ResearchPage.text("tc.research_page.ETHEREALBLOOM.1"),
                ResearchPage.crucible("EtherealBloom"),
                ResearchPage.text("tc.research_page.ETHEREALBLOOM.2")
            )
            .setHidden()
            .setAspectTriggers(Aspect.TAINT)
            .setConcealed()
            .setParents("CRUCIBLE")
            .registerResearchItem();
    }

    private static void initAlchemy3() {
        new ResearchItem("BATHSALTS", "ALCHEMY", new AspectList().add(Aspect.MIND, 3).add(Aspect.AURA, 3).add(Aspect.ORDER, 3).add(Aspect.HEAL, 3), -4, -4, 2, TcStack.of("thaumcraft:bath_salts"))
            .setPages(
                ResearchPage.text("tc.research_page.BATHSALTS.1"),
                ResearchPage.crucible("BathSalts")
            )
            .setHidden()
            .registerResearchItem();
        new ResearchItem("SANESOAP", "ALCHEMY", new AspectList().add(Aspect.MIND, 5).add(Aspect.ORDER, 5).add(Aspect.HEAL, 5).add(Aspect.ELDRITCH, 5), -3, -6, 1, TcStack.of("thaumcraft:sanitizing_soap"))
            .setPages(
                ResearchPage.text("tc.research_page.SANESOAP.1"),
                ResearchPage.crucible("SaneSoap")
            )
            .setParents("BATHSALTS")
            .registerResearchItem();
        new ResearchItem("ARCANESPA", "ALCHEMY", new AspectList().add(Aspect.WATER, 3).add(Aspect.MECHANISM, 3).add(Aspect.ORDER, 3), -6, -5, 1, TcStack.of("thaumcraft:arcane_spa"))
            .setPages(
                ResearchPage.text("tc.research_page.ARCANESPA.1"),
                ResearchPage.arcane("ArcaneSpa")
            )
            .setSecondary()
            .setParents("BATHSALTS")
            .registerResearchItem();
        new ResearchItem("DISTILESSENTIA", "ALCHEMY", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.WATER, 3).add(Aspect.SLIME, 3), 5, -1, 1, TcStack.of("thaumcraft:alembic"))
            .setPages(
                ResearchPage.text("tc.research_page.DISTILESSENTIA.1"),
                ResearchPage.arcane("AlchemyFurnace"),
                ResearchPage.text("tc.research_page.DISTILESSENTIA.2"),
                ResearchPage.arcane("Filter"),
                ResearchPage.arcane("Alembic"),
                ResearchPage.arcane("AlchemicalConstruct")
            )
            .setSiblings("JARLABEL")
            .setParents("NITOR", "ALUMENTUM")
            .registerResearchItem();
        new ResearchItem("JARLABEL", "ALCHEMY", new AspectList(), 4, -3, 0, TcStack.of("thaumcraft:warded_jar"))
            .setPages(
                ResearchPage.text("tc.research_page.JARLABEL.1"),
                ResearchPage.arcane("WardedJar"),
                ResearchPage.text("tc.research_page.JARLABEL.2"),
                ResearchPage.crafting("JarLabel"),
                ResearchPage.text("tc.research_page.JARLABEL.3"),
                ResearchPage.crafting("JarLabel0", "JarLabel1", "JarLabel2", "JarLabel3", "JarLabel4", "JarLabel5", "JarLabel6", "JarLabel7", "JarLabel8", "JarLabel9", "JarLabel10", "JarLabel11", "JarLabel12", "JarLabel13", "JarLabel14", "JarLabel15", "JarLabel16", "JarLabel17", "JarLabel18", "JarLabel19", "JarLabel20", "JarLabel21", "JarLabel22", "JarLabel23", "JarLabel24", "JarLabel25", "JarLabel26", "JarLabel27", "JarLabel28", "JarLabel29", "JarLabel30", "JarLabel31", "JarLabel32", "JarLabel33", "JarLabel34", "JarLabel35", "JarLabel36", "JarLabel37", "JarLabel38", "JarLabel39", "JarLabel40", "JarLabel41", "JarLabel42", "JarLabel43", "JarLabel44", "JarLabel45", "JarLabel46", "JarLabel47"),
                ResearchPage.crafting("JarLabelNull")
            )
            .setParents("DISTILESSENTIA")
            .setStub()
            .setRound()
            .registerResearchItem();
        new ResearchItem("JARVOID", "ALCHEMY", new AspectList().add(Aspect.WATER, 3).add(Aspect.ENTROPY, 3).add(Aspect.VOID, 6), 5, -5, 1, TcStack.of("thaumcraft:void_jar"))
            .setPages(
                ResearchPage.text("tc.research_page.JARVOID.1"),
                ResearchPage.arcane("JarVoid")
            )
            .setParents("JARLABEL")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("TUBES", "ALCHEMY", new AspectList().add(Aspect.WATER, 3).add(Aspect.MAGIC, 3).add(Aspect.EXCHANGE, 3), 7, 0, 1, TcStack.of("thaumcraft:essentia_tube"))
            .setPages(
                ResearchPage.text("tc.research_page.TUBES.1"),
                ResearchPage.arcane("Tube"),
                ResearchPage.text("tc.research_page.TUBES.2"),
                ResearchPage.arcane("TubeValve"),
                ResearchPage.text("tc.research_page.TUBES.3"),
                ResearchPage.arcane("Resonator"),
                ResearchPage.text("tc.research_page.TUBES.4")
            )
            .setParents("DISTILESSENTIA")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("TUBEFILTER", "ALCHEMY", new AspectList().add(Aspect.WATER, 3).add(Aspect.MAGIC, 3).add(Aspect.EXCHANGE, 3).add(Aspect.ORDER, 3), 9, 1, 2, TcStack.of("thaumcraft:filtered_essentia_tube"))
            .setPages(
                ResearchPage.text("tc.research_page.TUBEFILTER.1"),
                ResearchPage.arcane("TubeFilter"),
                ResearchPage.text("tc.research_page.TUBEFILTER.2"),
                ResearchPage.arcane("TubeRestrict"),
                ResearchPage.arcane("TubeOneway")
            )
            .setParents("TUBES")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("ESSENTIACRYSTAL", "ALCHEMY", new AspectList().add(Aspect.WATER, 5).add(Aspect.CRYSTAL, 5).add(Aspect.EXCHANGE, 3).add(Aspect.MAGIC, 5), 8, -2, 1, TcStack.of("thaumcraft:essentia_crystallizer"))
            .setPages(
                ResearchPage.text("tc.research_page.ESSENTIACRYSTAL.1"),
                ResearchPage.arcane("EssentiaCrystalizer")
            )
            .setConcealed()
            .setParents("TUBES")
            .registerResearchItem();
        new ResearchItem("CENTRIFUGE", "ALCHEMY", new AspectList().add(Aspect.ENTROPY, 3).add(Aspect.MAGIC, 3).add(Aspect.EXCHANGE, 3).add(Aspect.CRAFT, 3), 10, 0, 2, TcStack.of("thaumcraft:alchemical_centrifuge"))
            .setPages(
                ResearchPage.text("tc.research_page.CENTRIFUGE.1"),
                ResearchPage.arcane("Centrifuge"),
                ResearchPage.text("tc.research_page.CENTRIFUGE.2"),
                ResearchPage.text("tc.research_page.CENTRIFUGE.3"),
                ResearchPage.arcane("TubeBuffer")
            )
            .setParents("TUBEFILTER")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("THAUMATORIUM", "ALCHEMY", new AspectList().add(Aspect.WATER, 3).add(Aspect.MAGIC, 6).add(Aspect.EXCHANGE, 3).add(Aspect.CRAFT, 3), 10, -2, 3, ResourceLocation.parse("thaumcraft:textures/block/alchemyblock.png"))
            .setPages(
                ResearchPage.text("tc.research_page.THAUMATORIUM.1"),
                ResearchPage.compound("Thaumatorium"),
                ResearchPage.text("tc.research_page.THAUMATORIUM.2"),
                ResearchPage.text("tc.research_page.THAUMATORIUM.3"),
                ResearchPage.arcane("MnemonicMatrix")
            )
            .setParents("CENTRIFUGE")
            .setConcealed()
            .registerResearchItem();
    }

    private static void initArtifice() {
        initArtifice1();
        initArtifice2();
        initArtifice3();
        initArtifice4();
        initArtifice5();
    }

    private static void initArtifice1() {
        new ResearchItem("BASICARTIFACE", "ARTIFICE", new AspectList(), 0, 1, 0, TcStack.of("thaumcraft:primal_charm"))
            .setPages(
                ResearchPage.text("tc.research_page.BASICARTIFACE.1"),
                ResearchPage.arcane("PrimalCharm"),
                ResearchPage.crafting("MundaneAmulet"),
                ResearchPage.crafting("MundaneRing"),
                ResearchPage.crafting("MundaneBelt"),
                ResearchPage.arcane("MirrorGlass")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem("ARCANESTONE", "ARTIFICE", new AspectList(), 5, -2, 0, TcStack.of("thaumcraft:arcane_stone"))
            .setPages(
                ResearchPage.text("tc.research_page.ARCANESTONE.1"),
                ResearchPage.arcane("ArcaneStone1"),
                ResearchPage.crafting("ArcaneStone2"),
                ResearchPage.crafting("ArcaneStone3"),
                ResearchPage.crafting("ArcaneStone4")
            )
            .setStub()
            .setAutoUnlock()
            .setRound()
            .registerResearchItem();
        new ResearchItem("GRATE", "ARTIFICE", new AspectList(), 2, -1, 0, TcStack.of("thaumcraft:item_grate"))
            .setPages(
                ResearchPage.text("tc.research_page.GRATE.1"),
                ResearchPage.crafting("Grate")
            )
            .setStub()
            .setAutoUnlock()
            .setRound()
            .registerResearchItem();
        new ResearchItem("TABLE", "ARTIFICE", new AspectList(), 0, -1, 0, TcStack.of("thaumcraft:table"))
            .setPages(
                ResearchPage.text("tc.research_page.TABLE.1"),
                ResearchPage.crafting("Table")
            )
            .setStub()
            .setAutoUnlock()
            .setRound()
            .registerResearchItem();
        new ResearchItem("ARCTABLE", "ARTIFICE", new AspectList(), -1, -3, 0, TcStack.of("thaumcraft:arcane_worktable"))
            .setPages(
                ResearchPage.text("tc.research_page.ARCTABLE.1"),
                ResearchPage.compound("ArcTable")
            )
            .setStub()
            .setAutoUnlock()
            .setRound()
            .setParents("TABLE")
            .registerResearchItem();
        new ResearchItem("RESTABLE", "ARTIFICE", new AspectList(), 1, -3, 0, TcStack.of("thaumcraft:table"))
            .setPages(
                ResearchPage.text("tc.research_page.RESTABLE.1"),
                ResearchPage.compound("ResTable")
            )
            .setStub()
            .setAutoUnlock()
            .setRound()
            .setParents("TABLE")
            .registerResearchItem();
        new ResearchItem("THAUMOMETER", "ARTIFICE", new AspectList(), 2, 1, 0, TcStack.of("thaumcraft:thaumometer"))
            .setStub()
            .setAutoUnlock()
            .setRound()
            .setPages(
                ResearchPage.text("tc.research_page.THAUMOMETER.1"),
                ResearchPage.crafting("Thaumometer")
            )
            .registerResearchItem();
        new ResearchItem("PAVETRAVEL", "ARTIFICE", new AspectList().add(Aspect.TRAVEL, 3).add(Aspect.EARTH, 3).add(Aspect.FLIGHT, 3), 4, -4, 1, TcStack.of("thaumcraft:travel_paving_stone"))
            .setPages(
                ResearchPage.text("tc.research_page.PAVETRAVEL.1"),
                ResearchPage.arcane("PaveTravel")
            )
            .setParents("ARCANESTONE")
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("PAVEWARD", "ARTIFICE", new AspectList().add(Aspect.MOTION, 3).add(Aspect.TRAP, 3).add(Aspect.BEAST, 3), 6, -4, 1, TcStack.of("thaumcraft:warding_paving_stone"))
            .setPages(
                ResearchPage.text("tc.research_page.PAVEWARD.1"),
                ResearchPage.arcane("PaveWard"),
                ResearchPage.text("tc.research_page.PAVEWARD.2")
            )
            .setParents("ARCANESTONE")
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("GOGGLES", "ARTIFICE", new AspectList().add(Aspect.SENSES, 3).add(Aspect.AURA, 3).add(Aspect.MAGIC, 3), 4, 1, 1, TcStack.of("thaumcraft:goggles_of_revealing"))
            .setPages(
                ResearchPage.text("tc.research_page.GOGGLES.1"),
                ResearchPage.arcane("Goggles")
            )
            .setParents("THAUMOMETER")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("ARCANEEAR", "ARTIFICE", new AspectList().add(Aspect.SENSES, 3).add(Aspect.ENERGY, 3).add(Aspect.AIR, 3), 6, 0, 1, TcStack.of("thaumcraft:arcane_ear"))
            .setPages(
                ResearchPage.text("tc.research_page.ARCANEEAR.1"),
                ResearchPage.arcane("ArcaneEar")
            )
            .setParents("GOGGLES")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("SINSTONE", "ARTIFICE", new AspectList().add(Aspect.SENSES, 3).add(Aspect.DARKNESS, 3).add(Aspect.ELDRITCH, 3).add(Aspect.AURA, 3), 6, 2, 1, TcStack.of("thaumcraft:sinister_lodestone"))
            .setPages(
                ResearchPage.text("tc.research_page.SINSTONE.1"),
                ResearchPage.infusion("SinStone")
            )
            .setParents("GOGGLES")
            .setConcealed()
            .setWarp(2)
            .registerResearchItem();
    }

    private static void initArtifice2() {
        new ResearchItem("LEVITATOR", "ARTIFICE", new AspectList().add(Aspect.MOTION, 3).add(Aspect.FLIGHT, 3).add(Aspect.AIR, 3), -3, -3, 1, TcStack.of("thaumcraft:arcane_levitator"))
            .setPages(
                ResearchPage.text("tc.research_page.LEVITATOR.1"),
                ResearchPage.arcane("Levitator")
            )
            .setConcealed()
            .setParents("NITOR")
            .registerResearchItem();
        new ResearchItem("INFERNALFURNACE", "ARTIFICE", new AspectList().add(Aspect.FIRE, 6).add(Aspect.METAL, 3).add(Aspect.CRAFT, 3).add(Aspect.AURA, 3), -4, -1, 2, ResourceLocation.parse("thaumcraft:textures/misc/r_infernalfurnace.png"))
            .setPages(
                ResearchPage.text("tc.research_page.INFERNALFURNACE.1"),
                ResearchPage.compound("InfernalFurnace"),
                ResearchPage.text("tc.research_page.INFERNALFURNACE.2")
            )
            .setParents("NITOR", "ALUMENTUM")
            .setConcealed()
            .setWarp(2)
            .registerResearchItem();
        new ResearchItem("BELLOWS", "ARTIFICE", new AspectList().add(Aspect.AIR, 6).add(Aspect.MECHANISM, 3).add(Aspect.MOTION, 3), -6, -2, 1, TcStack.of("thaumcraft:arcane_bellows"))
            .setPages(
                ResearchPage.text("tc.research_page.BELLOWS.1"),
                ResearchPage.arcane("Bellows"),
                ResearchPage.text("tc.research_page.BELLOWS.2")
            )
            .setParents("INFERNALFURNACE")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("ENCHFABRIC", "ARTIFICE", new AspectList().add(Aspect.CLOTH, 3).add(Aspect.MAGIC, 3), 0, 3, 1, TcStack.of("thaumcraft:enchanted_fabric"))
            .setPages(
                ResearchPage.text("tc.research_page.ENCHFABRIC.1"),
                ResearchPage.arcane("EnchantedFabric"),
                ResearchPage.text("tc.research_page.ENCHFABRIC.2"),
                ResearchPage.arcane("RobeChest"),
                ResearchPage.arcane("RobeLegs"),
                ResearchPage.arcane("RobeBoots")
            )
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("INFUSION", "ARTIFICE", new AspectList().add(Aspect.MAGIC, 6).add(Aspect.MECHANISM, 3).add(Aspect.CRAFT, 6), -4, 5, 2, TcStack.of("thaumcraft:runic_matrix"))
            .setPages(
                ResearchPage.text("tc.research_page.INFUSION.1"),
                ResearchPage.arcane("InfusionMatrix"),
                ResearchPage.arcane("ArcanePedestal"),
                ResearchPage.text("tc.research_page.INFUSION.2"),
                ResearchPage.compound("InfusionAltar"),
                ResearchPage.text("tc.research_page.INFUSION.3"),
                ResearchPage.text("tc.research_page.INFUSION.4"),
                ResearchPage.text("tc.research_page.INFUSION.5")
            )
            .setParents("DISTILESSENTIA")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("FLUXSCRUB", "ARTIFICE", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.TRAP, 3).add(Aspect.AIR, 3).add(Aspect.WATER, 3), -8, -3, 1, TcStack.of("thaumcraft:flux_scrubber"))
            .setPages(
                ResearchPage.text("tc.research_page.FLUXSCRUB.1"),
                ResearchPage.arcane("FluxScrubber")
            )
            .setParentsHidden("INFUSION")
            .setParents("VISPOWER", "BELLOWS", "TUBES")
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("RUNICARMOR", "ARTIFICE", new AspectList().add(Aspect.ARMOR, 6).add(Aspect.AIR, 3).add(Aspect.MAGIC, 3).add(Aspect.ENERGY, 3).add(Aspect.MIND, 3), 3, 4, 3, TcStack.of("thaumcraft:runic_shielding_ring"))
            .setPages(
                ResearchPage.text("tc.research_page.RUNICARMOR.1"),
                ResearchPage.text("tc.research_page.RUNICARMOR.2"),
                ResearchPage.infusion("RunicRing"),
                ResearchPage.infusion("RunicAmulet"),
                ResearchPage.infusion("RunicGirdle")
            )
            .setParentsHidden("INFUSION")
            .setParents("ENCHFABRIC")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("RUNICCHARGED", "ARTIFICE", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ARMOR, 3).add(Aspect.ENERGY, 6), 2, 3, 2, TcStack.of("thaumcraft:charged_shielding_ring"))
            .setPages(
                ResearchPage.text("tc.research_page.RUNICCHARGED.1"),
                ResearchPage.infusion("RunicRingCharged")
            )
            .setParents("RUNICARMOR")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("RUNICHEALING", "ARTIFICE", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ARMOR, 3).add(Aspect.HEAL, 4).add(Aspect.WATER, 4), 4, 3, 2, TcStack.of("thaumcraft:revitalizing_shielding_ring"))
            .setPages(
                ResearchPage.text("tc.research_page.RUNICHEALING.1"),
                ResearchPage.infusion("RunicRingHealing")
            )
            .setParents("RUNICARMOR")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("RUNICKINETIC", "ARTIFICE", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ARMOR, 3).add(Aspect.AIR, 6), 2, 5, 2, TcStack.of("thaumcraft:kinetic_shielding_girdle"))
            .setPages(
                ResearchPage.text("tc.research_page.RUNICKINETIC.1"),
                ResearchPage.infusion("RunicGirdleKinetic")
            )
            .setParents("RUNICARMOR")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("RUNICEMERGENCY", "ARTIFICE", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ARMOR, 3).add(Aspect.EARTH, 4).add(Aspect.VOID, 4), 4, 5, 2, TcStack.of("thaumcraft:emergency_shielding_amulet"))
            .setPages(
                ResearchPage.text("tc.research_page.RUNICEMERGENCY.1"),
                ResearchPage.infusion("RunicAmuletEmergency")
            )
            .setParents("RUNICARMOR")
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("BANNERS", "ARTIFICE", new AspectList().add(Aspect.SENSES, 3).add(Aspect.CLOTH, 3).add(Aspect.MAGIC, 1), 4, 8, 1, TcStack.of("thaumcraft:banner"))
            .setPages(
                ResearchPage.text("tc.research_page.BANNERS.1"),
                ResearchPage.arcane("Banner_0", "Banner_1", "Banner_2", "Banner_3", "Banner_4", "Banner_5", "Banner_6", "Banner_7", "Banner_8", "Banner_9", "Banner_10", "Banner_11", "Banner_12", "Banner_13", "Banner_14", "Banner_15")
            )
            .setHidden()
            .setItemTriggers("thaumcraft:banner")
            .setSecondary()
            .registerResearchItem();
    }

    private static void initArtifice3() {
        TcRecipes.register(new InfusionRecipe("ConfigResearch.RunicAugment0", "RUNICAUGMENTATION", TcStack.of("thaumcraft:robe_chestplate"), 5, new AspectList().add(Aspect.ARMOR, 16).add(Aspect.MAGIC, 16).add(Aspect.ENERGY, 32), TcIngredient.item("thaumcraft:robe_chestplate"), List.of(TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:diamond"), TcIngredient.item("thaumcraft:salis_mundus"))));
        TcRecipes.register(new InfusionRecipe("ConfigResearch.RunicAugment1", "RUNICAUGMENTATION", TcStack.of("thaumcraft:robe_chestplate"), 5, new AspectList().add(Aspect.ARMOR, 32).add(Aspect.MAGIC, 32).add(Aspect.ENERGY, 64), TcIngredient.item("thaumcraft:robe_chestplate"), List.of(TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:diamond"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"))));
        TcRecipes.register(new InfusionRecipe("ConfigResearch.RunicAugment2", "RUNICAUGMENTATION", TcStack.of("thaumcraft:robe_chestplate"), 6, new AspectList().add(Aspect.ARMOR, 64).add(Aspect.MAGIC, 64).add(Aspect.ENERGY, 128), TcIngredient.item("thaumcraft:robe_chestplate"), List.of(TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:diamond"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"))));
        TcRecipes.register(new InfusionRecipe("ConfigResearch.RunicAugment3", "RUNICAUGMENTATION", TcStack.of("thaumcraft:robe_chestplate"), 6, new AspectList().add(Aspect.ARMOR, 128).add(Aspect.MAGIC, 128).add(Aspect.ENERGY, 256), TcIngredient.item("thaumcraft:robe_chestplate"), List.of(TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:diamond"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"))));
        TcRecipes.register(new InfusionRecipe("ConfigResearch.RunicAugment4", "RUNICAUGMENTATION", TcStack.of("thaumcraft:robe_chestplate"), 7, new AspectList().add(Aspect.ARMOR, 256).add(Aspect.MAGIC, 256).add(Aspect.ENERGY, 512), TcIngredient.item("thaumcraft:robe_chestplate"), List.of(TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:shield"), TcIngredient.item("minecraft:diamond"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"), TcIngredient.item("thaumcraft:salis_mundus"))));
        new ResearchItem("RUNICAUGMENTATION", "ARTIFICE", new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ARMOR, 3).add(Aspect.EXCHANGE, 4).add(Aspect.GREED, 4), 6, 4, 1, ResourceLocation.parse("thaumcraft:textures/misc/r_runicupg.png"))
            .setPages(
                ResearchPage.text("tc.research_page.RUNICAUGMENTATION.1"),
                ResearchPage.infusion("ConfigResearch.RunicAugment0", "ConfigResearch.RunicAugment1", "ConfigResearch.RunicAugment2", "ConfigResearch.RunicAugment3", "ConfigResearch.RunicAugment4"),
                ResearchPage.text("tc.research_page.RUNICAUGMENTATION.2")
            )
            .setParents("RUNICARMOR")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("BOOTSTRAVELLER", "ARTIFICE", new AspectList().add(Aspect.TRAVEL, 3).add(Aspect.EARTH, 3).add(Aspect.FLIGHT, 3).add(Aspect.WATER, 3), -1, 5, 2, TcStack.of("thaumcraft:boots_traveller"))
            .setPages(
                ResearchPage.text("tc.research_page.BOOTSTRAVELLER.1"),
                ResearchPage.infusion("BootsTraveller")
            )
            .setParents("ENCHFABRIC", "INFUSION")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("HOVERHARNESS", "ARTIFICE", new AspectList().add(Aspect.FLIGHT, 6).add(Aspect.TRAVEL, 6).add(Aspect.AIR, 6).add(Aspect.MECHANISM, 3), 1, 7, 3, TcStack.of("thaumcraft:hover_harness"))
            .setPages(
                ResearchPage.text("tc.research_page.HOVERHARNESS.1"),
                ResearchPage.infusion("HoverHarness"),
                ResearchPage.text("tc.research_page.HOVERHARNESS.2")
            )
            .setParents("BOOTSTRAVELLER")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("HOVERGIRDLE", "ARTIFICE", new AspectList().add(Aspect.FLIGHT, 6).add(Aspect.TRAVEL, 3).add(Aspect.AIR, 3).add(Aspect.MOTION, 6), 2, 7, 3, TcStack.of("thaumcraft:thaumostatic_girdle"))
            .setPages(
                ResearchPage.text("tc.research_page.HOVERGIRDLE.1"),
                ResearchPage.infusion("HoverGirdle")
            )
            .setHidden()
            .setAspectTriggers(Aspect.FLIGHT)
            .setParents("HOVERHARNESS")
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("MIRROR", "ARTIFICE", new AspectList().add(Aspect.TRAVEL, 6).add(Aspect.ELDRITCH, 3).add(Aspect.DARKNESS, 3).add(Aspect.CRYSTAL, 3), -1, 8, 2, TcStack.of("thaumcraft:magic_mirror"))
            .setPages(
                ResearchPage.text("tc.research_page.MIRROR.1"),
                ResearchPage.text("tc.research_page.MIRROR.2"),
                ResearchPage.infusion("Mirror"),
                ResearchPage.text("tc.research_page.MIRROR.3")
            )
            .setHidden()
            .setEntityTriggers("minecraft:enderman")
            .setItemTriggers("minecraft:ender_pearl", "minecraft:end_portal_frame")
            .setParents("INFUSION")
            .registerResearchItem();
        new ResearchItem("MIRRORHAND", "ARTIFICE", new AspectList().add(Aspect.TOOL, 6).add(Aspect.ELDRITCH, 3).add(Aspect.CRYSTAL, 3).add(Aspect.TRAVEL, 3), 1, 9, 2, TcStack.of("thaumcraft:hand_mirror"))
            .setPages(
                ResearchPage.text("tc.research_page.MIRRORHAND.1"),
                ResearchPage.infusion("MirrorHand")
            )
            .setConcealed()
            .setSecondary()
            .setParents("MIRROR")
            .registerResearchItem();
        new ResearchItem("MIRRORESSENTIA", "ARTIFICE", new AspectList().add(Aspect.TRAVEL, 6).add(Aspect.ELDRITCH, 3).add(Aspect.WATER, 3).add(Aspect.MAGIC, 3), -1, 10, 2, TcStack.of("thaumcraft:essentia_mirror"))
            .setPages(
                ResearchPage.text("tc.research_page.MIRRORESSENTIA.1"),
                ResearchPage.infusion("MirrorEssentia"),
                ResearchPage.text("tc.research_page.MIRRORESSENTIA.2")
            )
            .setSecondary()
            .setConcealed()
            .setParents("MIRROR")
            .registerResearchItem();
        new ResearchItem("ARCANEBORE", "ARTIFICE", new AspectList().add(Aspect.MINE, 6).add(Aspect.MOTION, 3).add(Aspect.MECHANISM, 3).add(Aspect.TOOL, 3), -3, 8, 2, TcStack.of("thaumcraft:arcane_bore"))
            .setPages(
                ResearchPage.text("tc.research_page.ARCANEBORE.1"),
                ResearchPage.infusion("ArcaneBore"),
                ResearchPage.text("tc.research_page.ARCANEBORE.2"),
                ResearchPage.arcane("ArcaneBoreBase"),
                ResearchPage.text("tc.research_page.ARCANEBORE.3")
            )
            .setConcealed()
            .setParents("FOCUSEXCAVATION", "INFUSION")
            .registerResearchItem();
        new ResearchItem("ARCANELAMP", "ARTIFICE", new AspectList().add(Aspect.LIGHT, 3).add(Aspect.SENSES, 3).add(Aspect.DARKNESS, 3), -3, 1, 1, TcStack.of("thaumcraft:arcane_lamp"))
            .setPages(
                ResearchPage.text("tc.research_page.ARCANELAMP.1"),
                ResearchPage.arcane("ArcaneLamp"),
                ResearchPage.concealed("ARCANEBORE", "tc.research_page.ARCANELAMP.2")
            )
            .setSecondary()
            .setParents("NITOR")
            .registerResearchItem();
        new ResearchItem("LAMPGROWTH", "ARTIFICE", new AspectList().add(Aspect.LIGHT, 3).add(Aspect.PLANT, 6).add(Aspect.LIFE, 3).add(Aspect.CROP, 3), -4, 3, 2, TcStack.of("thaumcraft:lamp_of_growth"))
            .setPages(
                ResearchPage.text("tc.research_page.LAMPGROWTH.1"),
                ResearchPage.infusion("LampGrowth")
            )
            .setHidden()
            .setAspectTriggers(Aspect.LIGHT, Aspect.CROP)
            .setParents("ARCANELAMP", "INFUSION")
            .registerResearchItem();
        new ResearchItem("LAMPFERTILITY", "ARTIFICE", new AspectList().add(Aspect.BEAST, 6).add(Aspect.LIFE, 6).add(Aspect.LIGHT, 3), -2, 3, 2, TcStack.of("thaumcraft:lamp_of_fertility"))
            .setPages(
                ResearchPage.text("tc.research_page.LAMPFERTILITY.1"),
                ResearchPage.infusion("LampFertility")
            )
            .setHidden()
            .setAspectTriggers(Aspect.LIGHT, Aspect.LIFE)
            .setParents("ARCANELAMP", "INFUSION")
            .registerResearchItem();
        new ResearchItem("BONEBOW", "ARTIFICE", new AspectList().add(Aspect.WEAPON, 3).add(Aspect.AIR, 3).add(Aspect.MOTION, 3), -7, 1, 1, TcStack.of("thaumcraft:bone_bow"))
            .setPages(
                ResearchPage.text("tc.research_page.BONEBOW.1"),
                ResearchPage.arcane("BoneBow")
            )
            .setHidden()
            .setItemTriggers("minecraft:bow", "minecraft:bone")
            .registerResearchItem();
    }

    private static void initArtifice4() {
        new ResearchItem("PRIMALARROW", "ARTIFICE", new AspectList().add(Aspect.WEAPON, 3).add(Aspect.AIR, 3).add(Aspect.FIRE, 3).add(Aspect.WATER, 3).add(Aspect.EARTH, 3).add(Aspect.ORDER, 3).add(Aspect.ENTROPY, 3), -9, 0, 2, TcStack.of("thaumcraft:air_arrow"))
            .setPages(
                ResearchPage.text("tc.research_page.PRIMALARROW.1"),
                ResearchPage.arcane("PrimalArrow_0", "PrimalArrow_1", "PrimalArrow_2", "PrimalArrow_3", "PrimalArrow_4", "PrimalArrow_5"),
                ResearchPage.text("tc.research_page.PRIMALARROW.2"),
                ResearchPage.text("tc.research_page.PRIMALARROW.3")
            )
            .setConcealed()
            .setParents("BONEBOW")
            .registerResearchItem();
        new ResearchItem("ELEMENTALAXE", "ARTIFICE", new AspectList().add(Aspect.TOOL, 3).add(Aspect.WATER, 3).add(Aspect.MOTION, 3), -7, 4, 2, TcStack.of("thaumcraft:elemental_axe"))
            .setPages(
                ResearchPage.text("tc.research_page.ELEMENTALAXE.1"),
                ResearchPage.infusion("ElementalAxe"),
                ResearchPage.text("tc.research_page.ELEMENTALAXE.2")
            )
            .setParents("THAUMIUM", "INFUSION")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("ELEMENTALPICK", "ARTIFICE", new AspectList().add(Aspect.TOOL, 3).add(Aspect.FIRE, 3).add(Aspect.SENSES, 3), -7, 3, 2, TcStack.of("thaumcraft:elemental_pickaxe"))
            .setPages(
                ResearchPage.text("tc.research_page.ELEMENTALPICK.1"),
                ResearchPage.infusion("ElementalPick"),
                ResearchPage.text("tc.research_page.ELEMENTALPICK.2")
            )
            .setParents("THAUMIUM", "INFUSION")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("ELEMENTALSWORD", "ARTIFICE", new AspectList().add(Aspect.WEAPON, 3).add(Aspect.AIR, 3).add(Aspect.ENERGY, 3), -7, 5, 2, TcStack.of("thaumcraft:elemental_sword"))
            .setPages(
                ResearchPage.text("tc.research_page.ELEMENTALSWORD.1"),
                ResearchPage.infusion("ElementalSword")
            )
            .setParents("THAUMIUM", "INFUSION")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("ELEMENTALSHOVEL", "ARTIFICE", new AspectList().add(Aspect.TOOL, 3).add(Aspect.EARTH, 3).add(Aspect.CRAFT, 3), -7, 6, 2, TcStack.of("thaumcraft:elemental_shovel"))
            .setPages(
                ResearchPage.text("tc.research_page.ELEMENTALSHOVEL.1"),
                ResearchPage.infusion("ElementalShovel"),
                ResearchPage.text("tc.research_page.ELEMENTALSHOVEL.2")
            )
            .setParents("THAUMIUM", "INFUSION")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("ELEMENTALHOE", "ARTIFICE", new AspectList().add(Aspect.TOOL, 3).add(Aspect.LIFE, 3).add(Aspect.CROP, 3), -7, 7, 2, TcStack.of("thaumcraft:elemental_hoe"))
            .setPages(
                ResearchPage.text("tc.research_page.ELEMENTALHOE.1"),
                ResearchPage.infusion("ElementalHoe")
            )
            .setParents("THAUMIUM", "INFUSION")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("WARDEDARCANA", "ARTIFICE", new AspectList().add(Aspect.TOOL, 6).add(Aspect.MIND, 3).add(Aspect.MECHANISM, 3).add(Aspect.ARMOR, 3), -5, -4, 2, TcStack.of("thaumcraft:arcane_door"))
            .setPages(
                ResearchPage.text("tc.research_page.WARDEDARCANA.1"),
                ResearchPage.arcane("ArcaneDoor"),
                ResearchPage.text("tc.research_page.WARDEDARCANA.2"),
                ResearchPage.arcane("IronKey"),
                ResearchPage.arcane("GoldKey"),
                ResearchPage.text("tc.research_page.WARDEDARCANA.3"),
                ResearchPage.arcane("ArcanePressurePlate"),
                ResearchPage.text("tc.research_page.WARDEDARCANA.4"),
                ResearchPage.arcane("WardedGlass")
            )
            .setParents("THAUMIUM")
            .registerResearchItem();
        new ResearchItem("JARBRAIN", "ARTIFICE", new AspectList().add(Aspect.HUNGER, 3).add(Aspect.MIND, 3).add(Aspect.UNDEAD, 3).add(Aspect.GREED, 3), -5, 9, 2, TcStack.of("thaumcraft:brain_jar"))
            .setPages(
                ResearchPage.text("tc.research_page.JARBRAIN.1"),
                ResearchPage.infusion("JarBrain")
            )
            .setParents("INFUSION")
            .setHidden()
            .setItemTriggers("thaumcraft:quicksilver")
            .setEntityTriggers("thaumcraft:brainy_zombie", "thaumcraft:giant_brainy_zombie")
            .setWarp(3)
            .registerResearchItem();
        new ResearchItem("INFUSIONENCHANTMENT", "ARTIFICE", new AspectList().add(Aspect.MAGIC, 6).add(Aspect.MIND, 3).add(Aspect.WEAPON, 3).add(Aspect.ARMOR, 3).add(Aspect.TOOL, 3), -6, 11, 3, ResourceLocation.parse("thaumcraft:textures/misc/r_enchant.png"))
            .setPages(
                ResearchPage.text("tc.research_page.INFUSIONENCHANTMENT.1"),
                ResearchPage.text("tc.research_page.INFUSIONENCHANTMENT.2"),
                ResearchPage.text("tc.research_page.INFUSIONENCHANTMENT.3"),
                ResearchPage.infusionEnchantment("InfEnchRepair"),
                ResearchPage.infusionEnchantment("InfEnchHaste"),
                ResearchPage.infusionEnchantment("InfEnch0"),
                ResearchPage.infusionEnchantment("InfEnch1"),
                ResearchPage.infusionEnchantment("InfEnch2"),
                ResearchPage.infusionEnchantment("InfEnch3"),
                ResearchPage.infusionEnchantment("InfEnch4"),
                ResearchPage.infusionEnchantment("InfEnch5"),
                ResearchPage.infusionEnchantment("InfEnch6"),
                ResearchPage.infusionEnchantment("InfEnch7"),
                ResearchPage.infusionEnchantment("InfEnch8"),
                ResearchPage.infusionEnchantment("InfEnch9"),
                ResearchPage.infusionEnchantment("InfEnch10"),
                ResearchPage.infusionEnchantment("InfEnch11"),
                ResearchPage.infusionEnchantment("InfEnch12"),
                ResearchPage.infusionEnchantment("InfEnch13"),
                ResearchPage.infusionEnchantment("InfEnch14"),
                ResearchPage.infusionEnchantment("InfEnch15"),
                ResearchPage.infusionEnchantment("InfEnch16"),
                ResearchPage.infusionEnchantment("InfEnch17"),
                ResearchPage.infusionEnchantment("InfEnch18"),
                ResearchPage.infusionEnchantment("InfEnch19"),
                ResearchPage.infusionEnchantment("InfEnch20"),
                ResearchPage.infusionEnchantment("InfEnch21")
            )
            .setConcealed()
            .setParents("JARBRAIN")
            .registerResearchItem();
        new ResearchItem("ARMORFORTRESS", "ARTIFICE", new AspectList().add(Aspect.METAL, 3).add(Aspect.ARMOR, 5).add(Aspect.CRAFT, 5), -8, 9, 2, TcStack.of("thaumcraft:fortress_helmet"))
            .setPages(
                ResearchPage.text("tc.research_page.ARMORFORTRESS.1"),
                ResearchPage.text("tc.research_page.ARMORFORTRESS.2"),
                ResearchPage.infusion("ThaumiumFortressHelm"),
                ResearchPage.infusion("ThaumiumFortressChest"),
                ResearchPage.infusion("ThaumiumFortressLegs")
            )
            .setParents("THAUMIUM", "INFUSIONENCHANTMENT")
            .setHidden()
            .setAspectTriggers(Aspect.ARMOR)
            .registerResearchItem();
        new ResearchItem("HELMGOGGLES", "ARTIFICE", new AspectList().add(Aspect.SENSES, 5).add(Aspect.AURA, 3).add(Aspect.ARMOR, 3), -9, 7, 2, TcStack.of("thaumcraft:goggles_of_revealing"))
            .setPages(
                ResearchPage.text("tc.research_page.HELMGOGGLES.1"),
                ResearchPage.infusion("HelmGoggles")
            )
            .setParentsHidden("GOGGLES")
            .setParents("ARMORFORTRESS")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("MASKGRINNINGDEVIL", "ARTIFICE", new AspectList().add(Aspect.HEAL, 5).add(Aspect.MIND, 5).add(Aspect.ARMOR, 3), -10, 8, 2, ResourceLocation.parse("thaumcraft:textures/misc/r_mask0.png"))
            .setPages(
                ResearchPage.text("tc.research_page.MASKGRINNINGDEVIL.1"),
                ResearchPage.infusion("MaskGrinningDevil")
            )
            .setParents("ARMORFORTRESS")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
    }

    private static void initArtifice5() {
        new ResearchItem("MASKANGRYGHOST", "ARTIFICE", new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.DEATH, 5).add(Aspect.ARMOR, 3), -10, 9, 2, ResourceLocation.parse("thaumcraft:textures/misc/r_mask1.png"))
            .setPages(
                ResearchPage.text("tc.research_page.MASKANGRYGHOST.1"),
                ResearchPage.infusion("MaskAngryGhost")
            )
            .setParents("ARMORFORTRESS")
            .setConcealed()
            .setSecondary()
            .setWarp(1)
            .registerResearchItem();
        new ResearchItem("MASKSIPPINGFIEND", "ARTIFICE", new AspectList().add(Aspect.UNDEAD, 5).add(Aspect.LIFE, 5).add(Aspect.ARMOR, 3), -10, 10, 2, ResourceLocation.parse("thaumcraft:textures/misc/r_mask2.png"))
            .setPages(
                ResearchPage.text("tc.research_page.MASKSIPPINGFIEND.1"),
                ResearchPage.infusion("MaskSippingFiend")
            )
            .setParents("ARMORFORTRESS")
            .setConcealed()
            .setSecondary()
            .setWarp(1)
            .registerResearchItem();
    }

    private static void initGolemancy() {
        initGolemancy1();
        initGolemancy2();
        initGolemancy3();
        initGolemancy4();
    }

    private static void initGolemancy1() {
        new ResearchItem("HUNGRYCHEST", "GOLEMANCY", new AspectList().add(Aspect.HUNGER, 3).add(Aspect.VOID, 3), -1, 0, 1, TcStack.of("thaumcraft:hungry_chest"))
            .setPages(
                ResearchPage.text("tc.research_page.HUNGRYCHEST.1"),
                ResearchPage.arcane("HungryChest")
            )
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("GOLEMFETTER", "GOLEMANCY", new AspectList().add(Aspect.TRAP, 3).add(Aspect.MECHANISM, 3), 4, 8, 1, TcStack.of("thaumcraft:golem_fetter"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMFETTER.1"),
                ResearchPage.arcane("GolemFetter")
            )
            .setParents("GOLEMSTONE")
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("TRAVELTRUNK", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 3).add(Aspect.TRAVEL, 3).add(Aspect.TREE, 3).add(Aspect.VOID, 3), 0, 4, 2, TcStack.of("thaumcraft:traveling_trunk"))
            .setPages(
                ResearchPage.text("tc.research_page.TRAVELTRUNK.1"),
                ResearchPage.infusion("TravelTrunk"),
                ResearchPage.text("tc.research_page.TRAVELTRUNK.2"),
                ResearchPage.concealed("UPGRADEAIR", "tc.research_page.TRAVELTRUNK.UAI"),
                ResearchPage.concealed("UPGRADEEARTH", "tc.research_page.TRAVELTRUNK.UEA"),
                ResearchPage.concealed("UPGRADEFIRE", "tc.research_page.TRAVELTRUNK.UFI"),
                ResearchPage.concealed("UPGRADEWATER", "tc.research_page.TRAVELTRUNK.UWA"),
                ResearchPage.concealed("UPGRADEORDER", "tc.research_page.TRAVELTRUNK.UOR"),
                ResearchPage.concealed("UPGRADEENTROPY", "tc.research_page.TRAVELTRUNK.UEN")
            )
            .setConcealed()
            .setParents("INFUSION", "GOLEMWOOD")
            .registerResearchItem();
        new ResearchItem("GOLEMSTRAW", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 3).add(Aspect.MOTION, 3).add(Aspect.CROP, 3).add(Aspect.EXCHANGE, 3), 0, 2, 2, TcStack.of("thaumcraft:straw_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMSTRAW.1"),
                ResearchPage.text("tc.research_page.GOLEMSTRAW.2"),
                ResearchPage.crucible("GolemStraw"),
                ResearchPage.text("tc.research_page.GOLEMSTRAW.3")
            )
            .setSiblings("COREGATHER", "GOLEMBELL")
            .setParents("HUNGRYCHEST")
            .registerResearchItem();
        new ResearchItem("GOLEMWOOD", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 4).add(Aspect.MOTION, 4).add(Aspect.TREE, 3).add(Aspect.EXCHANGE, 3), 2, 4, 2, TcStack.of("thaumcraft:wood_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMWOOD.1"),
                ResearchPage.crucible("GolemWood")
            )
            .setSecondary()
            .setParents("GOLEMSTRAW")
            .registerResearchItem();
        new ResearchItem("GOLEMTALLOW", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 3).add(Aspect.MOTION, 3).add(Aspect.FLESH, 3).add(Aspect.MAGIC, 3).add(Aspect.EXCHANGE, 3), 4, 6, 2, TcStack.of("thaumcraft:tallow_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMTALLOW.1"),
                ResearchPage.crafting("BlockTallow"),
                ResearchPage.crucible("GolemTallow")
            )
            .setConcealed()
            .setParents("GOLEMCLAY", "TALLOW")
            .registerResearchItem();
        new ResearchItem("GOLEMCLAY", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 6).add(Aspect.MOTION, 6).add(Aspect.EARTH, 3).add(Aspect.EXCHANGE, 3), 2, 6, 2, TcStack.of("thaumcraft:clay_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMCLAY.1"),
                ResearchPage.crucible("GolemClay")
            )
            .setSecondary()
            .setConcealed()
            .setParents("GOLEMWOOD")
            .registerResearchItem();
        new ResearchItem("GOLEMFLESH", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 7).add(Aspect.MOTION, 7).add(Aspect.FLESH, 6).add(Aspect.EXCHANGE, 3), 4, 4, 2, TcStack.of("thaumcraft:flesh_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMFLESH.1"),
                ResearchPage.crafting("BlockFlesh"),
                ResearchPage.crucible("GolemFlesh")
            )
            .setConcealed()
            .setParents("GOLEMWOOD")
            .setWarp(3)
            .registerResearchItem();
        new ResearchItem("GOLEMSTONE", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 6).add(Aspect.MOTION, 6).add(Aspect.EARTH, 3).add(Aspect.EXCHANGE, 3), 2, 8, 2, TcStack.of("thaumcraft:stone_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMSTONE.1"),
                ResearchPage.crucible("GolemStone")
            )
            .setSecondary()
            .setConcealed()
            .setParents("GOLEMCLAY")
            .registerResearchItem();
        new ResearchItem("GOLEMIRON", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 9).add(Aspect.MOTION, 9).add(Aspect.METAL, 3).add(Aspect.EXCHANGE, 3), 0, 10, 2, TcStack.of("thaumcraft:iron_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMIRON.1"),
                ResearchPage.crucible("GolemIron")
            )
            .setSecondary()
            .setConcealed()
            .setParents("GOLEMSTONE")
            .registerResearchItem();
        new ResearchItem("GOLEMTHAUMIUM", "GOLEMANCY", new AspectList().add(Aspect.SOUL, 10).add(Aspect.MOTION, 10).add(Aspect.METAL, 3).add(Aspect.MAGIC, 3).add(Aspect.EXCHANGE, 3), 2, 10, 2, TcStack.of("thaumcraft:thaumium_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMTHAUMIUM.1"),
                ResearchPage.crafting("BlockThaumium"),
                ResearchPage.crucible("GolemThaumium")
            )
            .setConcealed()
            .setParents("GOLEMIRON", "THAUMIUM")
            .registerResearchItem();
        new ResearchItem("GOLEMBELL", "GOLEMANCY", new AspectList(), 3, 0, 0, TcStack.of("thaumcraft:golem_bell"))
            .setPages(
                ResearchPage.text("tc.research_page.GOLEMBELL.1"),
                ResearchPage.text("tc.research_page.GOLEMBELL.2"),
                ResearchPage.arcane("GolemBell")
            )
            .setParents("GOLEMSTRAW")
            .setStub()
            .registerResearchItem();
    }

    private static void initGolemancy2() {
        new ResearchItem("COREGATHER", "GOLEMANCY", new AspectList(), -3, 3, 1, TcStack.of("thaumcraft:golem_core_gather"))
            .setPages(
                ResearchPage.text("tc.research_page.COREGATHER.1"),
                ResearchPage.arcane("CoreBlank"),
                ResearchPage.text("tc.research_page.COREGATHER.2"),
                ResearchPage.crucible("CoreGather")
            )
            .setConcealed()
            .setParents("GOLEMSTRAW")
            .setStub()
            .registerResearchItem();
        new ResearchItem("COREFILL", "GOLEMANCY", new AspectList().add(Aspect.HUNGER, 3).add(Aspect.EXCHANGE, 3).add(Aspect.VOID, 3), -5, 3, 2, TcStack.of("thaumcraft:golem_core_fill"))
            .setPages(
                ResearchPage.text("tc.research_page.COREFILL.1"),
                ResearchPage.crucible("CoreFill")
            )
            .setConcealed()
            .setSecondary()
            .setParents("COREGATHER")
            .registerResearchItem();
        new ResearchItem("COREEMPTY", "GOLEMANCY", new AspectList().add(Aspect.VOID, 3).add(Aspect.EXCHANGE, 3).add(Aspect.GREED, 3), -5, 1, 2, TcStack.of("thaumcraft:golem_core_empty"))
            .setPages(
                ResearchPage.text("tc.research_page.COREEMPTY.1"),
                ResearchPage.crucible("CoreEmpty")
            )
            .setConcealed()
            .setSecondary()
            .setParents("COREGATHER")
            .registerResearchItem();
        new ResearchItem("CORESORTING", "GOLEMANCY", new AspectList().add(Aspect.VOID, 3).add(Aspect.EXCHANGE, 3).add(Aspect.GREED, 3).add(Aspect.HUNGER, 3), -7, 2, 2, TcStack.of("thaumcraft:golem_core_sorting"))
            .setPages(
                ResearchPage.text("tc.research_page.CORESORTING.1"),
                ResearchPage.infusion("CoreSorting")
            )
            .setConcealed()
            .setSecondary()
            .setParents("COREEMPTY", "COREFILL", "INFUSION")
            .registerResearchItem();
        new ResearchItem("COREUSE", "GOLEMANCY", new AspectList().add(Aspect.TOOL, 3).add(Aspect.EXCHANGE, 3).add(Aspect.MECHANISM, 3).add(Aspect.MAN, 3), -7, 0, 3, TcStack.of("thaumcraft:golem_core_use"))
            .setPages(
                ResearchPage.text("tc.research_page.COREUSE.1"),
                ResearchPage.text("tc.research_page.COREUSE.2"),
                ResearchPage.infusion("CoreUse"),
                ResearchPage.concealed("UPGRADEAIR", "tc.research_page.COREUSE.3")
            )
            .setConcealed()
            .setParents("COREEMPTY", "INFUSION")
            .registerResearchItem();
        new ResearchItem("COREHARVEST", "GOLEMANCY", new AspectList().add(Aspect.HARVEST, 6).add(Aspect.CROP, 3).add(Aspect.TRAVEL, 3), -2, 5, 2, TcStack.of("thaumcraft:golem_core_harvest"))
            .setPages(
                ResearchPage.text("tc.research_page.COREHARVEST.1"),
                ResearchPage.crucible("CoreHarvest"),
                ResearchPage.concealed("UPGRADEORDER", "tc.research_page.COREHARVEST.2")
            )
            .setConcealed()
            .setParents("COREGATHER")
            .registerResearchItem();
        new ResearchItem("COREFISHING", "GOLEMANCY", new AspectList().add(Aspect.WATER, 3).add(Aspect.HARVEST, 3).add(Aspect.BEAST, 3).add(Aspect.HUNGER, 3), -2, 7, 2, TcStack.of("thaumcraft:golem_core_fishing"))
            .setPages(
                ResearchPage.text("tc.research_page.COREFISHING.1"),
                ResearchPage.infusion("CoreFishing"),
                ResearchPage.concealed("UPGRADEAIR", "tc.research_page.COREFISHING.2"),
                ResearchPage.concealed("UPGRADEFIRE", "tc.research_page.COREFISHING.3"),
                ResearchPage.concealed("UPGRADEORDER", "tc.research_page.COREFISHING.4"),
                ResearchPage.concealed("UPGRADEENTROPY", "tc.research_page.COREFISHING.5")
            )
            .setConcealed()
            .setSecondary()
            .setParents("COREHARVEST", "INFUSION")
            .registerResearchItem();
        new ResearchItem("CORELUMBER", "GOLEMANCY", new AspectList().add(Aspect.TREE, 6).add(Aspect.HARVEST, 3).add(Aspect.TOOL, 3).add(Aspect.ENERGY, 3), -1, 7, 2, TcStack.of("thaumcraft:golem_core_chop"))
            .setPages(
                ResearchPage.text("tc.research_page.CORELUMBER.1"),
                ResearchPage.infusion("CoreLumber")
            )
            .setConcealed()
            .setSecondary()
            .setParents("COREHARVEST", "ELEMENTALAXE")
            .registerResearchItem();
        new ResearchItem("COREGUARD", "GOLEMANCY", new AspectList().add(Aspect.WEAPON, 3).add(Aspect.TRAP, 3).add(Aspect.SENSES, 3), -4, 5, 2, TcStack.of("thaumcraft:golem_core_guard"))
            .setPages(
                ResearchPage.text("tc.research_page.COREGUARD.1"),
                ResearchPage.crucible("CoreGuard"),
                ResearchPage.concealed("UPGRADEORDER", "tc.research_page.COREGUARD.2")
            )
            .setConcealed()
            .setParents("COREGATHER")
            .registerResearchItem();
        new ResearchItem("COREBUTCHER", "GOLEMANCY", new AspectList().add(Aspect.WEAPON, 3).add(Aspect.BEAST, 3).add(Aspect.SENSES, 3).add(Aspect.HARVEST, 3), -3, 7, 2, TcStack.of("thaumcraft:golem_core_butcher"))
            .setPages(
                ResearchPage.text("tc.research_page.COREBUTCHER.1"),
                ResearchPage.crucible("CoreButcher")
            )
            .setConcealed()
            .setSecondary()
            .setParents("COREGUARD", "COREHARVEST")
            .setWarp(1)
            .registerResearchItem();
        new ResearchItem("CORELIQUID", "GOLEMANCY", new AspectList().add(Aspect.WATER, 3).add(Aspect.EXCHANGE, 3).add(Aspect.TRAVEL, 3), -7, 4, 2, TcStack.of("thaumcraft:golem_core_decanting"))
            .setPages(
                ResearchPage.text("tc.research_page.CORELIQUID.1"),
                ResearchPage.crucible("CoreLiquid"),
                ResearchPage.concealed("UPGRADEENTROPY", "tc.research_page.CORELIQUID.2")
            )
            .setConcealed()
            .setParents("COREFILL")
            .registerResearchItem();
        new ResearchItem("COREALCHEMY", "GOLEMANCY", new AspectList().add(Aspect.WATER, 3).add(Aspect.TRAVEL, 3).add(Aspect.MAGIC, 3).add(Aspect.ENERGY, 3), -9, 3, 2, TcStack.of("thaumcraft:golem_core_alchemy"))
            .setPages(
                ResearchPage.text("tc.research_page.COREALCHEMY.1"),
                ResearchPage.infusion("CoreAlchemy"),
                ResearchPage.text("tc.research_page.COREALCHEMY.2")
            )
            .setConcealed()
            .setSecondary()
            .setParents("CORELIQUID", "INFUSION")
            .registerResearchItem();
    }

    private static void initGolemancy3() {
        new ResearchItem("UPGRADEAIR", "GOLEMANCY", new AspectList().add(Aspect.AIR, 6).add(Aspect.MOTION, 3), 7, -3, 1, TcStack.of("thaumcraft:golem_upgrade_air"))
            .setPages(
                ResearchPage.text("tc.research_page.UPGRADEAIR.1"),
                ResearchPage.arcane("UpgradeAir")
            )
            .setConcealed()
            .setSecondary()
            .setParents("GOLEMBELL")
            .registerResearchItem();
        new ResearchItem("UPGRADEEARTH", "GOLEMANCY", new AspectList().add(Aspect.EARTH, 6).add(Aspect.LIFE, 3), 6, -2, 1, TcStack.of("thaumcraft:golem_upgrade_earth"))
            .setPages(
                ResearchPage.text("tc.research_page.UPGRADEEARTH.1"),
                ResearchPage.arcane("UpgradeEarth")
            )
            .setConcealed()
            .setSecondary()
            .setParents("GOLEMBELL")
            .registerResearchItem();
        new ResearchItem("UPGRADEFIRE", "GOLEMANCY", new AspectList().add(Aspect.FIRE, 6).add(Aspect.ENERGY, 3), 5, -1, 1, TcStack.of("thaumcraft:golem_upgrade_fire"))
            .setPages(
                ResearchPage.text("tc.research_page.UPGRADEFIRE.1"),
                ResearchPage.arcane("UpgradeFire")
            )
            .setConcealed()
            .setSecondary()
            .setParents("GOLEMBELL")
            .registerResearchItem();
        new ResearchItem("UPGRADEWATER", "GOLEMANCY", new AspectList().add(Aspect.WATER, 6).add(Aspect.SENSES, 3), 5, 1, 1, TcStack.of("thaumcraft:golem_upgrade_water"))
            .setPages(
                ResearchPage.text("tc.research_page.UPGRADEWATER.1"),
                ResearchPage.arcane("UpgradeWater")
            )
            .setConcealed()
            .setSecondary()
            .setParents("GOLEMBELL")
            .registerResearchItem();
        new ResearchItem("UPGRADEORDER", "GOLEMANCY", new AspectList().add(Aspect.ORDER, 6).add(Aspect.MIND, 3), 6, 2, 1, TcStack.of("thaumcraft:golem_upgrade_order"))
            .setPages(
                ResearchPage.text("tc.research_page.UPGRADEORDER.1"),
                ResearchPage.arcane("UpgradeOrder")
            )
            .setConcealed()
            .setSecondary()
            .setParents("GOLEMBELL")
            .registerResearchItem();
        new ResearchItem("UPGRADEENTROPY", "GOLEMANCY", new AspectList().add(Aspect.ENTROPY, 6).add(Aspect.MIND, 3), 7, 3, 1, TcStack.of("thaumcraft:golem_upgrade_entropy"))
            .setPages(
                ResearchPage.text("tc.research_page.UPGRADEENTROPY.1"),
                ResearchPage.arcane("UpgradeEntropy")
            )
            .setConcealed()
            .setSecondary()
            .setParents("GOLEMBELL")
            .registerResearchItem();
        new ResearchItem("ADVANCEDGOLEM", "GOLEMANCY", new AspectList().add(Aspect.LIFE, 3).add(Aspect.ENERGY, 3).add(Aspect.MIND, 6).add(Aspect.SENSES, 3), 8, 0, 2, TcStack.of("thaumcraft:straw_golem"))
            .setPages(
                ResearchPage.text("tc.research_page.ADVANCEDGOLEM.1"),
                ResearchPage.infusion("AdvancedGolem")
            )
            .setConcealed()
            .setParents("INFUSION", "UPGRADEAIR", "UPGRADEEARTH", "UPGRADEFIRE", "UPGRADEWATER", "UPGRADEORDER", "UPGRADEENTROPY")
            .setWarp(5)
            .registerResearchItem();
        new ResearchItem("TINYHAT", "GOLEMANCY", new AspectList().add(Aspect.CLOTH, 2).add(Aspect.LIFE, 1).add(Aspect.GREED, 1), 5, 10, 1, TcStack.of("thaumcraft:top_hat"))
            .setPages(
                ResearchPage.text("tc.research_page.TINYHAT.1"),
                ResearchPage.arcane("TinyHat")
            )
            .setHidden()
            .setSecondary()
            .setItemTriggers("minecraft:white_wool", "minecraft:orange_wool", "minecraft:purple_wool", "minecraft:blue_wool", "minecraft:brown_wool", "minecraft:green_wool", "minecraft:red_wool", "minecraft:black_wool", "minecraft:magenta_wool", "minecraft:light_blue_wool", "minecraft:yellow_wool", "minecraft:lime_wool", "minecraft:pink_wool", "minecraft:gray_wool", "minecraft:light_gray_wool", "minecraft:cyan_wool")
            .setAspectTriggers(Aspect.CLOTH)
            .registerResearchItem();
        new ResearchItem("TINYGLASSES", "GOLEMANCY", new AspectList().add(Aspect.CLOTH, 2).add(Aspect.SENSES, 1).add(Aspect.GREED, 1), 6, 10, 1, TcStack.of("thaumcraft:spectacles"))
            .setPages(
                ResearchPage.text("tc.research_page.TINYGLASSES.1"),
                ResearchPage.arcane("TinyGlasses")
            )
            .setHidden()
            .setSecondary()
            .setItemTriggers("minecraft:white_wool", "minecraft:orange_wool", "minecraft:purple_wool", "minecraft:blue_wool", "minecraft:brown_wool", "minecraft:green_wool", "minecraft:red_wool", "minecraft:black_wool", "minecraft:magenta_wool", "minecraft:light_blue_wool", "minecraft:yellow_wool", "minecraft:lime_wool", "minecraft:pink_wool", "minecraft:gray_wool", "minecraft:light_gray_wool", "minecraft:cyan_wool")
            .setAspectTriggers(Aspect.CLOTH)
            .registerResearchItem();
        new ResearchItem("TINYBOWTIE", "GOLEMANCY", new AspectList().add(Aspect.CLOTH, 2).add(Aspect.TRAVEL, 1).add(Aspect.GREED, 1), 7, 10, 1, TcStack.of("thaumcraft:bowtie"))
            .setPages(
                ResearchPage.text("tc.research_page.TINYBOWTIE.1"),
                ResearchPage.arcane("TinyBowtie")
            )
            .setHidden()
            .setSecondary()
            .setItemTriggers("minecraft:white_wool", "minecraft:orange_wool", "minecraft:purple_wool", "minecraft:blue_wool", "minecraft:brown_wool", "minecraft:green_wool", "minecraft:red_wool", "minecraft:black_wool", "minecraft:magenta_wool", "minecraft:light_blue_wool", "minecraft:yellow_wool", "minecraft:lime_wool", "minecraft:pink_wool", "minecraft:gray_wool", "minecraft:light_gray_wool", "minecraft:cyan_wool")
            .setAspectTriggers(Aspect.CLOTH)
            .registerResearchItem();
        new ResearchItem("TINYFEZ", "GOLEMANCY", new AspectList().add(Aspect.CLOTH, 2).add(Aspect.ENERGY, 1).add(Aspect.GREED, 1), 8, 10, 1, TcStack.of("thaumcraft:fez"))
            .setPages(
                ResearchPage.text("tc.research_page.TINYFEZ.1"),
                ResearchPage.arcane("TinyFez")
            )
            .setHidden()
            .setSecondary()
            .setItemTriggers("minecraft:white_wool", "minecraft:orange_wool", "minecraft:purple_wool", "minecraft:blue_wool", "minecraft:brown_wool", "minecraft:green_wool", "minecraft:red_wool", "minecraft:black_wool", "minecraft:magenta_wool", "minecraft:light_blue_wool", "minecraft:yellow_wool", "minecraft:lime_wool", "minecraft:pink_wool", "minecraft:gray_wool", "minecraft:light_gray_wool", "minecraft:cyan_wool")
            .setAspectTriggers(Aspect.CLOTH)
            .registerResearchItem();
        new ResearchItem("TINYDART", "GOLEMANCY", new AspectList().add(Aspect.FLIGHT, 1).add(Aspect.WEAPON, 2).add(Aspect.GREED, 1), 5, 11, 1, TcStack.of("thaumcraft:dart_launcher"))
            .setPages(
                ResearchPage.text("tc.research_page.TINYDART.1"),
                ResearchPage.arcane("TinyDart")
            )
            .setHidden()
            .setSecondary()
            .setAspectTriggers(Aspect.WEAPON)
            .registerResearchItem();
    }

    private static void initGolemancy4() {
        new ResearchItem("TINYVISOR", "GOLEMANCY", new AspectList().add(Aspect.SENSES, 1).add(Aspect.ARMOR, 2).add(Aspect.GREED, 1), 6, 11, 1, TcStack.of("thaumcraft:visor"))
            .setPages(
                ResearchPage.text("tc.research_page.TINYVISOR.1"),
                ResearchPage.arcane("TinyVisor")
            )
            .setHidden()
            .setSecondary()
            .setAspectTriggers(Aspect.ARMOR)
            .registerResearchItem();
        new ResearchItem("TINYARMOR", "GOLEMANCY", new AspectList().add(Aspect.METAL, 1).add(Aspect.ARMOR, 2).add(Aspect.GREED, 1), 7, 11, 1, TcStack.of("thaumcraft:iron_plating"))
            .setPages(
                ResearchPage.text("tc.research_page.TINYARMOR.1"),
                ResearchPage.arcane("TinyArmor")
            )
            .setHidden()
            .setSecondary()
            .setAspectTriggers(Aspect.ARMOR)
            .registerResearchItem();
        new ResearchItem("TINYHAMMER", "GOLEMANCY", new AspectList().add(Aspect.METAL, 1).add(Aspect.WEAPON, 2).add(Aspect.GREED, 1), 8, 11, 1, TcStack.of("thaumcraft:mace_arm"))
            .setPages(
                ResearchPage.text("tc.research_page.TINYHAMMER.1"),
                ResearchPage.arcane("TinyHammer")
            )
            .setHidden()
            .setSecondary()
            .setAspectTriggers(Aspect.WEAPON)
            .registerResearchItem();
    }

    private static void initEldritch() {
        initEldritch1();
        initEldritch2();
    }

    private static void initEldritch1() {
        new ResearchItem("ELDRITCHMINOR", "ELDRITCH", new AspectList(), 1, 0, 0, ResourceLocation.parse("thaumcraft:textures/misc/r_eldritchminor.png"))
            .setPages(
                ResearchPage.text("tc.research_page.ELDRITCHMINOR.1"),
                ResearchPage.crucible("VoidSeed")
            )
            .setHidden()
            .setRound()
            .setSpecial()
            .registerResearchItem();
        new ResearchItem("ELDRITCHMAJOR", "ELDRITCH", new AspectList(), -1, 0, 0, ResourceLocation.parse("thaumcraft:textures/misc/r_eldritchmajor.png"))
            .setPages(
                ResearchPage.text("tc.research_page.ELDRITCHMAJOR.1"),
                ResearchPage.text("tc.research_page.ELDRITCHMAJOR.2")
            )
            .setStub()
            .setHidden()
            .setRound()
            .setSpecial()
            .registerResearchItem();
        new ResearchItem("OCULUS", "ELDRITCH", new AspectList().add(Aspect.MIND, 3).add(Aspect.DARKNESS, 3).add(Aspect.EXCHANGE, 3).add(Aspect.TRAVEL, 6).add(Aspect.ELDRITCH, 6), -2, 2, 1, TcStack.of("thaumcraft:eldritch_eye"))
            .setPages(
                ResearchPage.text("tc.research_page.OCULUS.1"),
                ResearchPage.infusion("EldritchEye"),
                ResearchPage.text("tc.research_page.OCULUS.2")
            )
            .setRound()
            .setConcealed()
            .setParents("CRIMSON", "ELDRITCHMAJOR")
            .setSpecial()
            .setWarp(6)
            .registerResearchItem();
        new ResearchItem("ENTEROUTER", "ELDRITCH", new AspectList(), -3, 4, 1, ResourceLocation.parse("thaumcraft:textures/misc/r_outer.png"))
            .setPages(
                ResearchPage.text("tc.research_page.ENTEROUTER.1")
            )
            .setStub()
            .setHidden()
            .setRound()
            .setParents("OCULUS")
            .registerResearchItem();
        new ResearchItem("OUTERREV", "ELDRITCH", new AspectList().add(Aspect.ELDRITCH, 4).add(Aspect.MIND, 4), -5, 3, 1, ResourceLocation.parse("thaumcraft:textures/misc/r_outerrev.png"))
            .setPages(
                ResearchPage.text("tc.research_page.OUTERREV.1")
            )
            .setItemTriggers("thaumcraft:glyphed_stone", "thaumcraft:runed_stone")
            .setLost()
            .setSecondary()
            .setSpecial()
            .setParents("ENTEROUTER")
            .registerResearchItem();
        new ResearchItem("PRIMPEARL", "ELDRITCH", new AspectList().add(Aspect.AIR, 8).add(Aspect.EARTH, 8).add(Aspect.FIRE, 8).add(Aspect.WATER, 8).add(Aspect.ORDER, 8).add(Aspect.ENTROPY, 8), 0, 4, 1, TcStack.of("thaumcraft:primordial_pearl"))
            .setPages(
                ResearchPage.text("tc.research_page.PRIMPEARL.1"),
                ResearchPage.text("tc.research_page.PRIMPEARL.2")
            )
            .setItemTriggers("thaumcraft:primordial_pearl")
            .setLost()
            .setSecondary()
            .setSpecial()
            .setParents("ELDRITCHMINOR")
            .registerResearchItem();
        new ResearchItem("PRIMNODE", "ELDRITCH", new AspectList().add(Aspect.AURA, 1).add(Aspect.MAGIC, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1), 0, 6, 1, ResourceLocation.parse("thaumcraft:textures/misc/r_nodes_2.png"))
            .setPages(
                ResearchPage.text("tc.research_page.PRIMNODE.1")
            )
            .setSecondary()
            .setParents("PRIMPEARL")
            .setWarp(1)
            .registerResearchItem();
        new ResearchItem("ADVALCHEMYFURNACE", "ELDRITCH", new AspectList().add(Aspect.AURA, 1).add(Aspect.MAGIC, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1), -2, 6, 1, TcStack.of("thaumcraft:advanced_alchemical_construct"))
            .setPages(
                ResearchPage.text("tc.research_page.ADVALCHEMYFURNACE.1"),
                ResearchPage.arcane("AdvAlchemyConstruct"),
                ResearchPage.text("tc.research_page.ADVALCHEMYFURNACE.2"),
                ResearchPage.compound("AdvAlchemyFurnace")
            )
            .setSecondary()
            .setParents("PRIMPEARL", "DISTILESSENTIA", "VISPOWER")
            .registerResearchItem();
        new ResearchItem("PRIMALCRUSHER", "ELDRITCH", new AspectList().add(Aspect.MINE, 6).add(Aspect.TOOL, 6).add(Aspect.ENTROPY, 6).add(Aspect.VOID, 6).add(Aspect.WEAPON, 6).add(Aspect.ELDRITCH, 6).add(Aspect.GREED, 6), 2, 5, 2, TcStack.of("thaumcraft:primal_crusher"))
            .setPages(
                ResearchPage.text("tc.research_page.PRIMALCRUSHER.1"),
                ResearchPage.infusion("PrimalCrusher"),
                ResearchPage.text("tc.research_page.PRIMALCRUSHER.2")
            )
            .setConcealed()
            .setParents("PRIMPEARL")
            .setParentsHidden("VOIDMETAL", "ELEMENTALPICK", "ELEMENTALSHOVEL")
            .registerResearchItem();
        new ResearchItem("SANITYCHECK", "ELDRITCH", new AspectList().add(Aspect.MIND, 5).add(Aspect.ELDRITCH, 3).add(Aspect.SENSES, 5), 2, 2, 1, TcStack.of("thaumcraft:sanity_checker"))
            .setPages(
                ResearchPage.text("tc.research_page.SANITYCHECK.1"),
                ResearchPage.infusion("SanityCheck")
            )
            .setParents("ELDRITCHMINOR")
            .registerResearchItem();
        new ResearchItem("VOIDMETAL", "ELDRITCH", new AspectList().add(Aspect.METAL, 3).add(Aspect.ELDRITCH, 3).add(Aspect.DARKNESS, 3).add(Aspect.VOID, 5), 2, -2, 2, TcStack.of("thaumcraft:void_metal_ingot"))
            .setPages(
                ResearchPage.text("tc.research_page.VOIDMETAL.1"),
                ResearchPage.crucible("VoidMetal"),
                ResearchPage.text("tc.research_page.VOIDMETAL.2"),
                ResearchPage.crafting("VoidAxe"),
                ResearchPage.crafting("VoidSword"),
                ResearchPage.crafting("VoidPick"),
                ResearchPage.crafting("VoidShovel"),
                ResearchPage.crafting("VoidHoe"),
                ResearchPage.crafting("VoidHelm"),
                ResearchPage.crafting("VoidChest"),
                ResearchPage.crafting("VoidLegs"),
                ResearchPage.crafting("VoidBoots")
            )
            .setParents("THAUMIUM", "ELDRITCHMINOR")
            .registerResearchItem();
        new ResearchItem("ESSENTIARESERVOIR", "ELDRITCH", new AspectList().add(Aspect.WATER, 5).add(Aspect.VOID, 3).add(Aspect.EXCHANGE, 3).add(Aspect.MAGIC, 5).add(Aspect.VOID, 5), 4, -3, 2, TcStack.of("thaumcraft:essentia_reservoir"))
            .setPages(
                ResearchPage.text("tc.research_page.ESSENTIARESERVOIR.1"),
                ResearchPage.infusion("EssentiaReservoir"),
                ResearchPage.text("tc.research_page.ESSENTIARESERVOIR.2")
            )
            .setParents("VOIDMETAL", "CENTRIFUGE", "INFUSION")
            .registerResearchItem();
    }

    private static void initEldritch2() {
        new ResearchItem("CAP_void", "ELDRITCH", new AspectList().add(Aspect.VOID, 5).add(Aspect.ELDRITCH, 5).add(Aspect.TOOL, 3).add(Aspect.MAGIC, 3).add(Aspect.AURA, 3), 5, -1, 3, TcStack.of("thaumcraft:void_wand_cap"))
            .setPages(
                ResearchPage.text("tc.research_page.CAP_void.1"),
                ResearchPage.arcane("WandCapVoidInert"),
                ResearchPage.infusion("WandCapVoid")
            )
            .setConcealed()
            .setParents("CAP_thaumium", "VOIDMETAL")
            .setWarp(1)
            .registerResearchItem();
        new ResearchItem("ARMORVOIDFORTRESS", "ELDRITCH", new AspectList().add(Aspect.ARMOR, 5).add(Aspect.ELDRITCH, 3).add(Aspect.CLOTH, 3).add(Aspect.DARKNESS, 3).add(Aspect.VOID, 5), 0, -3, 3, TcStack.of("thaumcraft:void_robe_helmet"))
            .setPages(
                ResearchPage.text("tc.research_page.ARMORVOIDFORTRESS.1"),
                ResearchPage.infusion("VoidRobeHelm"),
                ResearchPage.infusion("VoidRobeChest"),
                ResearchPage.infusion("VoidRobeLegs")
            )
            .setParents("VOIDMETAL", "ENCHFABRIC", "ELDRITCHMAJOR")
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        new ResearchItem("FOCUSPRIMAL", "ELDRITCH", new AspectList().add(Aspect.AIR, 6).add(Aspect.WATER, 6).add(Aspect.FIRE, 6).add(Aspect.EARTH, 6).add(Aspect.ORDER, 6).add(Aspect.ENTROPY, 6).add(Aspect.MAGIC, 6), 4, 1, 2, TcStack.of("thaumcraft:focus_primal"))
            .setPages(
                ResearchPage.text("tc.research_page.FOCUSPRIMAL.1"),
                ResearchPage.arcane("FocusPrimal")
            )
            .setConcealed()
            .setParents("ELDRITCHMINOR")
            .setWarp(2)
            .registerResearchItem();
        new ResearchItem("ROD_primal_staff", "ELDRITCH", new AspectList().add(Aspect.AIR, 9).add(Aspect.EARTH, 9).add(Aspect.FIRE, 9).add(Aspect.WATER, 9).add(Aspect.ORDER, 9).add(Aspect.ENTROPY, 9).add(Aspect.TOOL, 9).add(Aspect.MAGIC, 12), 6, 2, 3, TcStack.of("thaumcraft:primal_staff_core"))
            .setPages(
                ResearchPage.text("tc.research_page.ROD_primal_staff.1"),
                ResearchPage.infusion("WandRodPrimalStaff")
            )
            .setHidden()
            .setEntityTriggers("thaumcraft:primal_orb")
            .setItemTriggers("thaumcraft:focus_primal")
            .setParents("FOCUSPRIMAL")
            .setParentsHidden("ROD_silverwood_staff", "ROD_bone_staff", "ROD_greatwood_staff", "ROD_blaze_staff", "ROD_reed_staff", "ROD_obsidian_staff", "ROD_quartz_staff", "ROD_ice_staff")
            .setWarp(3)
            .registerResearchItem();
    }
}
