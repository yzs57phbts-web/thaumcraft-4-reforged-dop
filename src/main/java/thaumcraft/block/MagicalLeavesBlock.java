package thaumcraft.block;

import net.minecraft.world.phys.Vec3;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MagicalLeavesBlock extends LeavesBlock {
    private final Supplier<? extends Block> sapling;
    private final int saplingChance;
    private final boolean sparkle;

    public MagicalLeavesBlock(Supplier<? extends Block> sapling, int saplingChance, boolean sparkle, Properties properties) {
        super(properties);
        this.sapling = sapling;
        this.saplingChance = saplingChance;
        this.sparkle = sparkle;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            if (random.nextInt(saplingChance) == 0) {
                Block.popResource(level, pos, new ItemStack(sapling.get()));
            }
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (sparkle && random.nextInt(500) == 0) {
            level.addParticle(
                new DustParticleOptions(Vec3.fromRGB24(0xCCDDEE).toVector3f(), 1.0F),
                pos.getX() + 0.5F + random.nextFloat() - random.nextFloat(),
                pos.getY() + 0.5F + random.nextFloat() - random.nextFloat(),
                pos.getZ() + 0.5F + random.nextFloat() - random.nextFloat(),
                0.0,
                0.0,
                0.0
            );
        }
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }
}
