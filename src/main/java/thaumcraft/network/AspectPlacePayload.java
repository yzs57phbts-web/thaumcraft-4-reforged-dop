package thaumcraft.network;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;

public record AspectPlacePayload(BlockPos table, int q, int r, Optional<Aspect> aspect) implements CustomPacketPayload {
    public static final Type<AspectPlacePayload> TYPE = new Type<>(Thaumcraft.id("aspect_place"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectPlacePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, AspectPlacePayload::table,
        ByteBufCodecs.VAR_INT, AspectPlacePayload::q,
        ByteBufCodecs.VAR_INT, AspectPlacePayload::r,
        ByteBufCodecs.optional(Aspect.STREAM_CODEC), AspectPlacePayload::aspect,
        AspectPlacePayload::new
    );

    @Override
    public Type<AspectPlacePayload> type() {
        return TYPE;
    }
}
