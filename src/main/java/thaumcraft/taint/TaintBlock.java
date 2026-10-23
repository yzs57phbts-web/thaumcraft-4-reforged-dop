package thaumcraft.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.alchemy.FluxGooBlock;
import thaumcraft.effect.EffectHooks;
import thaumcraft.entity.TaintedMob;
import thaumcraft.entity.monster.taint.FallingTaint;
import thaumcraft.entity.monster.taint.TaintSwarmer;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModSounds;

public class TaintBlock extends Block {
    public enum Kind {
        CRUST,
        SOIL,
        FLESH
    }

    private final Kind kind;

    public TaintBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        return new SoundType(0.5F, 0.8F, ModSounds.GORE.get(), ModSounds.GORE.get(), ModSounds.GORE.get(), ModSounds.GORE.get(), ModSounds.GORE.get());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return kind != Kind.FLESH;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == Kind.FLESH) {
            return;
        }
        int adjacent = TaintSpread.adjacentTaint(level, pos);
        TaintBiomes.spread(level, pos, adjacent, 200);
        if (kind == Kind.CRUST) {
            if (tryToFall(level, pos, pos)) {
                return;
            }
            if (level.isEmptyBlock(pos.above())) {
                Direction direction = Direction.from2DDataValue(random.nextInt(4));
                BlockPos target = pos.relative(direction);
                boolean canShift = true;
                for (int depth = 0; depth < 4; depth++) {
                    if (!level.isEmptyBlock(target.below(depth)) || !level.getBlockState(pos.below(depth)).is(this)) {
                        canShift = false;
                        break;
                    }
                }
                if (canShift && tryToFall(level, pos, target)) {
                    return;
                }
            }
        }
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
        if (TaintBiomes.isTainted(level, target)) {
            TaintSpread.spreadFibres(level, target);
            if (kind == Kind.CRUST && level.isEmptyBlock(pos.above()) && random.nextInt(200) == 0) {
                AABB nearbySwarmer = new AABB(pos).inflate(16.0);
                if (level.getEntitiesOfClass(TaintSwarmer.class, nearbySwarmer).isEmpty()) {
                    TaintSwarmer swarmer = ModEntities.TAINT_SPORE_SWARMER.get().create(level);
                    if (swarmer != null) {
                        swarmer.absMoveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                        if (level.addFreshEntity(swarmer)) {
                            level.removeBlock(pos, false);
                            level.playSound(null, pos, thaumcraft.registry.ModSounds.ROOTS.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.1F, 0.9F + random.nextFloat() * 0.2F);
                        }
                    }
                }
            } else if (kind == Kind.CRUST && level.getBlockState(pos.above()).is(this)) {
                boolean enclosed = true;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (!level.getBlockState(pos.relative(direction)).is(this)) {
                        enclosed = false;
                        break;
                    }
                }
                if (enclosed) {
                    level.setBlockAndUpdate(pos, ModBlocks.FLUX_GOO.get().withLevel(FluxGooBlock.QUANTA_PER_BLOCK));
                }
            }
        } else if (kind == Kind.CRUST && random.nextInt(20) == 0) {
            level.setBlockAndUpdate(pos, ModBlocks.FLUX_GOO.get().withLevel(FluxGooBlock.QUANTA_PER_BLOCK));
        } else if (kind == Kind.SOIL && random.nextInt(10) == 0) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
    }

    private boolean tryToFall(ServerLevel level, BlockPos pos, BlockPos target) {
        if (!canFallBelow(level, target.below())) {
            return false;
        }
        return FallingTaint.fall(level, pos, target, level.getBlockState(pos));
    }

    private static boolean canFallBelow(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (level.getBlockState(pos.offset(x, y, z)).is(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }
        if (state.is(ModBlocks.FLUX_GOO.get()) && state.getValue(FluxGooBlock.LEVEL) >= 5) {
            return false;
        }
        return state.isAir() || state.is(Blocks.FIRE) || state.is(ModBlocks.FIBROUS_TAINT.get()) || state.canBeReplaced() || !state.getFluidState().isEmpty();
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && kind != Kind.FLESH && entity instanceof LivingEntity living && !(living instanceof TaintedMob)
            && !living.getType().getTags().anyMatch(net.minecraft.tags.EntityTypeTags.UNDEAD::equals)) {
            int chance = living instanceof net.minecraft.world.entity.player.Player ? 100 : 20;
            if (level.getRandom().nextInt(chance) == 0) {
                EffectHooks.applyTaintPoison(living, living instanceof net.minecraft.world.entity.player.Player ? 80 : 160, 0);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }
}
