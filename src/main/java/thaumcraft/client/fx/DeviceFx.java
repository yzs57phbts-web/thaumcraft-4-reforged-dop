package thaumcraft.client.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlocks;

public final class DeviceFx {
    private DeviceFx() {
    }

    public static void wardingStone(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        if (level.hasNeighborSignal(pos)) {
            for (int index = 0; index < ClientFx.particleCount(2); index++) {
                Fx.get().blockRunes(level, pos.getX(), pos.getY() + 0.7, pos.getZ(),
                    0.2F + random.nextFloat() * 0.4F, random.nextFloat() * 0.3F, 0.8F + random.nextFloat() * 0.2F, 20, -0.02F);
            }
        } else if (missingField(level, pos)) {
            for (int index = 0; index < ClientFx.particleCount(3); index++) {
                Fx.get().blockRunes(level, pos.getX(), pos.getY() + 0.7, pos.getZ(),
                    0.9F + random.nextFloat() * 0.1F, random.nextFloat() * 0.3F, random.nextFloat() * 0.3F, 24, -0.02F);
            }
        } else {
            for (var entity : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos).inflate(1.0), entity -> !(entity instanceof Player))) {
                if (ClientFx.particleCount(1) > 0) {
                    Fx.get().blockRunes(level, pos.getX(),
                        pos.getY() + 0.6 + random.nextFloat() * Math.max(0.8F, entity.getEyeHeight()), pos.getZ(),
                        0.6F + random.nextFloat() * 0.4F, 0.0F, 0.3F + random.nextFloat() * 0.7F, 20, 0.0F);
                }
                break;
            }
        }
    }

    private static boolean missingField(Level level, BlockPos pos) {
        return !level.getBlockState(pos.above()).is(ModBlocks.WARDING_AURA.get())
            && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
            || !level.getBlockState(pos.above(2)).is(ModBlocks.WARDING_AURA.get())
            && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    public static void nitorParticles(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        if (random.nextInt(9 - ClientFx.particleCount(2)) == 0) {
            wisp(
                level,
                pos.getX() + 0.5F,
                pos.getY() + 0.5F,
                pos.getZ() + 0.5F,
                pos.getX() + 0.3F + random.nextFloat() * 0.4F,
                pos.getY() + 0.5F,
                pos.getZ() + 0.3F + random.nextFloat() * 0.4F,
                0.5F,
                4,
                -0.025F
            );
        }
        if (random.nextInt(15 - ClientFx.particleCount(4)) == 0) {
            wisp(
                level,
                pos.getX() + 0.5F,
                pos.getY() + 0.5F,
                pos.getZ() + 0.5F,
                pos.getX() + 0.4F + random.nextFloat() * 0.2F,
                pos.getY() + 0.5F,
                pos.getZ() + 0.4F + random.nextFloat() * 0.2F,
                0.25F,
                1,
                -0.02F
            );
        }
    }

    private static void wisp(Level level, double x, double y, double z, double targetX, double targetY, double targetZ, float size, int type, float gravity) {
        WispFx fx = new WispFx(level.getRandom(), x, y, z, size, type, null);
        fx.moveTo(targetX, targetY, targetZ);
        fx.shrink(true);
        fx.gravity(gravity);
        WorldFxRenderer.add(fx);
    }

    public static void nitorSparkle(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        double x = pos.getX() + 0.5F;
        double y = pos.getY() + 0.5F;
        double z = pos.getZ() + 0.5F;
        SparkleFx fx = new SparkleFx(random, x, y, z, 1.0F, 6, 3);
        int maxAge = 9;
        fx.motion(
            (pos.getX() + 0.5F + (random.nextFloat() - random.nextFloat()) / 3.0F - x) / maxAge,
            (pos.getY() + 0.5F + (random.nextFloat() - random.nextFloat()) / 3.0F - y) / maxAge,
            (pos.getZ() + 0.5F + (random.nextFloat() - random.nextFloat()) / 3.0F - z) / maxAge
        );
        fx.gravity(0.05F);
        WorldFxRenderer.add(fx);
    }

    public static void crystalSpark(Level level, double x, double y, double z, float size, int color) {
        RandomSource random = level.getRandom();
        CrystalSparkFx fx = new CrystalSparkFx(random, x, y, z, size);
        fx.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
        fx.alpha(0.8F);
        WorldFxRenderer.add(fx);
    }
}
