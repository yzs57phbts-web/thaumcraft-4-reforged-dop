package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModBlockEntities;

public class RestrictTubeBlockEntity extends TubeBlockEntity {
    public RestrictTubeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RESTRICT_TUBE.get(), pos, state);
    }

    protected RestrictTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(filter, true, directional);
    }
}
