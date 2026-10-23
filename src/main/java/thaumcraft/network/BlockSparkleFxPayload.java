package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record BlockSparkleFxPayload(BlockPos pos, int color) implements CustomPacketPayload {
    public static final Type<BlockSparkleFxPayload> TYPE = new Type<>(Thaumcraft.id("block_sparkle_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockSparkleFxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BlockSparkleFxPayload::pos,
        ByteBufCodecs.INT, BlockSparkleFxPayload::color,
        BlockSparkleFxPayload::new
    );

    @Override
    public Type<BlockSparkleFxPayload> type() {
        return TYPE;
    }
}
