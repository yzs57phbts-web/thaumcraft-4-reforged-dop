package thaumcraft.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.block.InfusedStoneBlock;
import thaumcraft.client.geom.EldritchLockGeometry;
import thaumcraft.registry.EldritchEntities;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModBlocks;
import thaumcraft.world.gen.OreGeneration;

@GameTestHolder("bbtt_tc_tests")
@PrefixGameTestTemplate(false)
public final class PackFixGameTests {
    private static ServerPlayer armored(GameTestHelper helper, boolean fortress) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        player.setHealth(1000);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(fortress ? ModItems.FORTRESS_HELMET.get() : ModItems.VOID_ROBE_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(fortress ? ModItems.FORTRESS_CHESTPLATE.get() : ModItems.VOID_ROBE_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(fortress ? ModItems.FORTRESS_LEGGINGS.get() : ModItems.VOID_ROBE_LEGGINGS.get()));
        player.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 2, 3.5)));
        for (int tick = 0; tick < 61; tick++) player.tick();
        player.doTick();
        player.setHealth(1000);
        return player;
    }

    private static void damage(GameTestHelper helper, ServerPlayer player, DamageSource source, float expected) {
        player.invulnerableTime = 0;
        player.setHealth(1000);
        player.hurt(source, 10);
        float actual = 1000 - player.getHealth();
        helper.assertTrue(Math.abs(actual - expected) < 0.001, "Expected " + expected + " damage, got " + actual);
    }

    @GameTest(template = "test/platform")
    public static void fortressProtection(GameTestHelper helper) {
        var player = armored(helper, true);
        // Three fortress pieces, no boots: 20 armor (including +4 set bonus), 6 toughness.
        helper.assertTrue(player.getArmorValue() == 20, "Three-piece modern fortress armor");
        helper.assertTrue(player.getAttributeValue(Attributes.ARMOR_TOUGHNESS) == 6, "Three-piece fortress toughness");
        damage(helper, player, helper.getLevel().damageSources().cactus(), 3.142857F);
        damage(helper, player, helper.getLevel().damageSources().magic(), 10 * (1 - 16 * 1.25F / 35));
        damage(helper, player, helper.getLevel().damageSources().inFire(), 2.357143F);
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void voidRobeProtection(GameTestHelper helper) {
        var player = armored(helper, false);
        // Three void robe pieces, no boots: 16 armor, 6 toughness, no physical set bonus.
        helper.assertTrue(player.getArmorValue() == 16, "Three-piece void robe armor");
        helper.assertTrue(player.getAttributeValue(Attributes.ARMOR_TOUGHNESS) == 6, "Three-piece void robe toughness");
        damage(helper, player, helper.getLevel().damageSources().cactus(), 4.742857F);
        damage(helper, player, helper.getLevel().damageSources().magic(), 10 * (1 - 16F / 35));
        damage(helper, player, helper.getLevel().damageSources().fall(), 10);
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void modernArmorLastDurability(GameTestHelper helper) {
        var player = armored(helper, true);
        for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
            var stack = player.getItemBySlot(slot);
            stack.setDamageValue(stack.getMaxDamage() - 1);
        }
        // Modern protection stays effective on the last hit; pure magic does not wear vanilla armor.
        damage(helper, player, helper.getLevel().damageSources().magic(), 10 * (1 - 16 * 1.25F / 35));
        for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
            var stack = player.getItemBySlot(slot);
            helper.assertTrue(stack.getDamageValue() == stack.getMaxDamage() - 1, "Magic must not wear armor");
        }
        damage(helper, player, helper.getLevel().damageSources().cactus(), 3.142857F);
        for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
            helper.assertTrue(player.getItemBySlot(slot).isEmpty(), "Last physical hit must break the worn piece");
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void phaseDamageAndSave(GameTestHelper helper) {
        var golem = EldritchEntities.ELDRITCH_GOLEM.get().create(helper.getLevel());
        double first = golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
        golem.setHeadless(true);
        helper.assertTrue(golem.getAttributeValue(Attributes.ATTACK_DAMAGE) == first * 2, "Phase two must double damage");
        golem.setHeadless(true);
        helper.assertTrue(golem.getAttributeValue(Attributes.ATTACK_DAMAGE) == first * 2, "Modifier stacked");
        CompoundTag tag = new CompoundTag();
        golem.saveWithoutId(tag);
        var loaded = EldritchEntities.ELDRITCH_GOLEM.get().create(helper.getLevel());
        loaded.load(tag);
        helper.assertTrue(loaded.isHeadless() && loaded.getAttributeValue(Attributes.ATTACK_DAMAGE) == first * 2, "Phase damage lost after loading");
        loaded.setHeadless(false);
        helper.assertTrue(loaded.getAttributeValue(Attributes.ATTACK_DAMAGE) == first, "Phase modifier not removed");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void deepslateOreHost(GameTestHelper helper) {
        for (var block : java.util.List.of(ModBlocks.EARTH_INFUSED_STONE.get(), ModBlocks.CINNABAR_ORE.get(), ModBlocks.AMBER_ORE.get())) {
            var ore = block.defaultBlockState();
            helper.assertTrue(OreGeneration.withHostRock(ore, Blocks.DEEPSLATE.defaultBlockState()).getValue(InfusedStoneBlock.DEEPSLATE), "Deep host lost: " + block);
            helper.assertFalse(OreGeneration.withHostRock(ore, Blocks.STONE.defaultBlockState()).getValue(InfusedStoneBlock.DEEPSLATE), "Stone host became deep: " + block);
            var pos = helper.absolutePos(new net.minecraft.core.BlockPos(2, 2, 2));
            helper.getLevel().setBlockAndUpdate(pos, ore.setValue(InfusedStoneBlock.DEEPSLATE, true));
            helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(InfusedStoneBlock.DEEPSLATE), "Ore state not stored: " + block);
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void generatedOreHost(GameTestHelper helper) {
        OreGenerationGameTests.deepHosts(helper);
    }

    @GameTest(template = "test/platform")
    public static void purpleTaintWater(GameTestHelper helper) {
        var biome = helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
            .get(Thaumcraft.id("tainted_land"));
        helper.assertTrue(biome != null && biome.getWaterColor() == 0x6D4189 && biome.getWaterFogColor() == 0x281B38,
            "Taint water registry did not load the purple surface and underwater colors");
        helper.succeed();
    }

    @GameTest(template = "test/platform")
    public static void doorGeometry(GameTestHelper helper) {
        for (int[] normal : new int[][]{{0,-1},{0,1},{-1,0},{1,0}}) {
            var positions = new java.util.HashSet<String>();
            for (int side=0;side<4;side++) for (int segment=1;segment<=4;segment++) {
                var p=EldritchLockGeometry.segment(side,segment,normal[0],normal[1],42);
                helper.assertTrue(positions.add(p.x()+":"+p.y()+":"+p.z()), "Door segments overlap");
                helper.assertTrue(p.x()*normal[0]+p.z()*normal[1] == 0, "Segment outside door plane");
            }
            for (int side=0;side<4;side++) helper.assertTrue(EldritchLockGeometry.segmentCount(side,100)==0,"Door did not fully open");
        }
        helper.succeed();
    }
}
