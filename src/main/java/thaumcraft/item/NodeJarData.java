package thaumcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import javax.annotation.Nullable;
import thaumcraft.node.NodeModifier;
import thaumcraft.node.NodeType;

public record NodeJarData(NodeType type, @Nullable NodeModifier modifier, String id) {
    public static final NodeJarData DEFAULT = new NodeJarData(NodeType.NORMAL, null, "");

    public static final Codec<NodeJarData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        NodeType.CODEC.optionalFieldOf("type", NodeType.NORMAL).forGetter(NodeJarData::type),
        NodeModifier.CODEC.optionalFieldOf("modifier").forGetter(data -> Optional.ofNullable(data.modifier())),
        Codec.STRING.optionalFieldOf("id", "").forGetter(NodeJarData::id)
    ).apply(instance, (type, modifier, id) -> new NodeJarData(type, modifier.orElse(null), id)));

    public static final StreamCodec<ByteBuf, NodeJarData> STREAM_CODEC = StreamCodec.composite(
        NodeType.STREAM_CODEC, NodeJarData::type,
        ByteBufCodecs.optional(NodeModifier.STREAM_CODEC), data -> Optional.ofNullable(data.modifier()),
        ByteBufCodecs.STRING_UTF8, NodeJarData::id,
        (type, modifier, id) -> new NodeJarData(type, modifier.orElse(null), id)
    );
}
