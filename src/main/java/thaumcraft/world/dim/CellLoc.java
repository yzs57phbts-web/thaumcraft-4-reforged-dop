package thaumcraft.world.dim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CellLoc(int x, int z) implements Comparable<CellLoc> {
    public static final Codec<CellLoc> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("x").forGetter(CellLoc::x),
        Codec.INT.fieldOf("z").forGetter(CellLoc::z)
    ).apply(instance, CellLoc::new));

    @Override
    public int compareTo(CellLoc other) {
        int zOrder = Integer.compare(z, other.z);
        return zOrder != 0 ? zOrder : Integer.compare(x, other.x);
    }
}
