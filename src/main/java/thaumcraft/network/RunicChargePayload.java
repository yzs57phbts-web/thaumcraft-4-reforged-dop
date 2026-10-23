package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record RunicChargePayload(int charge, int max) implements CustomPacketPayload {
    public static final Type<RunicChargePayload> TYPE = new Type<>(Thaumcraft.id("runic_charge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RunicChargePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, RunicChargePayload::charge,
        ByteBufCodecs.VAR_INT, RunicChargePayload::max,
        RunicChargePayload::new
    );

    @Override
    public Type<RunicChargePayload> type() {
        return TYPE;
    }
}
