package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record NodeZapPayload(float x, float y, float z, float targetX, float targetY, float targetZ) implements CustomPacketPayload {
    public static final Type<NodeZapPayload> TYPE = new Type<>(Thaumcraft.id("node_zap"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NodeZapPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, NodeZapPayload::x,
        ByteBufCodecs.FLOAT, NodeZapPayload::y,
        ByteBufCodecs.FLOAT, NodeZapPayload::z,
        ByteBufCodecs.FLOAT, NodeZapPayload::targetX,
        ByteBufCodecs.FLOAT, NodeZapPayload::targetY,
        ByteBufCodecs.FLOAT, NodeZapPayload::targetZ,
        NodeZapPayload::new
    );

    @Override
    public Type<NodeZapPayload> type() {
        return TYPE;
    }
}
