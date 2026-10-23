package thaumcraft.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModEntities;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class ThaumicSlimeGameTests {
    private static ThaumicSlime createSlime(GameTestHelper helper) {
        ThaumicSlime slime = ModEntities.THAUMIC_SLIME.get().create(helper.getLevel());
        helper.assertTrue(slime != null, "thaumic slime factory returned null");
        BlockPos pos = helper.absolutePos(new BlockPos(2, 32, 2));
        slime.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 137.0F, 0.0F);
        slime.setSlimeSize(1);
        slime.getRandom().setSeed(0L);
        return slime;
    }

    @GameTest(template = "test/platform", timeoutTicks = 100)
    public static void facesRandomJumps(GameTestHelper helper) {
        ThaumicSlime slime = createSlime(helper);
        checkJumps(helper, slime, null);
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 100)
    public static void facesMergeJumps(GameTestHelper helper) {
        ThaumicSlime slime = createSlime(helper);
        ThaumicSlime target = createSlime(helper);
        target.setPos(slime.getX() + 6.0, slime.getY(), slime.getZ() - 3.0);
        target.tickCount = 101;
        target.setNoAi(true);
        helper.getLevel().addFreshEntity(target);
        checkJumps(helper, slime, target);
        target.discard();
        helper.succeed();
    }

    private static void checkJumps(GameTestHelper helper, ThaumicSlime slime, ThaumicSlime target) {
        int jumps = 0;
        for (int tick = 0; tick < 120; tick++) {
            slime.setOnGround(true);
            slime.setDeltaMovement(Vec3.ZERO);
            slime.customServerAiStep();
            Vec3 movement = slime.getDeltaMovement();
            if (movement.y <= 0.0) {
                continue;
            }
            jumps++;
            Vec3 horizontal = new Vec3(movement.x, 0.0, movement.z).normalize();
            Vec3 facing = Vec3.directionFromRotation(0.0F, slime.yBodyRot);
            helper.assertTrue(facing.dot(horizontal) > 0.999, "slime body does not face its jump direction");
            helper.assertTrue(Math.abs(Mth.wrapDegrees(slime.getYRot() - slime.yBodyRot)) < 0.001F, "slime yaw differs from body yaw");
            helper.assertTrue(Math.abs(Mth.wrapDegrees(slime.getYHeadRot() - slime.yBodyRot)) < 0.001F, "slime head differs from body yaw");
            if (target != null) {
                Vec3 toTarget = target.position().subtract(slime.position()).normalize();
                helper.assertTrue(horizontal.dot(toTarget) > 0.999, "merge jump does not approach the other slime");
            }
            slime.setOnGround(false);
            slime.customServerAiStep();
            slime.tickHeadTurn(slime.yBodyRot, 0.0F);
            helper.assertTrue(Vec3.directionFromRotation(0.0F, slime.yBodyRot).dot(horizontal) > 0.999, "slime turns away from its jump while airborne");
        }
        helper.assertTrue(jumps >= 3, "not enough slime jumps were exercised");
    }
}
