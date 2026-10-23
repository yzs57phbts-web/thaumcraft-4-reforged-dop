package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record VisDrainFxPayload(BlockPos drainer, BlockPos source, int primal) implements CustomPacketPayload {
    public static final Type<VisDrainFxPayload> TYPE = new Type<>(Thaumcraft.id("vis_drain_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VisDrainFxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, VisDrainFxPayload::drainer,
        BlockPos.STREAM_CODEC, VisDrainFxPayload::source,
        ByteBufCodecs.VAR_INT, VisDrainFxPayload::primal,
        VisDrainFxPayload::new
    );

    @Override
    public Type<VisDrainFxPayload> type() {
        return TYPE;
    }
}
