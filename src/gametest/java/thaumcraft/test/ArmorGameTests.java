package thaumcraft.test;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import thaumcraft.item.armor.ArmorEvents;
import thaumcraft.registry.ModEnchantments;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.RobeArmorDyeRecipe;
import thaumcraft.item.armor.Hover;
import thaumcraft.item.armor.RobeColor;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.research.Warp;
import thaumcraft.wand.WandManager;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class ArmorGameTests {
    private static ServerPlayer mockServerPlayer(GameTestHelper helper) {
        return TestSupport.mockServerPlayer(helper);
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void visDiscountSums(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ROBE_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.ROBE_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.ROBE_BOOTS.get()));
        helper.assertTrue(java.util.Objects.equals(Math.round(WandManager.getTotalVisDiscount(player, null) * 100.0F), 5), "robe discount");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.GOGGLES_OF_REVEALING.get()));
        helper.assertTrue(java.util.Objects.equals(Math.round(WandManager.getTotalVisDiscount(player, null) * 100.0F), 10), "robe and goggles discount");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void warpFromGear(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        helper.assertTrue(java.util.Objects.equals(Warp.getWarpFromGear(player), 0), "bare warp");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.VOID_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.VOID_ROBE_CHESTPLATE.get()));
        helper.assertTrue(java.util.Objects.equals(Warp.getWarpFromGear(player), 3), "void gear warp");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void robeDyeMixesColor(GameTestHelper helper) {
        ItemStack robe = new ItemStack(ModItems.ROBE_CHESTPLATE.get());
        CraftingInput input = CraftingInput.of(2, 1, List.of(robe, new ItemStack(Items.RED_DYE)));
        helper.assertTrue(RobeArmorDyeRecipe.INSTANCE.matches(input, helper.getLevel()), "recipe does not match");
        ItemStack result = RobeArmorDyeRecipe.INSTANCE.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.has(DataComponents.DYED_COLOR), "no color applied");
        int color = RobeColor.get(result);
        int red = color >> 16 & 0xFF;
        int blue = color & 0xFF;
        helper.assertTrue(red > 0x6A && blue < 0x80, "color not reddened");
        CraftingInput invalid = CraftingInput.of(2, 1, List.of(new ItemStack(ModItems.VOID_ROBE_CHESTPLATE.get()), new ItemStack(Items.RED_DYE)));
        helper.assertFalse(RobeArmorDyeRecipe.INSTANCE.matches(invalid, helper.getLevel()), "void robe matched robe recipe");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void hoverBurnsFuel(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        ItemStack armor = new ItemStack(ModItems.HOVER_HARNESS.get());
        ItemStack jar = new ItemStack(ModBlocks.FILLED_JAR.get());
        AspectKeys.ASPECTS.set(jar, new AspectList().add(Aspect.ENERGY, 2));
        helper.assertTrue(Hover.isFuel(jar), "jar is not fuel");
        Hover.setJar(armor, jar);
        player.setItemSlot(EquipmentSlot.CHEST, armor);
        armor = player.getItemBySlot(EquipmentSlot.CHEST);
        helper.assertTrue(java.util.Objects.equals(Hover.getHarnessFuel(armor), 2), "fuel");
        for (int tick = 0; tick < Hover.EFFICIENCY; tick++) {
            helper.assertTrue(Hover.expendCharge(player, armor), "charge ended early");
        }
        helper.assertTrue(java.util.Objects.equals(Hover.getHarnessFuel(armor), 2), "fuel before burn");
        helper.assertTrue(Hover.expendCharge(player, armor), "no fuel left after burn");
        helper.assertTrue(java.util.Objects.equals(Hover.getHarnessFuel(armor), 1), "fuel after burn");
        Hover.toggle(player);
        helper.assertTrue(Hover.isHovering(armor), "hover not toggled");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void robeDyeRecipesAndWashing(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(Thaumcraft.id("robe_armor_dye")).isPresent(), "robe dye recipe missing");
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(Thaumcraft.id("void_robe_armor_dye")).isPresent(), "void robe dye recipe missing");
        ServerPlayer player = mockServerPlayer(helper);
        ItemStack robe = new ItemStack(ModItems.ROBE_CHESTPLATE.get());
        RobeColor.set(robe, 0x123456);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, robe);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var water = net.minecraft.world.level.block.Blocks.WATER_CAULDRON.defaultBlockState()
            .setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3);
        helper.getLevel().setBlockAndUpdate(pos, water);
        var interaction = net.minecraft.core.cauldron.CauldronInteraction.WATER.map().get(robe.getItem());
        interaction.interact(water, helper.getLevel(), pos, player, net.minecraft.world.InteractionHand.MAIN_HAND, robe);
        helper.assertTrue(!robe.has(DataComponents.DYED_COLOR), "robe dye not washed");
        helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL) == 2, "water not consumed");
        helper.succeed();
    }


    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void travellerElytra(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        ArmorEvents.setMovingForward(player, true);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.BOOTS_TRAVELLER.get()));
        var step = player.getAttribute(Attributes.STEP_HEIGHT);
        double originalStep = step.getValue();
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        ArmorEvents.onPlayerTick(new PlayerTickEvent.Pre(player));
        helper.assertTrue(player.getDeltaMovement().horizontalDistance() > 0.05, "boots must still accelerate walking");
        helper.assertTrue(step.getValue() > originalStep, "boots must still increase step height");
        ArmorEvents.onJump(new LivingEvent.LivingJumpEvent(player));
        helper.assertTrue(player.getDeltaMovement().y > 0.27, "boots must still boost jumping");
        player.setOnGround(false);
        player.startFallFlying();
        Vec3 gliding = new Vec3(0.4, -0.05, 0.8);
        for (var boots : List.of(Items.LEATHER_BOOTS, ModItems.BOOTS_TRAVELLER.get())) {
            for (int haste : new int[]{0, 3}) {
                ItemStack stack = new ItemStack(boots);
                if (haste > 0) {
                    stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.HASTE), haste);
                }
                player.setItemSlot(EquipmentSlot.FEET, stack);
                player.setDeltaMovement(gliding);
                for (int tick = 0; tick < 80; tick++) {
                    ArmorEvents.onPlayerTick(new PlayerTickEvent.Pre(player));
                }
                helper.assertTrue(player.getDeltaMovement().equals(gliding), "boots/haste added elytra thrust: " + boots + " haste=" + haste);
                helper.assertTrue(Math.abs(step.getValue() - originalStep) < 1.0E-6, "gliding must remove the step modifier");
            }
        }
        player.stopFallFlying();
        player.fallDistance = 3;
        ArmorEvents.onPlayerTick(new PlayerTickEvent.Pre(player));
        helper.assertTrue(player.fallDistance < 3, "boots must still reduce fall distance");
        helper.succeed();
    }
}
