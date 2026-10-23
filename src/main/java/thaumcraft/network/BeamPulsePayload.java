package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record BeamPulsePayload(int source, int target, int color) implements CustomPacketPayload {
    public static final Type<BeamPulsePayload> TYPE = new Type<>(Thaumcraft.id("beam_pulse"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BeamPulsePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, BeamPulsePayload::source,
        ByteBufCodecs.INT, BeamPulsePayload::target,
        ByteBufCodecs.INT, BeamPulsePayload::color,
        BeamPulsePayload::new
    );

    @Override
    public Type<BeamPulsePayload> type() {
        return TYPE;
    }
}
