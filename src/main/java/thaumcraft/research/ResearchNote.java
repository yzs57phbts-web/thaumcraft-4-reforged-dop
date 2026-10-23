package thaumcraft.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.aspect.Aspect;

public record ResearchNote(String key, int color, boolean complete, int copies, List<Hex> hexes) {
    public static final Codec<ResearchNote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(ResearchNote::key),
        Codec.INT.optionalFieldOf("color", 0).forGetter(ResearchNote::color),
        Codec.BOOL.optionalFieldOf("complete", false).forGetter(ResearchNote::complete),
        Codec.INT.optionalFieldOf("copies", 0).forGetter(ResearchNote::copies),
        Hex.CODEC.listOf().optionalFieldOf("hexgrid", List.of()).forGetter(ResearchNote::hexes)
    ).apply(instance, ResearchNote::new));
    public static final StreamCodec<ByteBuf, ResearchNote> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ResearchNote::key,
        ByteBufCodecs.VAR_INT, ResearchNote::color,
        ByteBufCodecs.BOOL, ResearchNote::complete,
        ByteBufCodecs.VAR_INT, ResearchNote::copies,
        Hex.STREAM_CODEC.apply(ByteBufCodecs.list()), ResearchNote::hexes,
        ResearchNote::new
    );

    public ResearchNote {
        hexes = List.copyOf(hexes);
    }

    public record Hex(int q, int r, int type, Optional<Aspect> aspect) {
        public static final int EMPTY = 0;
        public static final int GIVEN = 1;
        public static final int PLACED = 2;

        public static final Codec<Hex> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("q").forGetter(Hex::q),
            Codec.INT.fieldOf("r").forGetter(Hex::r),
            Codec.INT.fieldOf("type").forGetter(Hex::type),
            Aspect.CODEC.optionalFieldOf("aspect").forGetter(Hex::aspect)
        ).apply(instance, Hex::new));
        public static final StreamCodec<ByteBuf, Hex> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Hex::q,
            ByteBufCodecs.VAR_INT, Hex::r,
            ByteBufCodecs.VAR_INT, Hex::type,
            ByteBufCodecs.optional(Aspect.STREAM_CODEC), Hex::aspect,
            Hex::new
        );
    }
}
