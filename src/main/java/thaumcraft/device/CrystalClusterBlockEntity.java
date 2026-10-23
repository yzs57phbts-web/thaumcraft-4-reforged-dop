package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class CrystalClusterBlockEntity extends TcBlockEntity {
    public CrystalClusterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_CLUSTER.get(), pos, state);
    }
}
