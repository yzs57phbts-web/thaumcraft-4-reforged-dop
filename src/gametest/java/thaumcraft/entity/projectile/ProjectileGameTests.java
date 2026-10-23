package thaumcraft.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.projectile.AlumentumProjectile;
import thaumcraft.entity.projectile.BottleTaint;
import thaumcraft.entity.projectile.EldritchOrb;
import thaumcraft.entity.projectile.GolemOrb;
import thaumcraft.registry.ModEffects;
import thaumcraft.registry.ModEntities;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class ProjectileGameTests {
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void alumentumExplodes(GameTestHelper helper) {
        BlockPos targetPos = helper.absolutePos(new BlockPos(3, 3, 3));
        LivingEntity target = (LivingEntity) EntityType.COW.create(helper.getLevel());
        helper.assertTrue(target != null, "cow factory returned null");
        target.moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().addFreshEntity(target);
        float health = target.getHealth();
        helper.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false, helper.getLevel().getServer());
        AlumentumProjectile projectile = new AlumentumProjectile(ModEntities.ALUMENTUM_PROJECTILE.get(), helper.getLevel());
        projectile.moveTo(target.getX(), target.getY() + 0.2, target.getZ(), 0.0F, 0.0F);
        projectile.onHit(new BlockHitResult(target.position(), Direction.UP, target.blockPosition(), false));
        helper.assertTrue(target.getHealth() < health, "alumentum impact did not damage a nearby entity");
        helper.assertTrue(helper.getLevel().getBlockState(targetPos.below()).is(Blocks.STONE), "alumentum damaged blocks while mob griefing was disabled");
        helper.assertTrue(projectile.isRemoved(), "alumentum projectile survived impact");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void bottleTaintPoisons(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        LivingEntity cow = (LivingEntity) EntityType.COW.create(helper.getLevel());
        LivingEntity zombie = (LivingEntity) EntityType.ZOMBIE.create(helper.getLevel());
        helper.assertTrue(cow != null && zombie != null, "test mob factory returned null");
        cow.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        zombie.moveTo(pos.getX() + 1.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(cow);
        helper.getLevel().addFreshEntity(zombie);
        BottleTaint bottle = new BottleTaint(ModEntities.BOTTLE_TAINT.get(), helper.getLevel());
        bottle.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        bottle.onHit(new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(cow.hasEffect(ModEffects.TAINT_POISON), "bottle taint did not poison a living non-undead target");
        helper.assertFalse(zombie.hasEffect(ModEffects.TAINT_POISON), "bottle taint poisoned an undead target");
        helper.assertTrue(bottle.isRemoved(), "bottle taint survived impact");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void dartDealsOneDamage(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        LivingEntity target = (LivingEntity) EntityType.COW.create(helper.getLevel());
        helper.assertTrue(target != null, "cow factory returned null");
        target.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(target);
        float health = target.getHealth();
        ExposedDart dart = new ExposedDart(ModEntities.DART.get(), helper.getLevel());
        dart.setDeltaMovement(1.0, 0.0, 0.0);
        dart.hit(new EntityHitResult(target));
        helper.assertValueEqual(target.getHealth(), health - 1.0F, "dart did not deal one point at unit velocity");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void eldritchOrbWeakens(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        Monster owner = (Monster) EntityType.ZOMBIE.create(helper.getLevel());
        LivingEntity target = (LivingEntity) EntityType.COW.create(helper.getLevel());
        helper.assertTrue(owner != null && target != null, "test mob factory returned null");
        owner.moveTo(pos.getX() - 1.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        target.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(target);
        float health = target.getHealth();
        EldritchOrb orb = new EldritchOrb(ModEntities.ELDRITCH_ORB.get(), helper.getLevel());
        orb.setOwner(owner);
        orb.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        orb.onHit(new EntityHitResult(target));
        helper.assertTrue(target.getHealth() < health, "eldritch orb did not damage the non-undead target");
        helper.assertTrue(target.hasEffect(MobEffects.WEAKNESS), "eldritch orb did not apply weakness");
        helper.assertValueEqual(target.getEffect(MobEffects.WEAKNESS).getDuration(), 160, "eldritch orb weakness duration");
        helper.succeed();
    }
    @GameTest(template = "test/platform", timeoutTicks = 240)
    public static void golemOrbDamageScaling(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        Monster owner = (Monster) EntityType.ZOMBIE.create(helper.getLevel());
        LivingEntity normalTarget = (LivingEntity) EntityType.COW.create(helper.getLevel());
        LivingEntity redTarget = (LivingEntity) EntityType.COW.create(helper.getLevel());
        helper.assertTrue(owner != null && normalTarget != null && redTarget != null, "cow factory returned null");
        owner.moveTo(pos.getX() - 2.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        normalTarget.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        redTarget.moveTo(pos.getX() + 1.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(normalTarget);
        helper.getLevel().addFreshEntity(redTarget);
        float normalHealth = normalTarget.getHealth();
        float redHealth = redTarget.getHealth();
        GolemOrb normal = new GolemOrb(ModEntities.GOLEM_ORB.get(), helper.getLevel());
        normal.setOwner(owner);
        normal.setRed(false);
        normal.moveTo(normalTarget.getX(), normalTarget.getY(), normalTarget.getZ(), 0.0F, 0.0F);
        normal.onHit(new EntityHitResult(normalTarget));
        GolemOrb red = new GolemOrb(ModEntities.GOLEM_ORB.get(), helper.getLevel());
        red.setOwner(owner);
        red.setRed(true);
        red.moveTo(redTarget.getX(), redTarget.getY(), redTarget.getZ(), 0.0F, 0.0F);
        red.onHit(new EntityHitResult(redTarget));
        helper.assertTrue(normalTarget.getHealth() < normalHealth, "normal golem orb did not damage its hit target");
        helper.assertTrue(redTarget.getHealth() < redHealth, "red golem orb did not damage its hit target");
        helper.assertTrue(redHealth - redTarget.getHealth() > normalHealth - normalTarget.getHealth(), "red golem orb did not deal more damage");
        helper.succeed();
    }

    private static final class ExposedDart extends DartProjectile {
        private ExposedDart(net.minecraft.world.entity.EntityType<? extends DartProjectile> type, net.minecraft.world.level.Level level) {
            super(type, level);
        }

        private void hit(EntityHitResult result) {
            super.onHitEntity(result);
        }
    }
}
