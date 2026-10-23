package thaumcraft.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WandParts(String cap, String rod, boolean sceptre) {
    public static final WandParts DEFAULT = new WandParts("iron", "wood", false);
    public static final Codec<WandParts> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("cap").forGetter(WandParts::cap),
        Codec.STRING.fieldOf("rod").forGetter(WandParts::rod),
        Codec.BOOL.optionalFieldOf("sceptre", false).forGetter(WandParts::sceptre)
    ).apply(instance, WandParts::new));
    public static final StreamCodec<ByteBuf, WandParts> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, WandParts::cap,
        ByteBufCodecs.STRING_UTF8, WandParts::rod,
        ByteBufCodecs.BOOL, WandParts::sceptre,
        WandParts::new
    );

    public WandCap getCap() {
        return WandRegistry.cap(cap);
    }

    public WandRod getRod() {
        return WandRegistry.rod(rod);
    }
}
