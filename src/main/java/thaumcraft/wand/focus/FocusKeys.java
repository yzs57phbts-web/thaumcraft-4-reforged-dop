package thaumcraft.wand.focus;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import thaumcraft.platform.StackKey;

import thaumcraft.compat.ItemStackTemplate;

public final class FocusKeys {
    public static final StackKey<List<Integer>> UPGRADES = StackKey.register(
        "focus_upgrades",
        Codec.INT.listOf(),
        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())
    );
    public static final StackKey<ItemContainerContents> POUCH = StackKey.register("focus_pouch", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);
    public static final StackKey<ItemStackTemplate> PICKED = StackKey.register("focus_picked_block", ItemStackTemplate.CODEC, ItemStackTemplate.STREAM_CODEC);

    private FocusKeys() {
    }

    public static void init() {
    }
}
