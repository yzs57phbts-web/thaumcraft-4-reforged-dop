package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record InfusionSourceFxPayload(BlockPos pos, byte dx, byte dy, byte dz, int color) implements CustomPacketPayload {
    public static final Type<InfusionSourceFxPayload> TYPE = new Type<>(Thaumcraft.id("infusion_source_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionSourceFxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, InfusionSourceFxPayload::pos,
        ByteBufCodecs.BYTE, InfusionSourceFxPayload::dx,
        ByteBufCodecs.BYTE, InfusionSourceFxPayload::dy,
        ByteBufCodecs.BYTE, InfusionSourceFxPayload::dz,
        ByteBufCodecs.INT, InfusionSourceFxPayload::color,
        InfusionSourceFxPayload::new
    );

    @Override
    public Type<InfusionSourceFxPayload> type() {
        return TYPE;
    }
}
