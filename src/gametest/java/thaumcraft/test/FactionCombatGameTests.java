package thaumcraft.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.Pech;
import thaumcraft.entity.projectile.GolemOrb;
import thaumcraft.registry.EldritchEntities;
import thaumcraft.registry.ModEntities;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class FactionCombatGameTests {
    private static HurtByTargetGoal retaliation(Mob mob) {
        return mob.targetSelector.getAvailableGoals().stream().map(goal -> goal.getGoal())
            .filter(HurtByTargetGoal.class::isInstance).map(HurtByTargetGoal.class::cast).findFirst().orElseThrow();
    }

    private static void hit(Object projectile, Class<?> owner, String method, Class<?> parameter, Entity victim) {
        try {
            var hit = owner.getDeclaredMethod(method, parameter);
            hit.setAccessible(true);
            hit.invoke(projectile, new EntityHitResult(victim));
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }

    @GameTest(template="test/platform", batch="faction_cultists", timeoutTicks=120)
    public static void allCultistRanksIgnoreAlliedOrbsAndPreserveEnemy(GameTestHelper helper) {
        var level = helper.getLevel();
        var types = List.<EntityType<? extends Mob>>of(ModEntities.CULTIST_KNIGHT.get(), ModEntities.CULTIST_CLERIC.get(), EldritchEntities.CULTIST_LEADER.get());
        var player = TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        for (var victimType : types) for (var shooterType : types) {
            var victim = helper.spawn(victimType, new BlockPos(2, 2, 2));
            var shooter = helper.spawn(shooterType, new BlockPos(4, 2, 2));
            // Random warded champions can absorb the entire hit; this test concerns faction AI.
            thaumcraft.entity.monster.ChampionKeys.MODIFIER.set(victim, thaumcraft.entity.monster.ChampionModifier.NORMAL);
            victim.setAbsorptionAmount(0);
            victim.tickCount = 10;
            player.moveTo(victim.position().add(1, 0, 0));
            helper.assertTrue(victim.isAlliedTo(shooter) && !victim.canAttack(shooter), "Missing alliance: " + victimType + " / " + shooterType);
            float health = victim.getHealth();
            hit(GolemOrb.create(level, shooter, victim, true), GolemOrb.class, "onHit", HitResult.class, victim);
            helper.assertTrue(victim.getHealth() < health, "Friendly orb damage unexpectedly disabled");
            helper.assertTrue(victim.getLastHurtByMob() == null && !retaliation(victim).canUse(), "Allied orb started retaliation");
            victim.setTarget(shooter);
            helper.assertTrue(victim.getTarget() == null, "Direct allied targeting bypassed protection");
            victim.invulnerableTime = 0;
            victim.hurt(level.damageSources().playerAttack(player), 1);
            helper.assertTrue(victim.getLastHurtByMob() == player && retaliation(victim).canUse(), "Player retaliation broken");
            retaliation(victim).start();
            victim.invulnerableTime = 0;
            hit(GolemOrb.create(level, shooter, victim, true), GolemOrb.class, "onHit", HitResult.class, victim);
            victim.setTarget(shooter);
            helper.assertTrue(victim.getTarget() == player && victim.getLastHurtByMob() == player, "Friendly hit replaced player enemy");
            victim.discard();
            shooter.discard();
        }
        helper.succeed();
    }

    @GameTest(template="test/platform", batch="faction_fireballs", timeoutTicks=120)
    public static void clericFireballDoesNotStartFriendlyRetaliation(GameTestHelper helper) {
        var shooter = helper.spawn(ModEntities.CULTIST_CLERIC.get(), new BlockPos(2, 2, 2));
        var victim = helper.spawn(ModEntities.CULTIST_KNIGHT.get(), new BlockPos(4, 2, 2));
        thaumcraft.entity.monster.ChampionKeys.MODIFIER.set(victim, thaumcraft.entity.monster.ChampionModifier.NORMAL);
        victim.setAbsorptionAmount(0);
        victim.tickCount = 10;
        float health = victim.getHealth();
        var fireball = new SmallFireball(helper.getLevel(), shooter, new Vec3(1, 0, 0));
        hit(fireball, SmallFireball.class, "onHitEntity", EntityHitResult.class, victim);
        helper.assertTrue(victim.getHealth() < health, "Fireball did not actually hit cultist");
        helper.assertTrue(victim.getLastHurtByMob() == null && !retaliation(victim).canUse(), "Fireball caused friendly retaliation");
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(victim.getTarget() == null && shooter.getTarget() == null, "Cultists acquired allied target after AI ticks");
            helper.succeed();
        });
    }

    @GameTest(template="test/platform", batch="faction_pech_arrow", timeoutTicks=120)
    public static void pechArrowsAndMeleeDoNotStartInfighting(GameTestHelper helper) {
        var shooter = helper.spawn(ModEntities.PECH.get(), new BlockPos(2, 2, 2));
        shooter.setPechType(Pech.PechType.ARCHER);
        for (var type : Pech.PechType.values()) {
            var victim = helper.spawn(ModEntities.PECH.get(), new BlockPos(4, 2, 2));
            thaumcraft.entity.monster.ChampionKeys.MODIFIER.set(victim, thaumcraft.entity.monster.ChampionModifier.NORMAL);
            victim.setAbsorptionAmount(0);
            victim.setPechType(type);
            victim.setTamed(true);
            victim.tickCount = 10;
            float health = victim.getHealth();
            var arrow = new Arrow(helper.getLevel(), shooter, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
            arrow.setDeltaMovement(1, 0, 0);
            hit(arrow, AbstractArrow.class, "onHitEntity", EntityHitResult.class, victim);
            helper.assertTrue(victim.getHealth() < health, "Pech arrow did not actually hit " + type);
            helper.assertTrue(victim.getLastHurtByMob() == null && !retaliation(victim).canUse(), "Pech retaliated against arrow owner");
            victim.invulnerableTime = 0;
            victim.hurt(helper.getLevel().damageSources().mobAttack(shooter), 1);
            victim.setTarget(shooter);
            victim.targetSelector.tick();
            helper.assertTrue(victim.isAlliedTo(shooter) && !victim.canAttack(shooter) && victim.getTarget() == null, "Pech melee/direct target bypassed protection");
            helper.assertTrue(victim.isTamed() && victim.getAnger() == 0, "Friendly damage angered or untamed Pech");
            victim.discard();
        }
        helper.succeed();
    }

    @GameTest(template="test/platform", batch="faction_pech_player", timeoutTicks=120)
    public static void pechGroupStillRetaliatesAgainstPlayerAfterFriendlyHit(GameTestHelper helper) {
        var victim = helper.spawn(ModEntities.PECH.get(), new BlockPos(2, 2, 2));
        var neighbor = helper.spawn(ModEntities.PECH.get(), new BlockPos(4, 2, 2));
        var player = TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(victim.position().add(1, 0, 0));
        victim.setTamed(true);
        neighbor.setTamed(true);
        victim.tickCount = 10;
        victim.hurt(helper.getLevel().damageSources().playerAttack(player), 1);
        helper.assertTrue(victim.getTarget() == player && neighbor.getTarget() == player, "Pech group no longer defends against player");
        helper.assertTrue(victim.getAnger() > 0 && neighbor.getAnger() > 0 && !victim.isTamed() && !neighbor.isTamed(), "Player damage no longer causes normal anger");
        helper.assertTrue(retaliation(victim).canUse(), "Pech retaliation against player disabled");
        victim.invulnerableTime = 0;
        victim.hurt(helper.getLevel().damageSources().mobAttack(neighbor), 1);
        victim.setTarget(neighbor);
        helper.assertTrue(victim.getTarget() == player && victim.getLastHurtByMob() == player, "Friendly Pech hit replaced player enemy");
        helper.succeed();
    }
}
