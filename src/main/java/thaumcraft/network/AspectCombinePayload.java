package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;

public record AspectCombinePayload(BlockPos table, Aspect first, Aspect second, boolean firstBonus, boolean secondBonus) implements CustomPacketPayload {
    public static final Type<AspectCombinePayload> TYPE = new Type<>(Thaumcraft.id("aspect_combine"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectCombinePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, AspectCombinePayload::table,
        Aspect.STREAM_CODEC, AspectCombinePayload::first,
        Aspect.STREAM_CODEC, AspectCombinePayload::second,
        ByteBufCodecs.BOOL, AspectCombinePayload::firstBonus,
        ByteBufCodecs.BOOL, AspectCombinePayload::secondBonus,
        AspectCombinePayload::new
    );

    @Override
    public Type<AspectCombinePayload> type() {
        return TYPE;
    }
}
