package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.effect.EffectHooks;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;

public class FluxGooBlock extends FluxBlock {
    public FluxGooBlock(Properties properties) {
        super(8, -1, 30, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        int quantaIndex = state.getValue(LEVEL) - 1;
        if (AlchemyHooks.isThaumicSlime(entity)) {
            if (level.isClientSide()) {
                return;
            }
            if (AlchemyHooks.getSlimeSize(entity) < quantaIndex && level.getRandom().nextBoolean()) {
                AlchemyHooks.setSlimeSize(entity, AlchemyHooks.getSlimeSize(entity) + 1);
                if (quantaIndex > 1) {
                    level.setBlock(pos, withLevel(quantaIndex), Block.UPDATE_ALL);
                } else {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            return;
        }
        double factor = 1.0F - state.getValue(LEVEL) / (float) QUANTA_PER_BLOCK;
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            EffectHooks.applyVisExhaust(living, 600, quantaIndex / 3);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        BlockState current = level.getBlockState(pos);
        if (!current.is(this)) {
            return;
        }
        int meta = current.getValue(LEVEL) - 1;
        boolean airAbove = level.isEmptyBlock(pos.above());
        if (meta >= 2 && meta < 6 && airAbove && random.nextInt(25) == 0) {
            spawnSlime(level, pos, 1);
        } else if (meta >= 6 && airAbove) {
            if (random.nextInt(25) == 0) {
                spawnSlime(level, pos, 2);
            } else if (random.nextInt(50) == 0) {
                AlchemyHooks.taintFromFlux(level, pos);
            }
        } else if (random.nextInt(30) == 0) {
            if (meta == 0) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            } else {
                level.setBlock(pos, withLevel(meta), Block.UPDATE_ALL);
                if (random.nextBoolean() && airAbove) {
                    level.setBlock(pos.above(), ModBlocks.FLUX_GAS.get().defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private void spawnSlime(ServerLevel level, BlockPos pos, int size) {
        if (AlchemyHooks.spawnThaumicSlime(level, pos, size)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ModSounds.GORE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }
}
