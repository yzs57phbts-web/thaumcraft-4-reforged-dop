package thaumcraft.entity.golem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;

public record Marker(GlobalPos location, Direction side, int color) {
    public static final Codec<Marker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        GlobalPos.CODEC.fieldOf("location").forGetter(Marker::location),
        Direction.CODEC.fieldOf("side").forGetter(Marker::side),
        Codec.intRange(-1, 15).fieldOf("color").forGetter(Marker::color)
    ).apply(instance, Marker::new));

    public BlockPos pos() {
        return location.pos();
    }

    public boolean inLevel(Level level) {
        return location.dimension().equals(level.dimension());
    }

    public boolean matches(Marker other) {
        return location.equals(other.location) && side == other.side && (color == -1 || color == other.color);
    }
}
