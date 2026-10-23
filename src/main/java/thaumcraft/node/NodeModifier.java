package thaumcraft.node;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum NodeModifier implements StringRepresentable {
    BRIGHT,
    PALE,
    FADING;

    public static final Codec<NodeModifier> CODEC = StringRepresentable.fromEnum(NodeModifier::values);
    public static final StreamCodec<ByteBuf, NodeModifier> STREAM_CODEC = ByteBufCodecs.idMapper(index -> values()[index], NodeModifier::ordinal);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getTranslationKey() {
        return "nodemod." + name() + ".name";
    }
}
