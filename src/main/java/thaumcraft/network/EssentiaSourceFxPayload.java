package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record EssentiaSourceFxPayload(BlockPos pos, byte dx, byte dy, byte dz, int color) implements CustomPacketPayload {
    public static final Type<EssentiaSourceFxPayload> TYPE = new Type<>(Thaumcraft.id("essentia_source_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EssentiaSourceFxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, EssentiaSourceFxPayload::pos,
        ByteBufCodecs.BYTE, EssentiaSourceFxPayload::dx,
        ByteBufCodecs.BYTE, EssentiaSourceFxPayload::dy,
        ByteBufCodecs.BYTE, EssentiaSourceFxPayload::dz,
        ByteBufCodecs.INT, EssentiaSourceFxPayload::color,
        EssentiaSourceFxPayload::new
    );

    @Override
    public Type<EssentiaSourceFxPayload> type() {
        return TYPE;
    }
}
