package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record ResearchCompletePayload(String key) implements CustomPacketPayload {
    public static final Type<ResearchCompletePayload> TYPE = new Type<>(Thaumcraft.id("research_complete"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchCompletePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ResearchCompletePayload::key,
        ResearchCompletePayload::new
    );

    @Override
    public Type<ResearchCompletePayload> type() {
        return TYPE;
    }
}
