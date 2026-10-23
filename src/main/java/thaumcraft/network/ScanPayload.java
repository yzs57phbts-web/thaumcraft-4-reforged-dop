package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record ScanPayload(int kind, BlockPos pos, int entityId) implements CustomPacketPayload {
    public static final Type<ScanPayload> TYPE = new Type<>(Thaumcraft.id("scan"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScanPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ScanPayload::kind,
        BlockPos.STREAM_CODEC, ScanPayload::pos,
        ByteBufCodecs.VAR_INT, ScanPayload::entityId,
        ScanPayload::new
    );

    @Override
    public Type<ScanPayload> type() {
        return TYPE;
    }
}
