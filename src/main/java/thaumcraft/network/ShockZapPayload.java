package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record ShockZapPayload(int sourceId, int targetId) implements CustomPacketPayload {
    public static final Type<ShockZapPayload> TYPE = new Type<>(Thaumcraft.id("shock_zap"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShockZapPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ShockZapPayload::sourceId,
        ByteBufCodecs.VAR_INT, ShockZapPayload::targetId,
        ShockZapPayload::new
    );

    @Override
    public Type<ShockZapPayload> type() {
        return TYPE;
    }
}
