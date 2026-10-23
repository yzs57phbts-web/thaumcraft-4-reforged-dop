package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record WarpEventPayload(int event) implements CustomPacketPayload {
    public static final Type<WarpEventPayload> TYPE = new Type<>(Thaumcraft.id("warp_event"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WarpEventPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, WarpEventPayload::event,
        WarpEventPayload::new
    );

    @Override
    public Type<WarpEventPayload> type() {
        return TYPE;
    }
}
