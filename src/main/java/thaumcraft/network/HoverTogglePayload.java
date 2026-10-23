package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.item.armor.Hover;

public record HoverTogglePayload() implements CustomPacketPayload {
    public static final Type<HoverTogglePayload> TYPE = new Type<>(Thaumcraft.id("hover_toggle"));
    public static final StreamCodec<ByteBuf, HoverTogglePayload> STREAM_CODEC = StreamCodec.unit(new HoverTogglePayload());

    public static void handle(HoverTogglePayload payload, Player player) {
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.isCreative() && !serverPlayer.isSpectator()) {
            Hover.toggle(serverPlayer);
        }
    }

    @Override
    public Type<HoverTogglePayload> type() {
        return TYPE;
    }
}
