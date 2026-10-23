package thaumcraft.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.eldritch.EldritchCrab;
import thaumcraft.registry.ModEntities;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class EldritchCrabGameTests {
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void eldritchCrabBreaksHelm(GameTestHelper helper) {
        EldritchCrab crab = ModEntities.ELDRITCH_CRAB.get().create(helper.getLevel());
        helper.assertTrue(crab != null, "eldritch crab factory returned null");
        net.minecraft.core.BlockPos crabPos = helper.absolutePos(new net.minecraft.core.BlockPos(2, 2, 2));
        crab.moveTo(crabPos.getX(), crabPos.getY(), crabPos.getZ(), 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(crab);
        crab.setHelm(true);
        helper.assertTrue(crab.hurt(helper.getLevel().damageSources().generic(), 20.0F), "damage did not hit the crab");
        helper.assertFalse(crab.hasHelm(), "helm did not break at half health");
        helper.assertValueEqual(crab.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED), 0.3, "unhelmeted movement speed");
        helper.assertValueEqual(crab.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR), 0.0, "unhelmeted armor");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void eldritchCrabMountsRevengeTarget(GameTestHelper helper) {
        EldritchCrab crab = ModEntities.ELDRITCH_CRAB.get().create(helper.getLevel());
        helper.assertTrue(crab != null, "eldritch crab factory returned null");
        Player target = helper.makeMockPlayer(GameType.SURVIVAL);
        net.minecraft.core.BlockPos targetPos = helper.absolutePos(new net.minecraft.core.BlockPos(2, 2, 2));
        target.moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(target);
        crab.moveTo(targetPos.getX() + 0.5, targetPos.getY() + 1.5, targetPos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(crab);
        crab.setLastHurtByMob(target);
        crab.setHelm(false);
        crab.getRandom().setSeed(0L);
        crab.tick();
        helper.assertTrue(crab.getVehicle() == target, "crab did not mount its airborne revenge target");
        helper.succeed();
    }
}
