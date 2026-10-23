package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.wand.WandManager;
import thaumcraft.wand.Wands;

public record FocusChangePayload(String focus) implements CustomPacketPayload {
    public static final Type<FocusChangePayload> TYPE = new Type<>(Thaumcraft.id("focus_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FocusChangePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, FocusChangePayload::focus,
        FocusChangePayload::new
    );

    public static void handle(FocusChangePayload payload, Player player) {
        ItemStack held = Wands.focusWand(player);
        if (Wands.isWand(held) && !Wands.isSceptre(held)) {
            WandManager.changeFocus(held, player.level(), player, payload.focus());
        }
    }

    @Override
    public Type<FocusChangePayload> type() {
        return TYPE;
    }
}
