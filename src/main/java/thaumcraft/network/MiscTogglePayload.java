package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.item.tool.ElementalShovelItem;
import thaumcraft.wand.WandManager;
import thaumcraft.wand.Wands;

public record MiscTogglePayload(boolean sneak) implements CustomPacketPayload {
    public static final Type<MiscTogglePayload> TYPE = new Type<>(Thaumcraft.id("misc_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiscTogglePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, MiscTogglePayload::sneak,
        MiscTogglePayload::new
    );

    public static void handle(MiscTogglePayload payload, Player player) {
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack wand = Wands.heldWand(player);
        if (Wands.isWand(wand)) {
            WandManager.toggleMisc(wand, player.level(), player, payload.sneak());
        }
        if (held.getItem() instanceof ElementalShovelItem) {
            ElementalShovelItem.cycleOrientation(player);
        }
    }

    @Override
    public Type<MiscTogglePayload> type() {
        return TYPE;
    }
}
