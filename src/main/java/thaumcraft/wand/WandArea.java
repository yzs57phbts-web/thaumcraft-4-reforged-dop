package thaumcraft.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WandArea(int x, int y, int z, int dim) {
    public static final WandArea DEFAULT = new WandArea(0, 0, 0, 0);
    public static final Codec<WandArea> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("areax", 0).forGetter(WandArea::x),
        Codec.INT.optionalFieldOf("areay", 0).forGetter(WandArea::y),
        Codec.INT.optionalFieldOf("areaz", 0).forGetter(WandArea::z),
        Codec.INT.optionalFieldOf("areadim", 0).forGetter(WandArea::dim)
    ).apply(instance, WandArea::new));
    public static final StreamCodec<ByteBuf, WandArea> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, WandArea::x,
        ByteBufCodecs.VAR_INT, WandArea::y,
        ByteBufCodecs.VAR_INT, WandArea::z,
        ByteBufCodecs.VAR_INT, WandArea::dim,
        WandArea::new
    );
}
