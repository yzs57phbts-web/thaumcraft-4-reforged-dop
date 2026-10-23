package thaumcraft.client.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.boss.EldritchGolem;
import thaumcraft.entity.boss.EldritchWarden;
import thaumcraft.client.geom.LightningBolt;
import thaumcraft.client.fx.BoltRenderer;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.registry.ModBlocks;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class EldritchFx {
    private EldritchFx() {
    }

    @SubscribeEvent
    static void tick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof EldritchGolem golem) {
            golem(golem);
        } else if (entity instanceof EldritchWarden warden) {
            warden(warden);
        }
    }

    private static void golem(EldritchGolem golem) {
        if (golem.isHeadless()) {
            double yaw = Math.toRadians(golem.getYRot());
            double ventX = golem.getX() - Math.sin(yaw);
            double ventY = golem.getY() + golem.getEyeHeight() - 0.75;
            double ventZ = golem.getZ() + Math.cos(yaw);
            WorldFxRenderer.add(new GenericFx(golem.getRandom(), ventX, ventY, ventZ, 0.0, 0.001, 0.0, true, 112, 9, 1, 6, 0, 0.4F)
                .color(0.65F, 0.85F, 1.0F));
            if (golem.getRandom().nextInt(20) == 0) {
                WorldFxRenderer.add(new GenericFx(golem.getRandom(), ventX, ventY + 0.5, ventZ, 0.0, 0.005, 0.0, false, 147, 4, 1, 6, 0, 0.3F)
                    .color(0.3F, 0.65F, 1.0F));
            }
            if (golem.getArcTicks() > 0) {
                BlockPos target = golem.getArcTarget();
                LightningBolt bolt = new LightningBolt(golem.getX(), golem.getY() + golem.getBbHeight() / 2.0, golem.getZ(),
                    target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, golem.getId() * 31L + golem.tickCount)
                    .setType(6)
                    .setWidth(0.025F);
                bolt.fractal(4, 0.3F, 0.6F, 0.1F, 0.3F);
                BoltRenderer.add(bolt);
            }
        }
        if (golem.getDeltaMovement().horizontalDistanceSqr() <= 2.5000003E-7 || golem.getRandom().nextInt(5) != 0) {
            return;
        }
        BlockPos under = golem.blockPosition().below();
        BlockState state = golem.level().getBlockState(under);
        if (state.isAir()) {
            return;
        }
        golem.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
            golem.getX() + (golem.getRandom().nextFloat() - 0.5F) * golem.getBbWidth(),
            golem.getBoundingBox().minY + 0.1,
            golem.getZ() + (golem.getRandom().nextFloat() - 0.5F) * golem.getBbWidth(),
            4.0 * (golem.getRandom().nextFloat() - 0.5F),
            0.5,
            (golem.getRandom().nextFloat() - 0.5F) * 4.0);
    }

    private static void warden(EldritchWarden warden) {
        int frenzy = warden.getFrenzyTicks();
        if (frenzy <= 0 || warden.tickCount % 10 != 0) {
            return;
        }
        double radius = (150.0 - frenzy) / 8.0;
        int stride = 1 + frenzy / 8;
        Level level = warden.level();
        for (int angle = 0; angle < 180; angle += 2 * stride) {
            double radians = Math.toRadians(angle);
            BlockPos pos = BlockPos.containing(warden.getX() + radius * Math.cos(radians), warden.getBlockY(), warden.getZ() + radius * Math.sin(radians));
            if (level.getBlockState(pos).is(ModBlocks.SAPPING_FIELD.get()) && warden.getRandom().nextFloat() < 0.3F) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.02, 0.0);
            }
        }
    }

}
