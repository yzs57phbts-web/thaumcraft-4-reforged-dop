package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.item.armor.ArmorEvents;

public record GearMovementPayload(boolean forward) implements CustomPacketPayload {
    public static final Type<GearMovementPayload> TYPE = new Type<>(Thaumcraft.id("gear_movement"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GearMovementPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, GearMovementPayload::forward, GearMovementPayload::new
    );

    public static void handle(GearMovementPayload payload, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ArmorEvents.setMovingForward(serverPlayer, payload.forward());
        }
    }

    @Override
    public Type<GearMovementPayload> type() {
        return TYPE;
    }
}
