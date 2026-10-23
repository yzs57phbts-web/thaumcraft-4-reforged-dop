package thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import javax.annotation.Nullable;

public final class Essentia {
    private Essentia() {
    }

    public static @Nullable EssentiaTransport getConnectable(BlockGetter level, BlockPos pos, Direction face) {
        if (level.getBlockEntity(pos.relative(face)) instanceof EssentiaTransport transport && transport.isConnectable(face.getOpposite())) {
            return transport;
        }
        return null;
    }
}
