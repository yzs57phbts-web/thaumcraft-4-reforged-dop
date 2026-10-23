package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record ShieldFxPayload(int entityId, int target) implements CustomPacketPayload {
    public static final Type<ShieldFxPayload> TYPE = new Type<>(Thaumcraft.id("shield_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldFxPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ShieldFxPayload::entityId,
        ByteBufCodecs.VAR_INT, ShieldFxPayload::target,
        ShieldFxPayload::new
    );

    @Override
    public Type<ShieldFxPayload> type() {
        return TYPE;
    }
}
