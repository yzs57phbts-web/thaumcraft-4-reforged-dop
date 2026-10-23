package thaumcraft.node;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum NodeType implements StringRepresentable {
    NORMAL,
    UNSTABLE,
    DARK,
    TAINTED,
    HUNGRY,
    PURE;

    public static final Codec<NodeType> CODEC = StringRepresentable.fromEnum(NodeType::values);
    public static final StreamCodec<ByteBuf, NodeType> STREAM_CODEC = ByteBufCodecs.idMapper(index -> values()[index], NodeType::ordinal);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getTranslationKey() {
        return "nodetype." + name() + ".name";
    }
}
