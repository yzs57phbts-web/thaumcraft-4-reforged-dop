package thaumcraft.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.EldritchContent;

public final class EldritchNothingBlockEntity extends BlockEntity {
    public EldritchNothingBlockEntity(BlockPos pos, BlockState state) {
        super(EldritchContent.ELDRITCH_NOTHING_ENTITY.get(), pos, state);
    }

    public boolean exposed() {
        return this.getBlockState().getValue(EldritchNothingBlock.EXPOSED);
    }
}
