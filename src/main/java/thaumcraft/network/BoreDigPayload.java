package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record BoreDigPayload(BlockPos bore, BlockPos target, int stateId, boolean broken) implements CustomPacketPayload {
    public static final Type<BoreDigPayload> TYPE = new Type<>(Thaumcraft.id("bore_dig"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BoreDigPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BoreDigPayload::bore,
        BlockPos.STREAM_CODEC, BoreDigPayload::target,
        ByteBufCodecs.VAR_INT, BoreDigPayload::stateId,
        ByteBufCodecs.BOOL, BoreDigPayload::broken,
        BoreDigPayload::new
    );

    @Override
    public Type<BoreDigPayload> type() {
        return TYPE;
    }
}
