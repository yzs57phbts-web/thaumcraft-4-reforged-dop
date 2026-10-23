package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;

public record AspectDiscoveryPayload(Aspect aspect) implements CustomPacketPayload {
    public static final Type<AspectDiscoveryPayload> TYPE = new Type<>(Thaumcraft.id("aspect_discovery"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectDiscoveryPayload> STREAM_CODEC = StreamCodec.composite(
        Aspect.STREAM_CODEC, AspectDiscoveryPayload::aspect,
        AspectDiscoveryPayload::new
    );

    @Override
    public Type<AspectDiscoveryPayload> type() {
        return TYPE;
    }
}
