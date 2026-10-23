package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record PlayerCompletePayload(String key, int action) implements CustomPacketPayload {
    public static final Type<PlayerCompletePayload> TYPE = new Type<>(Thaumcraft.id("player_complete"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerCompletePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PlayerCompletePayload::key,
        ByteBufCodecs.VAR_INT, PlayerCompletePayload::action,
        PlayerCompletePayload::new
    );

    @Override
    public Type<PlayerCompletePayload> type() {
        return TYPE;
    }
}
