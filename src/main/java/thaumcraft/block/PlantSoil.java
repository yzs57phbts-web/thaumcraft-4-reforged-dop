package thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public enum PlantSoil {
    PLAINS,
    DESERT,
    CAVE;

    public boolean canSustain(BlockState state, BlockGetter level, BlockPos pos) {
        return switch (this) {
            case PLAINS -> state.is(BlockTags.DIRT);
            case DESERT -> state.is(BlockTags.SAND);
            case CAVE -> state.isFaceSturdy(level, pos, Direction.UP);
        };
    }
}
