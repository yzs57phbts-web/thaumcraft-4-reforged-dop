package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.fx.Fx;

public final class GlimmerLightBlock extends ArcaneLampLightBlock {
    public GlimmerLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(500) == 0) {
            float x = pos.getX() + random.nextInt(3) - random.nextInt(3);
            float y = pos.getY() + random.nextInt(3) - random.nextInt(3);
            float z = pos.getZ() + random.nextInt(3) - random.nextInt(3);
            Fx.get().sparkle(level, x, y, z, 0.1F + random.nextFloat() * 0.1F, 0xFFFFFF, random.nextBoolean() ? -0.033F : 0.033F);
        }
    }
}
