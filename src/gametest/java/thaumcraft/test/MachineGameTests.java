package thaumcraft.test;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.ArcaneRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;
import thaumcraft.device.ArcaneBoreBlock;
import thaumcraft.device.ArcaneBoreBlockEntity;
import thaumcraft.device.ArcaneSpaBlockEntity;
import thaumcraft.device.InfernalFurnaceBlock;
import thaumcraft.device.InfernalFurnaceBlockEntity;
import thaumcraft.device.InfernalTiles;
import thaumcraft.device.SmeltingBonuses;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchCategories;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class MachineGameTests {
    private MachineGameTests() {
    }





    private static void buildFurnace(GameTestHelper helper, BlockPos origin) {
        for (int layer = 0; layer < 3; layer++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    boolean corner = x != 1 && z != 1;
                    Block block = corner ? Blocks.NETHER_BRICKS : Blocks.OBSIDIAN;
                    if (x == 1 && z == 1) {
                        block = layer == 2 ? Blocks.AIR : layer == 1 ? Blocks.LAVA : Blocks.OBSIDIAN;
                    }
                    if (layer == 1 && x == 1 && z == 0) {
                        block = Blocks.IRON_BARS;
                    }
                    helper.setBlock(origin.offset(x, layer, z), block);
                }
            }
        }
    }

    private static boolean form(GameTestHelper helper, BlockPos origin) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Knowledge.get(player).addResearch("INFERNALFURNACE");
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_GREATWOOD, false);
        Wands.fill(wand);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        BlockPos clicked = helper.absolutePos(origin);
        return wand.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false))).consumesAction();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void furnaceFormsAndSmelts(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 2, 1);
        buildFurnace(helper, origin);
        helper.setBlock(origin.offset(1, 1, 1), Blocks.AIR);
        helper.assertTrue(!form(helper, origin), "furnace formed without its lava source");
        helper.setBlock(origin.offset(1, 1, 1), Blocks.LAVA);
        helper.assertTrue(form(helper, origin), "wand trigger did not form the furnace");
        BlockPos core = origin.offset(1, 1, 1);
        helper.assertBlockPresent(ModBlocks.INFERNAL_FURNACE.get(), core);
        helper.assertTrue(java.util.Objects.equals(helper.getBlockState(core).getValue(InfernalFurnaceBlock.TILE), InfernalTiles.CORE), "core tile");
        helper.assertTrue(java.util.Objects.equals(helper.getBlockState(core).getValue(InfernalFurnaceBlock.FACING), Direction.NORTH), "bars side");
        helper.assertBlockPresent(ModBlocks.INFERNAL_FURNACE.get(), origin.offset(1, 1, 0));
        helper.assertTrue(helper.getBlockState(origin.offset(1, 1, 0)).getValue(InfernalFurnaceBlock.TILE) == InfernalTiles.BARS, "bars tile");
        InfernalFurnaceBlockEntity furnace = helper.<InfernalFurnaceBlockEntity>getBlockEntity(core);
        furnace.addSpeedyTime(600);
        helper.assertTrue(furnace.addItemsToInventory(new ItemStack(Items.IRON_ORE)), "ore rejected");
        helper.succeedWhen(() -> {
            int ingots = 0;
            for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(core)).inflate(4.0))) {
                if (entity.getItem().is(Items.IRON_INGOT)) {
                    ingots += entity.getItem().getCount();
                }
            }
            helper.assertTrue(ingots == 1, "ingot not smelted: " + ingots);
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void furnaceBonusNuggets(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 2, 1);
        buildFurnace(helper, origin);
        helper.assertTrue(form(helper, origin), "wand trigger did not form the furnace");
        InfernalFurnaceBlockEntity furnace = helper.<InfernalFurnaceBlockEntity>getBlockEntity(origin.offset(1, 1, 1));
        ServerLevel level = helper.getLevel();
        ItemStack ore = new ItemStack(Items.IRON_ORE);
        Item bonus = SmeltingBonuses.get(ore);
        helper.assertTrue(bonus == Items.IRON_NUGGET, "iron ore bonus");
        helper.assertTrue(SmeltingBonuses.get(new ItemStack(ModItems.NATIVE_GOLD_CLUSTER.get())) == Items.GOLD_NUGGET, "gold cluster bonus");
        helper.assertTrue(SmeltingBonuses.get(new ItemStack(Items.STONE)) == null, "stone has no bonus");
        for (int index = 0; index < 64; index++) {
            furnace.ejectItem(level, new ItemStack(Items.IRON_INGOT), ore);
        }
        int nuggets = 0;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(origin)).inflate(6.0))) {
            if (entity.getItem().is(Items.IRON_NUGGET)) {
                nuggets += entity.getItem().getCount();
            }
        }
        helper.assertTrue(nuggets > 0 && nuggets < 64, "bonus nuggets out of bounds: " + nuggets);
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void furnaceCollapses(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 2, 1);
        buildFurnace(helper, origin);
        helper.assertTrue(form(helper, origin), "wand trigger did not form the furnace");
        helper.getLevel().destroyBlock(helper.absolutePos(origin.offset(0, 0, 0)), false);
        helper.assertBlockPresent(Blocks.AIR, origin.offset(1, 1, 1));
        helper.assertBlockPresent(Blocks.OBSIDIAN, origin.offset(1, 0, 1));
        helper.assertBlockPresent(Blocks.IRON_BARS, origin.offset(1, 1, 0));
        helper.assertBlockPresent(Blocks.NETHER_BRICKS, origin.offset(2, 2, 2));
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(Blaze.class, new AABB(helper.absolutePos(origin)).inflate(5.0)).isEmpty(), "no blaze spawned");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void boreDigs(GameTestHelper helper) {
        BlockPos base = new BlockPos(1, 2, 3);
        BlockPos bore = base.above();
        helper.setBlock(base, ModBlocks.ARCANE_BORE_BASE.get());
        helper.setBlock(bore, ModBlocks.ARCANE_BORE.get().defaultBlockState().setValue(ArcaneBoreBlock.BASE, Direction.UP));
        helper.setBlock(bore.above(), Blocks.REDSTONE_BLOCK);
        for (int distance = 3; distance <= 5; distance++) {
            helper.setBlock(bore.east(distance), Blocks.STONE);
        }
        ArcaneBoreBlockEntity entity = helper.<ArcaneBoreBlockEntity>getBlockEntity(bore);
        entity.setOrientation(Direction.EAST, true);
        entity.setItem(ArcaneBoreBlockEntity.FOCUS_SLOT, new ItemStack(ModItems.FOCUS_EXCAVATION.get()));
        entity.setItem(ArcaneBoreBlockEntity.PICKAXE_SLOT, new ItemStack(Items.IRON_PICKAXE));
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.AIR, bore.east(3));
            int cobble = 0;
            for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(bore)).inflate(6.0))) {
                if (item.getItem().is(Items.COBBLESTONE)) {
                    cobble += item.getItem().getCount();
                }
            }
            helper.assertTrue(cobble > 0, "no cobblestone dropped");
            helper.assertTrue(entity.getItem(ArcaneBoreBlockEntity.PICKAXE_SLOT).getDamageValue() == 1, "pickaxe not damaged");
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void spaPlacesWater(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, ModBlocks.ARCANE_SPA.get());
        ArcaneSpaBlockEntity spa = helper.<ArcaneSpaBlockEntity>getBlockEntity(pos);
        spa.toggleMix();
        helper.assertTrue(spa.fillFrom(new FluidStack(Fluids.WATER, 5000)), "tank rejected water");
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.WATER, pos.above());
            helper.assertTrue(java.util.Objects.equals(spa.getTank().getAmount(), 4000), "tank after placing");
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void deviceRecipesResolve(GameTestHelper helper) {
        var furnace = (thaumcraft.crafting.CompoundRecipe) TcRecipes.get("InfernalFurnace");
        helper.assertTrue(furnace.blocks().get(13).create().is(Items.LAVA_BUCKET), "infernal furnace blueprint hides the required lava source");
        for (String key : new String[] {"ArcaneSpa", "ArcaneBoreBase", "ArcaneBore"}) {
            TcRecipe recipe = TcRecipes.get(key);
            helper.assertTrue(recipe != null, "recipe missing: " + key);
            TcStack result = recipe instanceof ArcaneRecipe arcane ? arcane.result() : recipe instanceof InfusionRecipe infusion ? infusion.result() : null;
            helper.assertTrue(result != null && result.exists(), "recipe result unresolved: " + key);
            helper.assertTrue(hasResearch(recipe.research()), "research missing: " + recipe.research());
        }
        helper.succeed();
    }

    private static boolean hasResearch(String key) {
        return ResearchCategories.getResearch(key) != null;
    }
}
