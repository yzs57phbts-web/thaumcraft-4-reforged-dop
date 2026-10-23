package thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface InfusionStabiliser {
    boolean canStabiliseInfusion(Level level, BlockPos pos);
}
