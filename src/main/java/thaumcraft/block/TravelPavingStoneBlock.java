package thaumcraft.block;

import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TravelPavingStoneBlock extends Block {
    public TravelPavingStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity living) {
            if (level.isClientSide()) {
                for (int index = 0; index < 5; index++) {
                    level.addParticle(
                        new DustParticleOptions(Vec3.fromRGB24(0x008000).toVector3f(), 1.0F),
                        pos.getX() + level.getRandom().nextFloat(),
                        pos.getY() + 1.0,
                        pos.getZ() + level.getRandom().nextFloat(),
                        0.0,
                        0.0,
                        0.0
                    );
                }
            } else {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1));
                living.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0));
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
