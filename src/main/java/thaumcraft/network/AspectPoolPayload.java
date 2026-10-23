package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;

public record AspectPoolPayload(Aspect aspect, int amount, int total) implements CustomPacketPayload {
    public static final Type<AspectPoolPayload> TYPE = new Type<>(Thaumcraft.id("aspect_pool"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectPoolPayload> STREAM_CODEC = StreamCodec.composite(
        Aspect.STREAM_CODEC, AspectPoolPayload::aspect,
        ByteBufCodecs.VAR_INT, AspectPoolPayload::amount,
        ByteBufCodecs.VAR_INT, AspectPoolPayload::total,
        AspectPoolPayload::new
    );

    @Override
    public Type<AspectPoolPayload> type() {
        return TYPE;
    }
}
