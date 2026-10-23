package thaumcraft.item.armor;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import thaumcraft.platform.StackKey;

import thaumcraft.compat.ItemStackTemplate;

public final class ArmorKeys {
    public static final StackKey<Boolean> HOVER = StackKey.register("hover", Codec.BOOL, ByteBufCodecs.BOOL);
    public static final StackKey<Integer> HOVER_CHARGE = StackKey.register("hover_charge", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final StackKey<ItemStackTemplate> HARNESS_JAR = StackKey.register("harness_jar", ItemStackTemplate.CODEC, ItemStackTemplate.STREAM_CODEC);

    private ArmorKeys() {
    }

    public static void init() {
    }
}
