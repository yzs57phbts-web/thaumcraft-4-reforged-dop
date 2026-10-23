package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record WarpMessagePayload(int warpType, int amount) implements CustomPacketPayload {
    public static final Type<WarpMessagePayload> TYPE = new Type<>(Thaumcraft.id("warp_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WarpMessagePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, WarpMessagePayload::warpType,
        ByteBufCodecs.VAR_INT, WarpMessagePayload::amount,
        WarpMessagePayload::new
    );

    @Override
    public Type<WarpMessagePayload> type() {
        return TYPE;
    }
}
