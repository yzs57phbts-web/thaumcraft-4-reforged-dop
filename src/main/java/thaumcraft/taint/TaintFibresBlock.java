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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.effect.EffectHooks;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;

public class TaintFibresBlock extends Block {
    public enum Kind {
        FIBRES,
        GRASS,
        PLANT,
        STALK,
        MATURE_STALK
    }

    private final Kind kind;

    public TaintFibresBlock(Kind kind, Properties properties) {
        super(properties.randomTicks().noOcclusion());
        this.kind = kind;
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        return new SoundType(0.5F, 0.8F, ModSounds.GORE.get(), ModSounds.GORE.get(), ModSounds.GORE.get(), ModSounds.GORE.get(), ModSounds.GORE.get());
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (kind != Kind.FIBRES) {
            return Block.box(3.2, 0.0, 3.2, 12.8, 12.8, 12.8);
        }
        for (Direction direction : Direction.values()) {
            BlockPos support = pos.relative(direction);
            if (level.getBlockState(support).isFaceSturdy(level, support, direction.getOpposite())) {
                return switch (direction) {
                    case DOWN -> Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
                    case UP -> Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
                    case NORTH -> Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
                    case SOUTH -> Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
                    case WEST -> Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
                    case EAST -> Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
                };
            }
        }
        return Block.box(7.0, 0.0, 7.0, 9.0, 2.0, 9.0);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int adjacent = TaintSpread.adjacentTaint(level, pos);
        TaintBiomes.spread(level, pos, adjacent, 200);
        if (kind == Kind.FIBRES && TaintSpread.onlyAdjacentToTaint(level, pos) || !TaintBiomes.isTainted(level, pos)) {
            level.removeBlock(pos, false);
            return;
        }
        boolean selectedTaintTarget = TaintSpread.tick(level, pos);
        if (selectedTaintTarget && kind == Kind.STALK && random.nextInt(10) == 0 && level.isEmptyBlock(pos.above())) {
            level.setBlockAndUpdate(pos, ModBlocks.MATURE_SPORE_STALK.get().defaultBlockState());
            thaumcraft.entity.monster.taint.TaintSpore spore = thaumcraft.entity.monster.taint.TaintSpore.create(level, pos);
            if (spore != null) {
                level.addFreshEntity(spore);
            } else {
                level.setBlockAndUpdate(pos, ModBlocks.SPORE_STALK.get().defaultBlockState());
            }
        } else if (selectedTaintTarget && kind == Kind.MATURE_STALK && level.getEntitiesOfClass(
            thaumcraft.entity.monster.taint.TaintSpore.class,
            new net.minecraft.world.phys.AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1)
        ).isEmpty()) {
            level.setBlockAndUpdate(pos, ModBlocks.SPORE_STALK.get().defaultBlockState());
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide() && (kind == Kind.FIBRES
            ? TaintSpread.onlyAdjacentToTaint(level, pos)
            : !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP))) {
            level.removeBlock(pos, false);
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && !(living instanceof thaumcraft.entity.TaintedMob)
            && !living.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)
            && level.getRandom().nextInt(living instanceof net.minecraft.world.entity.player.Player ? 1000 : 500) == 0) {
            EffectHooks.applyTaintPoison(living, living instanceof net.minecraft.world.entity.player.Player ? 80 : 160, 0);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }
}
