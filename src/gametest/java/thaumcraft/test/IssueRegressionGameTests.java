package thaumcraft.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.Cultist;
import thaumcraft.entity.projectile.GolemOrb;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.EldritchEntities;
import thaumcraft.registry.ModBlocks;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class IssueRegressionGameTests {
    @GameTest(template = "test/platform", batch = "issues_cultists", timeoutTicks = 120)
    public static void cultistsIgnoreFriendlyProjectileButRetaliateAgainstPlayer(GameTestHelper helper) {
        var level = helper.getLevel();
        Cultist cleric = helper.spawn(ModEntities.CULTIST_CLERIC.get(), new BlockPos(2, 2, 2));
        Cultist knight = helper.spawn(ModEntities.CULTIST_KNIGHT.get(), new BlockPos(4, 2, 2));
        var leader = EldritchEntities.CULTIST_LEADER.get().create(level);
        helper.assertTrue(cleric.isAlliedTo(knight) && knight.isAlliedTo(cleric), "cultist alliance is asymmetric");
        helper.assertTrue(cleric.isAlliedTo(leader) && leader.isAlliedTo(cleric), "leader alliance is asymmetric");
        for (Cultist victim : new Cultist[] {knight, cleric}) {
            Cultist shooter = victim == knight ? cleric : knight;
            victim.tickCount = 10;
            GolemOrb orb = GolemOrb.create(level, shooter, victim, true);
            try {
                var hit = GolemOrb.class.getDeclaredMethod("onHit", net.minecraft.world.phys.HitResult.class);
                hit.setAccessible(true);
                hit.invoke(orb, new net.minecraft.world.phys.EntityHitResult(victim));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
            helper.assertTrue(victim.getLastHurtByMob() != shooter, "friendly projectile replaced the real attacker");
            helper.assertFalse(victim.canAttack(shooter), "cultist can attack friendly projectile owner");
            helper.assertFalse(victim.canAttack(leader), "cultist can attack leader");
            helper.assertFalse(retaliation(victim).canUse(), "friendly fire started retaliation");
            victim.invulnerableTime = 0;
            var player = TestSupport.mockServerPlayer(helper);
            player.moveTo(victim.position().add(1.0, 0.0, 0.0));
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            victim.hurt(level.damageSources().playerAttack(player), 1.0F);
            helper.assertTrue(retaliation(victim).canUse(), "cultist stopped retaliating against players");
        }
        helper.succeed();
    }

    private static HurtByTargetGoal retaliation(Cultist cultist) {
        return cultist.targetSelector.getAvailableGoals().stream().map(goal -> goal.getGoal())
            .filter(HurtByTargetGoal.class::isInstance).map(HurtByTargetGoal.class::cast).findFirst().orElseThrow();
    }

    @GameTest(template = "test/platform", batch = "issues_purifying_motion", timeoutTicks = 120)
    public static void purifyingFluidAllowsTravel(GameTestHelper helper) {
        checkTravel(helper, ModBlocks.PURIFYING_FLUID.get());
    }

    @GameTest(template = "test/platform", batch = "issues_liquid_death_motion", timeoutTicks = 120)
    public static void liquidDeathAllowsTravel(GameTestHelper helper) {
        checkTravel(helper, ModBlocks.LIQUID_DEATH.get());
    }

    @GameTest(template = "test/platform", batch = "issues_water_control", timeoutTicks = 120)
    public static void vanillaWaterControlAllowsTravel(GameTestHelper helper) {
        checkTravel(helper, Blocks.WATER);
    }

    @GameTest(template = "test/eldritch_regression", batch = "issues_purifying_flow", timeoutTicks = 160)
    public static void purifyingFluidFindsDropAndHasBoundedSpread(GameTestHelper helper) {
        BlockPos source = new BlockPos(16, 3, 16);
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                helper.setBlock(source.offset(x, -1, z), Blocks.STONE);
                helper.setBlock(source.offset(x, 0, z), Blocks.AIR);
            }
        }
        helper.setBlock(source.offset(2, -1, 0), Blocks.AIR);
        helper.setBlock(source, ModBlocks.PURIFYING_FLUID.get());
        helper.runAfterDelay(6, () -> {
            helper.assertFalse(helper.getBlockState(source.east()).getFluidState().isEmpty(), "purifying fluid did not choose nearby drop");
            helper.assertTrue(helper.getBlockState(source.west()).getFluidState().isEmpty(), "purifying fluid ignored nearby drop and spread the wrong way");
            helper.runAfterDelay(80, () -> {
                for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                    helper.assertTrue(helper.getBlockState(source.relative(direction, 9)).getFluidState().isEmpty(), "purifying source spread beyond water-like range");
                }
                helper.succeed();
            });
        });
    }

    private static void checkTravel(GameTestHelper helper, Block fluid) {
        BlockPos pos = new BlockPos(3, 3, 3);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                helper.setBlock(pos.offset(x, -1, z), Blocks.STONE);
                helper.setBlock(pos.offset(x, 0, z), fluid);
            }
        }
        Pig pig = helper.spawn(EntityType.PIG, pos);
        pig.setNoAi(true);
        pig.setInvulnerable(true);
        pig.setNoGravity(true);
        pig.tick();
        helper.assertTrue((pig.isInFluidType() || pig.isInWater()), "test mob did not enter fluid");
        pig.setNoAi(false);
        Vec3 before = pig.position();
        pig.setDeltaMovement(Vec3.ZERO);
        pig.travel(new Vec3(0.0, 0.0, 1.0));
        helper.assertTrue(pig.position().distanceToSqr(before) > 0.00001, "entity cannot move through " + fluid);
        helper.succeed();
    }
}
