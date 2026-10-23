package thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.damage.ModDamageTypes;
import thaumcraft.registry.ModFluids;
import thaumcraft.fx.Fx;

public class LiquidDeathBlock extends LiquidBlock {
    public static final int QUANTA_PER_BLOCK = 4;
    private static final int TICK_RATE = 7;

    public LiquidDeathBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        int level = state.getValue(LEVEL);
        int quanta = level == 0 ? QUANTA_PER_BLOCK : QUANTA_PER_BLOCK - level;
        if (quanta <= 0) {
            return net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState();
        }
        return quanta == QUANTA_PER_BLOCK
            ? ModFluids.LIQUID_DEATH.get().getSource(false)
            : ModFluids.FLOWING_LIQUID_DEATH.get().getFlowing(quanta, false);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }


    @Override
    protected boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
        return getQuanta(state) <= 2;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return getQuanta(state) <= 2;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Fx.get().liquidDeath(level, pos, state.getValue(LEVEL), random);
        super.animateTick(state, level, pos, random);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, TICK_RATE);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) {
        level.scheduleTick(pos, this, TICK_RATE);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int quanta = getQuanta(state);
        int remaining = flowVertically(level, pos, quanta);
        if (remaining <= 0) {
            return;
        }
        if (remaining == 1 && remaining == quanta) {
            return;
        }
        if (remaining != quanta && remaining == 1) {
            setQuantaOrAir(level, pos, remaining);
            return;
        }
        int lowerThan = remaining - 1;
        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        BlockPos[] neighbours = new BlockPos[sides.length];
        int[] amounts = new int[sides.length];
        for (int index = 0; index < sides.length; index++) {
            neighbours[index] = pos.relative(sides[index]);
            if (displaceIfPossible(level, neighbours[index])) {
                level.setBlock(neighbours[index], Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            amounts[index] = getQuantaValueBelow(level, neighbours[index], lowerThan);
        }
        int total = remaining;
        int count = 1;
        for (int amount : amounts) {
            if (amount >= 0) {
                count++;
                total += amount;
            }
        }
        if (count == 1) {
            if (remaining != quanta) {
                setQuantaOrAir(level, pos, remaining);
            }
            return;
        }
        int each = total / count;
        int remainder = total % count;
        for (int index = 0; index < amounts.length; index++) {
            if (amounts[index] < 0) {
                continue;
            }
            int newAmount = each;
            if (remainder == count || remainder > 1 && random.nextInt(count - remainder) != 0) {
                newAmount++;
                remainder--;
            }
            if (newAmount != amounts[index]) {
                setQuantaOrAir(level, neighbours[index], newAmount);
                level.scheduleTick(neighbours[index], this, TICK_RATE);
            }
            count--;
        }
        if (remainder > 0) {
            each++;
        }
        setQuantaOrAir(level, pos, each);
    }

    private int flowVertically(ServerLevel level, BlockPos pos, int amountToInput) {
        BlockPos below = pos.below();
        int amountBelow = getQuantaValueBelow(level, below, QUANTA_PER_BLOCK);
        if (amountBelow >= 0) {
            int total = amountBelow + amountToInput;
            int down = Math.min(QUANTA_PER_BLOCK, total);
            setQuantaOrAir(level, below, down);
            level.scheduleTick(below, this, TICK_RATE);
            int remaining = total - down;
            setQuantaOrAir(level, pos, remaining);
            return remaining;
        }
        if (displaceIfPossible(level, below)) {
            level.setBlock(below, withQuanta(amountToInput), Block.UPDATE_ALL);
            level.scheduleTick(below, this, TICK_RATE);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return 0;
        }
        return amountToInput;
    }

    private int getQuantaValueBelow(LevelAccessor level, BlockPos pos, int belowThis) {
        int amount = getQuantaValue(level, pos);
        return amount >= belowThis ? -1 : amount;
    }

    private int getQuantaValue(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return 0;
        }
        return state.is(this) ? getQuanta(state) : -1;
    }

    private int getQuanta(BlockState state) {
        int level = state.getValue(LEVEL);
        return level == 0 ? QUANTA_PER_BLOCK : Math.max(0, QUANTA_PER_BLOCK - level);
    }

    private BlockState withQuanta(int amount) {
        if (amount >= QUANTA_PER_BLOCK) {
            return defaultBlockState();
        }
        return defaultBlockState().setValue(LEVEL, QUANTA_PER_BLOCK - amount);
    }

    private void setQuantaOrAir(ServerLevel level, BlockPos pos, int amount) {
        level.setBlock(pos, amount <= 0 ? Blocks.AIR.defaultBlockState() : withQuanta(amount), Block.UPDATE_ALL);
    }

    private boolean displaceIfPossible(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        if (state.is(this) || state.is(BlockTags.DOORS) || state.is(BlockTags.SIGNS) || state.is(Blocks.SUGAR_CANE)) {
            return false;
        }
        if (!state.getCollisionShape(level, pos).isEmpty() || state.getDestroySpeed(level, pos) < 0.0F || state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL)) {
            return false;
        }
        Block.dropResources(state, level, pos);
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living) {
            int quanta = getQuanta(state);
            living.hurt(serverLevel.damageSources().source(ModDamageTypes.DISSOLVE), quanta);
        }
    }

}
