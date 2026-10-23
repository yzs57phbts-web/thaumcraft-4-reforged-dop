package thaumcraft.modern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class FloatingCandleEntity extends BlockEntity {
    public FloatingCandleEntity(BlockPos p,BlockState s){super(ModernContent.CANDLE.get(),p,s);}
    @Override public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider p){return saveWithoutMetadata(p);}
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
}
