package thaumcraft.test;

import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.TcStack;
import thaumcraft.crafting.InfusionCrafting;
import thaumcraft.wand.WandRegistry;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.Warp;
import thaumcraft.aspect.AspectList;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class AddonApiGameTests {
    @GameTest(template = "test/platform", batch = "addon_api")
    public static void customStackData(GameTestHelper helper) {
        String key = "test:addon_name";
        TcStack.registerDataHandler(key, (stack, value) -> stack.set(DataComponents.CUSTOM_NAME, Component.literal(value)));
        TcStack.registerDataMatcher(key, (stack, value) -> stack.getHoverName().getString().equals(value));
        TcStack result = TcStack.withData("minecraft:stick", 2, Map.of(key, "addon"));
        ItemStack stack = result.create();
        helper.assertTrue(stack.is(Items.STICK) && stack.getCount() == 2 && stack.getHoverName().getString().equals("addon"), "custom result data lost");
        helper.assertTrue(!InfusionCrafting.isTagResult(result), "custom result would copy the central item");
        helper.assertTrue(result.matches(stack) && !result.matches(new ItemStack(Items.STICK)), "custom matcher ignored");
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "addon_api")
    public static void wandComponents(GameTestHelper helper) {
        String tag = "test_addon";
        ResourceLocation texture = ResourceLocation.parse("test:textures/wand.png");
        if (WandRegistry.findCap(tag) == null) {
            WandRegistry.registerCap(tag, 0.8F, List.of(), 0, ResourceLocation.withDefaultNamespace("stick"), 1, null, texture);
            WandRegistry.registerRod(tag, 123, ResourceLocation.withDefaultNamespace("stick"), 1, null, false, false, false, texture);
        }
        helper.assertTrue(WandRegistry.findCap(tag).texture().equals(texture) && WandRegistry.findRod(tag).texture().equals(texture), "foreign texture namespace lost");
        boolean rejected = false;
        try {
            WandRegistry.registerCap("gold", 1, List.of(), 0, ResourceLocation.withDefaultNamespace("stick"), 1, null, texture);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        helper.assertTrue(rejected && WandRegistry.CAP_GOLD.texture().getNamespace().equals("thaumcraft"), "duplicate or existing component changed");
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "addon_api")
    public static void registeredWarp(GameTestHelper helper) {
        ResourceLocation item = ResourceLocation.withDefaultNamespace("stick");
        int original = Warp.getItemWarp(new ItemStack(Items.STICK));
        try {
            Warp.registerItem(item, 3);
            helper.assertTrue(Warp.getItemWarp(new ItemStack(Items.STICK)) == 3, "addon warp ignored");
        } finally {
            Warp.registerItem(item, original);
        }
        helper.succeed();
    }
    @GameTest(template = "test/platform", batch = "addon_api")
    public static void dynamicRecipe(GameTestHelper helper) {
        thaumcraft.crafting.ArcaneCrafting.registerDynamic(new thaumcraft.crafting.DynamicArcaneRecipe() {
            public boolean matches(List<ItemStack> grid, net.minecraft.world.entity.player.Player player) {
                return grid.size() == 9 && grid.get(4).is(Items.BARRIER);
            }
            public ItemStack result(List<ItemStack> grid) {
                return new ItemStack(Items.DIAMOND, 2);
            }
            public AspectList aspects(List<ItemStack> grid) {
                return new AspectList().add(thaumcraft.aspect.Aspect.AIR, 3);
            }
        });
        var grid = new java.util.ArrayList<ItemStack>(java.util.Collections.nCopies(9, ItemStack.EMPTY));
        grid.set(4, new ItemStack(Items.BARRIER));
        var match = thaumcraft.crafting.ArcaneCrafting.find(grid, TestSupport.mockServerPlayer(helper));
        helper.assertTrue(match != null && match.result().is(Items.DIAMOND) && match.result().getCount() == 2 && match.aspects().getAmount(thaumcraft.aspect.Aspect.AIR) == 3, "dynamic addon recipe not dispatched");
        helper.succeed();
    }

    @GameTest(template = "test/platform", batch = "addon_api")
    public static void aspectRegistrar(GameTestHelper helper) {
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        thaumcraft.aspect.AspectSync.addRegistrar(registry -> calls.incrementAndGet());
        thaumcraft.aspect.AspectSync.rebuild(helper.getLevel().getServer());
        thaumcraft.aspect.AspectSync.rebuild(helper.getLevel().getServer());
        helper.assertTrue(calls.get() == 2, "addon aspects not rebuilt on reload");
        helper.succeed();
    }
}
