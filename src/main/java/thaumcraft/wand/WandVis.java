package thaumcraft.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public record WandVis(int air, int earth, int fire, int water, int order, int entropy) {
    public static final WandVis EMPTY = new WandVis(0, 0, 0, 0, 0, 0);
    public static final Codec<WandVis> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("aer", 0).forGetter(WandVis::air),
        Codec.INT.optionalFieldOf("terra", 0).forGetter(WandVis::earth),
        Codec.INT.optionalFieldOf("ignis", 0).forGetter(WandVis::fire),
        Codec.INT.optionalFieldOf("aqua", 0).forGetter(WandVis::water),
        Codec.INT.optionalFieldOf("ordo", 0).forGetter(WandVis::order),
        Codec.INT.optionalFieldOf("perditio", 0).forGetter(WandVis::entropy)
    ).apply(instance, WandVis::new));
    public static final StreamCodec<ByteBuf, WandVis> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, WandVis::air,
        ByteBufCodecs.VAR_INT, WandVis::earth,
        ByteBufCodecs.VAR_INT, WandVis::fire,
        ByteBufCodecs.VAR_INT, WandVis::water,
        ByteBufCodecs.VAR_INT, WandVis::order,
        ByteBufCodecs.VAR_INT, WandVis::entropy,
        WandVis::new
    );

    public int get(Aspect aspect) {
        return switch (aspect) {
            case AIR -> air;
            case EARTH -> earth;
            case FIRE -> fire;
            case WATER -> water;
            case ORDER -> order;
            case ENTROPY -> entropy;
            default -> 0;
        };
    }

    public WandVis with(Aspect aspect, int amount) {
        return switch (aspect) {
            case AIR -> new WandVis(amount, earth, fire, water, order, entropy);
            case EARTH -> new WandVis(air, amount, fire, water, order, entropy);
            case FIRE -> new WandVis(air, earth, amount, water, order, entropy);
            case WATER -> new WandVis(air, earth, fire, amount, order, entropy);
            case ORDER -> new WandVis(air, earth, fire, water, amount, entropy);
            case ENTROPY -> new WandVis(air, earth, fire, water, order, amount);
            default -> this;
        };
    }

    public AspectList toAspectList() {
        AspectList list = new AspectList();
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            list.merge(aspect, get(aspect));
        }
        return list;
    }

    public static WandVis of(AspectList list) {
        WandVis vis = EMPTY;
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            vis = vis.with(aspect, list.getAmount(aspect));
        }
        return vis;
    }
}
