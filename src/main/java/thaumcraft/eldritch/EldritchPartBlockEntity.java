package thaumcraft.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.EldritchContent;

public final class EldritchPartBlockEntity extends BlockEntity {
    public EldritchPartBlockEntity(BlockPos pos, BlockState state) {
        super(EldritchContent.ELDRITCH_PART_ENTITY.get(), pos, state);
    }
}
