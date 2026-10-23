package thaumcraft.world.dim;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import javax.annotation.Nullable;

public final class OuterLands {
    public static final ResourceKey<Level> OUTER_LANDS = ResourceKey.create(Registries.DIMENSION, Thaumcraft.id("outer_lands"));

    private OuterLands() {
    }

    public static @Nullable ServerLevel get(ServerLevel from) {
        return from.getServer().getLevel(OUTER_LANDS);
    }
}
