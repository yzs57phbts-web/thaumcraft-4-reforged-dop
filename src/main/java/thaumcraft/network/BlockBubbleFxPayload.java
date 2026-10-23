package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record BlockBubbleFxPayload(BlockPos pos, int color) implements CustomPacketPayload {
    public static final Type<BlockBubbleFxPayload> TYPE = new Type<>(Thaumcraft.id("block_bubble_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockBubbleFxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BlockBubbleFxPayload::pos,
        ByteBufCodecs.INT, BlockBubbleFxPayload::color,
        BlockBubbleFxPayload::new
    );

    @Override
    public Type<BlockBubbleFxPayload> type() {
        return TYPE;
    }
}
