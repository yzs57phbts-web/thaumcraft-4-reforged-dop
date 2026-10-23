package thaumcraft.device;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.aspect.Aspect;
import thaumcraft.platform.StackKey;

public final class WardedKeys {
    public record KeyLink(BlockPos pos, int type) {
        public static final Codec<KeyLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("location").forGetter(KeyLink::pos),
            Codec.INT.fieldOf("type").forGetter(KeyLink::type)
        ).apply(instance, KeyLink::new));
        public static final StreamCodec<ByteBuf, KeyLink> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, KeyLink::pos,
            ByteBufCodecs.VAR_INT, KeyLink::type,
            KeyLink::new
        );
    }

    public static final StackKey<KeyLink> KEY_LINK = StackKey.register("arcane_key_link", KeyLink.CODEC, KeyLink.STREAM_CODEC);
    public static final StackKey<Integer> BANNER_COLOR = StackKey.register("banner_color", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final StackKey<Aspect> BANNER_ASPECT = StackKey.register("banner_aspect", Aspect.CODEC, Aspect.STREAM_CODEC);

    private WardedKeys() {
    }

    public static void init() {
    }
}
