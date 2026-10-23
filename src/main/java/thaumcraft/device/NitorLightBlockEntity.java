package thaumcraft.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;

public class NitorLightBlockEntity extends TcBlockEntity {
    public NitorLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NITOR_LIGHT.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos) {
        Fx.get().nitorParticles(level, pos);
    }
}
